package com.davodamc.classes.mage;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeleportAbility {

    private static final String ABILITY_NAME = "teletransporte";

    public static void teleportAbility(Player p, int cooldown, int quantityParticles, int reachDistance) {

        Block targetBlock = p.getTargetBlock(null, reachDistance);

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Mago", targetBlock, null)) return;

        Location playerLocation = p.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();

        double distance = playerLocation.distance(targetBlockLocation);

        if (distance > reachDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes teletransportarte hacia un bloque tan lejano!"));
            return;
        }

        Block blockAbove = targetBlock.getLocation().clone().add(0, 1, 0).getBlock();

        if (blockAbove.getType() != Material.AIR) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes teletransportarte a un bloque que tiene otro encima!"));
            return;
        }

        if (targetBlock.getType() == Material.BARRIER) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes teletransportarte a un bloque invisible!"));
            return;
        }

        Location teleportLocation = targetBlock.getLocation().clone().add(0.5, 1.5, 0.5);

        teleportLocation.setYaw(p.getLocation().getYaw());
        teleportLocation.setPitch(p.getLocation().getPitch());

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.teleport(teleportLocation);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.SPELL_WITCH, quantityParticles);
    }
}
