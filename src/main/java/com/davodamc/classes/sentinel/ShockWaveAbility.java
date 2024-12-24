package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShockWaveAbility {

    private static final String ABILITY_NAME = "onda de choque";

    public static void shockWaveAbility(Player p, Integer cooldown, int timesToLoop, int radius, int timeBetweenLoop, double upwardsForce, double forwardsForce) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Centinela", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        new BukkitRunnable() {
            int blockNumberLoop = 3;
            int loopCount = 0;

            @Override
            public void run() {
                if (loopCount >= timesToLoop) {
                    this.cancel();
                    return;
                }

                // Obtener el bloque frente al jugador en la dirección del loop
                Location blockLocation = p.getLocation().add(p.getLocation().getDirection().multiply(blockNumberLoop));
                blockNumberLoop += 3;

                // Empujar jugadores cercanos
                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(blockLocation, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                // ASYNC
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, blockLocation, 1);
                        p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.2f, 0.2f);

                        for (Entity entity : nearbyEntities) {
                            if (entity instanceof Player nearbyPlayer) {
                                Location playersLocation = nearbyPlayer.getLocation();
                                if(nearbyPlayer == p) continue;
                                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                                if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                                double distanceToPlayer = blockLocation.distance(playersLocation);
                                if (distanceToPlayer <= radius) {
                                    if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                                    Vector direction = nearbyPlayer.getLocation().toVector().subtract(blockLocation.toVector()).normalize();
                                    nearbyPlayer.setVelocity(direction.multiply(forwardsForce).setY(upwardsForce));
                                }
                            }
                        }
                    }
                }.runTaskAsynchronously(Main.getInstance());

                loopCount++;
            }
        }.runTaskTimer(Main.getInstance(), 0, timeBetweenLoop);
    }
}