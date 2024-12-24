package com.davodamc.classes.mage;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExplosiveRodAbility {

    private static final String ABILITY_NAME = "varita explosiva";

    public static void explosiveRodAbility(Player p, Integer cooldown, double damage, int reachDistance, double radius) {

        Block targetBlock = p.getTargetBlock(null, reachDistance);
        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();
        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();

        if (!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Mago", targetBlock, null)) return;

        Location playerLocation = p.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();

        double distance = playerLocation.distance(targetBlockLocation);

        if (distance > reachDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado lejos y tu " + ABILITY_NAME + " no llega!"));
            return;
        }

        p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);

        Location sphereCenter = targetBlockLocation.clone().add(0.0, 1.0, 0.0);

        particlesManager.spawnParticleSphere(p, sphereCenter, Particle.SPELL_WITCH, 0, 0, 0, radius, 100, 70);

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location enderPearlSpawn = targetBlockLocation.clone().add(0.0, 11.0, 0.0);

                EnderPearl enderPearl = p.getWorld().spawn(enderPearlSpawn, EnderPearl.class);

                // Aplicar velocidad hacia abajo a la perla de Ender
                enderPearl.setVelocity(new Vector(0.0, -1.0, 0.0));

                particlesManager.explosiveParticles(targetBlockLocation, Particle.FIREWORKS_SPARK, 1);
                p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                particlesManager.cancelParticleTask(p);

                // Obtener todas las entidades dentro del radio especificado
                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(targetBlockLocation, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playerLocation = nearbyPlayer.getLocation();
                        if(nearbyPlayer == p) continue;
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(abilitiesManager.isNPCOrStaff(nearbyPlayer)) continue;
                        if (abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;
                        double distanceToPlayer = targetBlockLocation.distance(playerLocation);
                        if (distanceToPlayer <= radius) {
                            // Aplicar daño al jugador
                            abilitiesManager.applySwordDamage(p, nearbyPlayer, damage);
                        }
                    }
                }

            }
        }.runTaskLater(Main.getInstance(), 60);

    }
}