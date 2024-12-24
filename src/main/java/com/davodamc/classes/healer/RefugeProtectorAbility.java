package com.davodamc.classes.healer;

import com.davodamc.Main;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RefugeProtectorAbility {

    private static final String ABILITY_NAME = "refugio protector";

    public static void refugeProtectorAbility(Player p, Integer cooldown, int radius, int duration) {
        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Curandero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);
        Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.CRIT_MAGIC, 15);
        p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.6f, 1.0f);

        BukkitRunnable refugeProtectorCircleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(p.getLocation())) return;
                Main.getInstance().getParticlesManager().createParticleCircle(p.getLocation().clone().add(0.0, 0.5, 0.0), radius, 80, 0, 120, 255);
                Main.getInstance().getParticlesManager().createParticleCircle(p.getLocation().clone().add(0.0, 3.5, 0.0), radius, 80, 0, 120, 255);

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(p.getLocation(), radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        if(nearbyPlayer == p) continue;
                        if(Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                        if(Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                        double distanceToPlayer = p.getLocation().distance(nearbyPlayer.getLocation());

                        if (distanceToPlayer <= radius) {
                            // Calcula el vector desde el jugador hacia el centro del círculo
                            Vector toCenter = p.getLocation().toVector().subtract(nearbyPlayer.getLocation().toVector()).normalize();

                            // Agrega un empuje hacia atrás en la dirección opuesta al centro
                            Vector backwardsKnockback = toCenter.multiply(-1.2);
                            // Pequeño impulso arriba para que no se quede pillado en un bloque dentro del refugio protector
                            backwardsKnockback.setY(0.5);

                            // Aplica la velocidad al jugador
                            nearbyPlayer.setVelocity(backwardsKnockback);
                        }
                    }
                }
            }
        };
        refugeProtectorCircleTask.runTaskTimer(Main.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Cancelar la tarea del halo
                refugeProtectorCircleTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);
    }
}