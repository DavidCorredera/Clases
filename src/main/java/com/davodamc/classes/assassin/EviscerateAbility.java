package com.davodamc.classes.assassin;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EviscerateAbility {

    private static final String ABILITY_NAME = "destripar";

    public static void eviscerateAbility(Player p, Integer cooldown, int maxDistance, int damage) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();

        Player targetPlayer = abilitiesManager.getTargetPlayer(p, 35);
        if (!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, targetPlayer)) return;

        if (targetPlayer == null || targetPlayer.getLocation().distance(p.getLocation()) > maxDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Debes estar mirando a un jugador dentro de " + maxDistance + " bloques!"));
            return;
        }

        if(abilitiesManager.isNPCOrStaff(targetPlayer)) return;

        Location tpLocation = targetPlayer.getLocation();
        Location locBehind = tpLocation.clone().add(tpLocation.getDirection().setY(0).normalize().multiply(-2));

        if (locBehind.getBlock().getType() != Material.AIR) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes teletransportarte hacia jugador que tiene un bloque sólido a sus espaldas!"));
            return;
        }
        if (abilitiesManager.isClanAlly(p, targetPlayer)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&cNo puedes utilizar esta habilidad contra un aliado/miembro de tu clan."));
            return;
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        abilitiesManager.soundInRadius(p, Sound.ENTITY_FOX_BITE, 5.0f, 1.0f);
        abilitiesManager.soundInRadius(p, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.0f);

        Location teleportLocation = tpLocation.clone().add(tpLocation.getDirection().setY(0).normalize().multiply(-2));
        teleportLocation.setY(tpLocation.getY());

        p.teleport(teleportLocation);

        abilitiesManager.applySwordDamage(p, targetPlayer, damage);

        Main.getInstance().getParticlesManager().showParticles(targetPlayer.getLocation(), Particle.SWEEP_ATTACK, 4);
    }
}