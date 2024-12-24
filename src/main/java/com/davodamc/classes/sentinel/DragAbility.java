package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DragAbility {

    private static final String ABILITY_NAME = "arrastre";

    public static void dragAbility(Player p, Integer cooldown, int radius, int reachDistance, int quantityParticles, float speedAttract, long duration) {

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

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location sphereCenter = targetBlockLocation.clone().add(0.0, 1.5, 0.0);

        // Mostrar las partículas de la esfera en el hilo principal
        Main.getInstance().getParticlesManager().showParticles(sphereCenter, Particle.SMOKE_NORMAL, quantityParticles);

        BukkitRunnable dragCircleTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Crear la circunferencia de partículas
                Main.getInstance().getParticlesManager().createParticleCircle(sphereCenter, radius, 140, 128, 128, 128);
                Main.getInstance().getParticlesManager().showParticles(sphereCenter, Particle.SMOKE_NORMAL, quantityParticles);

                // Obtener las entidades de forma sincronizada (en el hilo principal)
                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(sphereCenter, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                // Realizar la lógica de movimiento y partículas de forma asincrónica
                Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player nearbyPlayer) {
                            if (nearbyPlayer == p) continue;
                            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                            if (Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                            if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;

                            double distanceToPlayer = sphereCenter.distance(nearbyPlayer.getLocation());
                            if (distanceToPlayer <= radius) {
                                Vector direction = sphereCenter.toVector().subtract(nearbyPlayer.getLocation().toVector()).normalize();
                                nearbyPlayer.setVelocity(direction.multiply(speedAttract));

                                // Mostrar partículas de la atracción
                                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                                    Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.SMOKE_LARGE, quantityParticles);
                                });
                            }
                        }
                    }
                });
            }
        };
        dragCircleTask.runTaskTimer(Main.getInstance(), 0L, 10L);

        new BukkitRunnable() {
            @Override
            public void run() {
                dragCircleTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);
    }
}