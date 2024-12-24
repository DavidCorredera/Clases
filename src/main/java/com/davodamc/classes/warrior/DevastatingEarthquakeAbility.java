package com.davodamc.classes.warrior;

import com.davodamc.Main;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DevastatingEarthquakeAbility {

    private static final String ABILITY_NAME = "terremoto devastador";

    public static void devastatingEarthquakeAbility(Player p, Integer cooldown, int duration, int radius, int quantityParticles, int damage) {

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.playSound(p.getLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 0.5f, 1.0f);

        Location circleCenter = p.getLocation().clone().add(0.0, 1.0, 0.0);

        BukkitRunnable devastatingEarthquakeCircleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createParticleCircle(circleCenter, radius, quantityParticles, 128, 64, 0);

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(circleCenter, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        if (nearbyPlayer == p) continue;
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if (Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                        if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;

                        Location playersLocation = nearbyPlayer.getLocation();
                        double distanceToPlayer = circleCenter.distance(playersLocation);

                        // Aplicar daño al jugador
                        nearbyPlayer.damage(damage, p);

                        if (distanceToPlayer <= radius) {
                            // Crear efecto de terremoto alternante
                            new BukkitRunnable() {
                                int count = 0;
                                boolean upwards = true;
                                final int maxRepetitions = 10; // Define cuántas veces se alterna el movimiento

                                @Override
                                public void run() {
                                    if (count >= maxRepetitions) {
                                        cancel();
                                        return;
                                    }

                                    // Verificar si el jugador sigue dentro del radio
                                    if (circleCenter.distance(nearbyPlayer.getLocation()) > radius) {
                                        cancel();
                                        return;
                                    }

                                    // Alterna entre empujar hacia arriba y hacia abajo
                                    double force = 0.5;
                                    Vector direction = new Vector(0, upwards ? force : -force, 0);
                                    nearbyPlayer.setVelocity(direction);

                                    // Cambia la dirección y aumenta el conteo
                                    upwards = !upwards;
                                    count++;
                                }
                            }.runTaskTimer(Main.getInstance(), 0L, 2L);
                        }
                    }
                }
            }
        };
        devastatingEarthquakeCircleTask.runTaskTimer(Main.getInstance(), 0L, 8L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Cancelar la tarea del círculo
                devastatingEarthquakeCircleTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);
    }
}