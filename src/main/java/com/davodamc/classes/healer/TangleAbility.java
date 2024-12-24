package com.davodamc.classes.healer;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TangleAbility {

    private static final String ABILITY_NAME = "enredar";

    public static void tangleAbility(Player p, Integer cooldown, int quantityParticles, int reachDistance, int radius, int slowAndBlindTime) {

        Block targetBlock = p.getTargetBlock(null, reachDistance);

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Curandero", targetBlock, null)) return;

        Location playerLocation = p.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();

        double distance = playerLocation.distance(targetBlockLocation);

        if (distance > reachDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado lejos para generar " + ABILITY_NAME + "!"));
            return;
        }

        if(targetBlock.getLocation().getBlockY() > p.getLocation().getY()) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado alto para generar " + ABILITY_NAME + "!"));
            return;
        }

        Location tangleLocation = targetBlock.getLocation().clone().add(0.0, 0.5, 0.0);

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 0.8f, 1.0f);

        Main.getInstance().getParticlesManager().createParticleCircle(tangleLocation, radius, 60, 121, 210, 230);

        Main.getInstance().getParticlesManager().showParticles(tangleLocation, Particle.NAUTILUS, quantityParticles);

        Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(targetBlockLocation, radius, radius, radius);
        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player nearbyPlayer) {
                Location playersLocation = nearbyPlayer.getLocation();
                if(nearbyPlayer == p) continue;
                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                double distanceToPlayer = targetBlockLocation.distance(playersLocation);
                if (distanceToPlayer <= radius) {
                    if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                    if(!nearbyPlayer.hasPotionEffect(PotionEffectType.BLINDNESS))
                        nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, slowAndBlindTime, 10));
                    if(!nearbyPlayer.hasPotionEffect(PotionEffectType.SLOW))
                        nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowAndBlindTime, 10));
                }
            }
        }
    }
}
