package com.davodamc.classes.warrior;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DisarmAbility {

    private static final String ABILITY_NAME = "desarmar";

    public static void disarmAbility(Player p, Integer cooldown, int disarmRadius, int disarmTier, int disarmTime) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location playerLocation = p.getLocation().clone();

        Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.SOUL_FIRE_FLAME, 10);
        p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.5f, 1.0f);

        Main.getInstance().getParticlesManager().createParticleCircle(p.getLocation().clone().add(0.0, 0.5, 0.0), disarmRadius, 80, 255, 255, 255);

        Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(playerLocation, disarmRadius, disarmRadius, disarmRadius);
        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

        List<String> affectedPlayerNames = new ArrayList<>();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player nearbyPlayer) {
                Location playersLocation = nearbyPlayer.getLocation();
                if(nearbyPlayer == p) continue;
                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                if(Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                double distanceToPlayer = playerLocation.distance(playersLocation);
                if (distanceToPlayer <= disarmRadius) {
                    if(nearbyPlayer.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) continue;
                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, disarmTime, disarmTier));
                    nearbyPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has sido &bdesarmado &fpor &b" + p.getName() + "&f!"));
                    affectedPlayerNames.add(nearbyPlayer.getName());
                }
            }
        }
        if (!affectedPlayerNames.isEmpty()) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&fHas desarmado a: &b" + String.join(", ", affectedPlayerNames) + "&f."));
        } else {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No has desarmado a ningún jugador!"));
        }
    }
}
