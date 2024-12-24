package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeadlyFogAbility {

    private static final String ABILITY_NAME = "niebla mortal";

    public static void deadlyFogAbility(Player p, Integer cooldown, int quantityParticles, int reachDistance, int radius, int duration, int damage) {

        Block targetBlock = p.getTargetBlock(null, reachDistance);

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Centinela", targetBlock, null)) return;

        Location playerLocation = p.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();

        double distance = playerLocation.distance(targetBlockLocation);

        if (distance > reachDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado lejos para generar " + ABILITY_NAME + "!"));
            return;
        }

        if (targetBlock.getLocation().getBlockY() > p.getLocation().getY()) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado alto para generar " + ABILITY_NAME + "!"));
            return;
        }

        Location deadlyFogLocation = targetBlock.getLocation().clone().add(0.5, 1.0, 0.5);

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ENTITY_PHANTOM_AMBIENT, 0.8f, 1.0f);

        Main.getInstance().getParticlesManager().showParticles(deadlyFogLocation, Particle.SPELL_INSTANT, quantityParticles);

        BukkitRunnable deadlyFogCircleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createParticleTube(deadlyFogLocation, radius, 80, Particle.CLOUD, 8, 0.5);

                // Obtener entidades en el hilo principal
                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(deadlyFogLocation, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                // Procesar entidades en un hilo asincrónico
                Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player nearbyPlayer) {
                            if (nearbyPlayer == p) continue;
                            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                            if (Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;

                            double distanceToPlayer = deadlyFogLocation.distance(nearbyPlayer.getLocation());
                            if (distanceToPlayer <= radius) {
                                if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;

                                Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.FLAME, quantityParticles - 15);

                                // Volver al hilo principal para daño
                                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                                    nearbyPlayer.damage(damage, p);
                                });
                            }
                        }
                    }
                });
            }
        };
        deadlyFogCircleTask.runTaskTimer(Main.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                deadlyFogCircleTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);
    }
}