package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CampfireAbility {

    private static final String ABILITY_NAME = "campamento";

    // Mapa para almacenar los bloques del campamento
    public static final Map<UUID, List<Block>> campfireBlocks = new HashMap<>();

    public static void campfireAbility(Player p, int maxDistance, int cooldown, int timesToLoop, int radius, Double heartsHeal, int timeBetweenLoop) {

        Block targetBlock = p.getTargetBlock(null, maxDistance);

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Centinela", targetBlock, null)) return;

        double distance = p.getLocation().distance(targetBlock.getLocation());

        if (distance > maxDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes colocar el campamento en un bloque tan lejano!"));
            return;
        }

        Block blockAbove = targetBlock.getLocation().clone().add(0, 1, 0).getBlock();
        Block blockAbove2 = targetBlock.getLocation().clone().add(0, 2, 0).getBlock();
        Block blockAbove3 = targetBlock.getLocation().clone().add(0, 3, 0).getBlock(); // Tercer bloque a detectar

        if (targetBlock.getType() == Material.AIR) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes colocar el campamento en el aire!"));
            return;
        }

        if (blockAbove.getType() != Material.AIR || blockAbove2.getType() != Material.AIR || blockAbove3.getType() != Material.AIR) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No hay espacio suficiente para generar el campamento!"));
            return;
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        // Coloca los bloques para el campamento de forma síncrona
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            blockAbove.setType(Material.GRASS_BLOCK);
            blockAbove2.setType(Material.SOUL_CAMPFIRE);

            // Guardar los bloques en el mapa
            campfireBlocks.put(p.getUniqueId(), List.of(blockAbove, blockAbove2));

            Location firstParticleCircle = blockAbove2.getLocation().add(0.5, 1.5, 0.5); // Encima del campamento
            Location secondParticleCircle = blockAbove.getLocation().add(0.5, 0.5, 0.5); // Alrededor del campamento

            loopWithDelay(p, secondParticleCircle, 0, timesToLoop, radius, timeBetweenLoop, blockAbove, blockAbove2, heartsHeal);

            // Tarea para las partículas del campamento
            BukkitRunnable campfireCircleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    // Ejecuta las partículas de forma asíncrona
                    Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                        Main.getInstance().getParticlesManager().createParticleCircle(firstParticleCircle, 0.5, 20, 203, 200, 164); // Arriba del campamento
                        Main.getInstance().getParticlesManager().createParticleCircle(secondParticleCircle, radius, 100, 0, 255, 0); // Alrededor del campamento
                    });
                }
            };
            campfireCircleTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

            // Detener la tarea después de que se complete la duración
            new BukkitRunnable() {
                @Override
                public void run() {
                    campfireCircleTask.cancel();
                }
            }.runTaskLaterAsynchronously(Main.getInstance(), (long) timesToLoop * timeBetweenLoop);
        });
    }

    // Función que ejecuta el bucle con retraso
    public static void loopWithDelay(Player p, Location center, int iteration, int timesToLoop, double radius, int timeBetweenLoop, Block firstBlockToRemove, Block secondBlockToRemove, double heartsHeal) {
        if (iteration >= timesToLoop) {
            // Las modificaciones de bloques deben realizarse en el hilo principal
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                firstBlockToRemove.setType(Material.AIR);
                secondBlockToRemove.setType(Material.AIR);
            });
            return;
        }

        // Ejecutar la lógica del bucle con un retraso en el hilo principal
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {

            Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(center, radius, radius, radius);
            List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

            // Obtener las entidades cercanas de forma asíncrona
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {

                        // Comprobaciones adicionales de forma asíncrona
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if (Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;

                        double distanceToPlayer = center.distance(nearbyPlayer.getLocation());
                        if (distanceToPlayer <= radius) {
                            if (!Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;

                            Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.HEART, 8);

                            double newHealth = p.getHealth() + heartsHeal;
                            double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

                            // Limitar la salud máxima a 20 corazones
                            if (newHealth > maxHealth) newHealth = maxHealth;

                            // Cura al jugador de forma síncrona
                            double finalNewHealth = newHealth;
                            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {

                                p.setHealth(finalNewHealth);
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                            });
                        }
                    }
                }

                // Llamar a la siguiente iteración con un retraso
                loopWithDelay(p, center, iteration + 1, timesToLoop, radius, timeBetweenLoop, firstBlockToRemove, secondBlockToRemove, heartsHeal);

            }); // Ejecutar asíncrono el ciclo de entidades cercanas

        }, timeBetweenLoop); // Ejecutar después de X ticks
    }
}