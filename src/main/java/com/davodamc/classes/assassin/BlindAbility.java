package com.davodamc.classes.assassin;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlindAbility {

    private static final String ABILITY_NAME = "ceguera";

    public static void blindAbility(Player p, Integer cooldown, int blindnessRadius, int blindnessTier, int blindnessTime) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();
        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location blindLocation = p.getLocation().clone().add(0.0, 3.5, 0.0); // loc para spawnear el ojo y donde se va a ejecutar la ceguera
        World world = blindLocation.getWorld();

        EnderSignal eye = (EnderSignal) world.spawnEntity(blindLocation, EntityType.ENDER_SIGNAL);
        eye.setDropItem(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (eye.isValid()) {
                    eye.teleport(blindLocation); // Teletransportar el ojo de vuelta a la misma ubicación cada tick
                } else {
                    cancel(); // Si el ojo es destruido o se desvanece, se cancela la tarea
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L); // Ejecutar cada tick

        particlesManager.createParticleCircle(p.getLocation().clone().add(0.0, 0.5, 0.0), blindnessRadius, 60, 10, 10, 10);

        new BukkitRunnable() {
            @Override
            public void run() {
                particlesManager.showParticles(p.getLocation(), Particle.SMALL_FLAME, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_RABBIT_DEATH, 0.5f, 1.0f);

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(blindLocation, blindnessRadius, blindnessRadius, blindnessRadius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playersLocation = nearbyPlayer.getLocation();
                        if(nearbyPlayer == p) continue;
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(abilitiesManager.isNPCOrStaff(nearbyPlayer)) continue;
                        double distanceToPlayer = blindLocation.distance(playersLocation);
                        if (distanceToPlayer <= blindnessRadius) {
                            if (abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;
                            if(nearbyPlayer.hasPotionEffect(PotionEffectType.BLINDNESS)) continue;
                            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessTime, blindnessTier));
                            particlesManager.showParticles(blindLocation, Particle.SPELL_WITCH, 40);
                        }
                    }
                }

                eye.remove();
            }
        }.runTaskLater(Main.getInstance(), 60L);

    }
}
