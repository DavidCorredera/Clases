package com.davodamc.classes.assassin;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SacrificeAbility {

    private static final String ABILITY_NAME = "sacrificio";

    public static void sacrificeAbility(Player p, Player clickedPlayer, Integer cooldown, int takenDamage, int quantityParticles) {
        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, clickedPlayer)) return;

        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(clickedPlayer)) return;

        if(Main.getInstance().getAbilitiesManager().isClanAlly(p, clickedPlayer)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes sacrificarte con un aliado de tu clan!"));
            return;
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        int takenDamageBySelf = takenDamage / 2;

        Main.getInstance().getAbilitiesManager().applySwordDamage(p, clickedPlayer, takenDamage); // DAÑO AL CLICKEDPLAYER
        Main.getInstance().getAbilitiesManager().applySwordDamage(clickedPlayer, p, takenDamageBySelf); // DAÑO AL QUE USA LA HABILIDAD

        p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has usado sacrificio contra &b" + clickedPlayer.getName() + "&f!"));
        clickedPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡&b" + p.getName() + " &fha usado sacrificio contra ti!"));

        p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 0.5f);
        Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.FLAME, quantityParticles);
        Main.getInstance().getParticlesManager().showParticles(clickedPlayer.getLocation(), Particle.FLAME, quantityParticles);
    }
}