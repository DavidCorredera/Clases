package com.davodamc.classes.mage;

import com.davodamc.Main;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CorrosiveCurseAbility {

    private static final String ABILITY_NAME = "maldición corrosiva";

    public static void corrosiveCurseAbility(Player p, Integer cooldown, int radius, int witherTier, int quantityParticles, int duration) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Mago", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.0f);

        Location circleCenter = p.getLocation().clone().add(0.0, 1.0, 0.0);

        BukkitRunnable corrosiveCurseCircleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createParticleCircle(circleCenter, radius, quantityParticles, 10, 10, 10);

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(circleCenter, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playersLocation = nearbyPlayer.getLocation();
                        if(nearbyPlayer == p) continue;
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                        double distanceToPlayer = circleCenter.distance(playersLocation);
                        if (distanceToPlayer <= radius) {
                            if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                            if(nearbyPlayer.hasPotionEffect(PotionEffectType.WITHER)) continue;
                            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20, witherTier));
                        }
                    }
                }
            }
        }; corrosiveCurseCircleTask.runTaskTimer(Main.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Cancelar la tarea del halo
                corrosiveCurseCircleTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);
    }
}
