package com.davodamc.classes.mage;

import com.davodamc.Main;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CursedCloneAbility {

    private static final String ABILITY_NAME = "clon maldito";

    public static void cursedCloneAbility(Player p, Integer cooldown, int timeToExplode, int distance, int radius, int effectsTier, int effectsTime) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Mago", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location finalLocation = p.getLocation().clone().add(p.getLocation().getDirection().multiply(distance));

        Main.getInstance().getAbilitiesManager().createNPC(p, p.getLocation(), finalLocation);

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().showParticles(finalLocation, Particle.CLOUD, 40);
                p.playSound(p.getLocation(), Sound.ENTITY_BAT_DEATH, 0.8f, 1.3f);

                Main.getInstance().getAbilitiesManager().deleteNpcs(p.getName());

                Main.getInstance().getParticlesManager().createParticleCircle(finalLocation.clone().add(0.0, 0.5, 0.0), radius, 60, 10, 10, 10);

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(finalLocation, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playersLocation = nearbyPlayer.getLocation();
                        if(nearbyPlayer == p) continue;
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                        double distanceToPlayer = finalLocation.distance(playersLocation);
                        if (distanceToPlayer <= radius) {
                            if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                            if(!nearbyPlayer.hasPotionEffect(PotionEffectType.BLINDNESS))
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, effectsTime, effectsTier));
                            if(!nearbyPlayer.hasPotionEffect(PotionEffectType.WITHER))
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, effectsTime, effectsTier));
                            if(!nearbyPlayer.hasPotionEffect(PotionEffectType.SLOW))
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, effectsTime, effectsTier));

                            Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.SPELL_INSTANT, 20);
                        }
                    }
                }
            }
        }.runTaskLater(Main.getInstance(), timeToExplode);

    }
}