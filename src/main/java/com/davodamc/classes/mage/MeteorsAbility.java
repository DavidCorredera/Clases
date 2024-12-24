package com.davodamc.classes.mage;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MeteorsAbility {

    private static final String ABILITY_NAME = "meteoritos";

    public static void meteorsAbility(Player p, Integer cooldown, int quantityParticles, int reachDistance, int radius, int damage, int meteorCount) {

        Block targetBlock = p.getTargetBlock(null, reachDistance);

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Mago", targetBlock, null)) return;

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

        for (int i = 0; i < meteorCount; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Calcular una ubicación aleatoria dentro del radio
                    double offsetX = (Math.random() - 0.5) * radius * 2; // Aleatorio entre -radius y +radius
                    double offsetZ = (Math.random() - 0.5) * radius * 2; // Aleatorio entre -radius y +radius
                    Location spawnLocation = targetBlockLocation.clone().add(offsetX, 20, offsetZ); // Generar arriba del bloque objetivo

                    ArmorStand meteor = (ArmorStand) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.ARMOR_STAND);
                    meteor.setVisible(false);
                    meteor.setGravity(false);
                    meteor.setHelmet(new org.bukkit.inventory.ItemStack(Material.COAL_BLOCK)); // Bloque de carbón en la cabeza

                    // Crear una tarea para mover el "meteorito"
                    new BukkitRunnable() {
                        double rotationAngle = 0;
                        double velocityX = (Math.random() - 0.5) * 0.5; // Aumentar la velocidad horizontal
                        double velocityZ = (Math.random() - 0.5) * 0.5; // Aumentar la velocidad horizontal
                        double downwardSpeed = 0.5; // Velocidad de caída vertical

                        @Override
                        public void run() {
                            if (meteor.isDead() || meteor.getLocation().getY() <= targetBlock.getLocation().getY()) {
                                if (meteor.getLocation().getWorld().equals(p.getWorld())) {
                                    // Explosión final
                                    p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, meteor.getLocation(), 1);
                                    meteor.getWorld().playSound(meteor.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2F, 0.5F);
                                    meteor.remove();
                                    Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(meteor.getLocation(), radius, radius, radius);
                                    List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                                    for (Entity entity : nearbyEntities) {
                                        if (entity instanceof Player nearbyPlayer) {
                                            Location playersLocation = nearbyPlayer.getLocation();
                                            if (nearbyPlayer == p) continue;
                                            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation()))
                                                continue;
                                            if (Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer))
                                                continue;
                                            double distanceToPlayer = meteor.getLocation().distance(playersLocation);
                                            if (distanceToPlayer <= radius) {
                                                if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer))
                                                    continue;
                                                Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.SOUL_FIRE_FLAME, quantityParticles);
                                                Vector direction = nearbyPlayer.getLocation().toVector().subtract(meteor.getLocation().toVector()).normalize();
                                                nearbyPlayer.setVelocity(direction.multiply(1.2).setY(0.6));
                                                Main.getInstance().getAbilitiesManager().applySwordDamage(p, nearbyPlayer, damage);
                                                //nearbyPlayer.damage(damage, p);
                                            }
                                        }
                                    }
                                    this.cancel();
                                    return;
                                }
                            }

                            // Movimiento hacia abajo y rotación con desplazamiento horizontal
                            meteor.teleport(meteor.getLocation().add(new Vector(velocityX, -downwardSpeed, velocityZ))); // Velocidad de caída y desplazamiento
                            rotationAngle += 0.15; // Velocidad de rotación
                            meteor.setHeadPose(new EulerAngle(rotationAngle, rotationAngle, rotationAngle));

                            // Estela de explosiones pequeñas y partículas
                            meteor.getWorld().spawnParticle(Particle.SMOKE_LARGE, meteor.getLocation(), 5, 0.3, 0.3, 0.3, 0.1);
                            meteor.getWorld().spawnParticle(Particle.SMALL_FLAME, meteor.getLocation(), 3, 0.1, 0.1, 0.1, 0.3);
                            meteor.getWorld().playSound(meteor.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3F, 0.5F);
                        }
                    }.runTaskTimer(Main.getInstance(), 0L, 2L); // Repite cada 2 ticks
                }
            }.runTaskLater(Main.getInstance(), i * 10L); // Retraso de 10 ticks por meteorito
        }

        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 1.0f);
    }
}