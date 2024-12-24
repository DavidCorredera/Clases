package com.davodamc.classes.assassin;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NeedleAbility {

    private static final String ABILITY_NAME = "aguja";

    public static void needleAbility(Player p, Player clickedPlayer, Integer cooldown, int timeSlowness, int quantityParticles) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, clickedPlayer)) return;

        if(abilitiesManager.isNPCOrStaff(clickedPlayer)) return;

        if(clickedPlayer.hasPotionEffect(PotionEffectType.SLOW)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Tu víctima ya está inmovilizada!"));
            return;
        }

        if(abilitiesManager.isClanAlly(p, clickedPlayer)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes inmovilizar a un aliado de tu clan!"));
            return;
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has inmovilizado a &b" + clickedPlayer.getName() + " &fdurante &b" + timeSlowness / 20 + "&f segundos!"));
        clickedPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has sido &binmovilizado &fpor &b" + p.getName() + " &fdurante &b" + timeSlowness / 20 + " &fsegundos!"));

        p.playSound(p.getLocation(), Sound.BLOCK_SOUL_SAND_FALL, 0.3f, 0.5f);
        Main.getInstance().getParticlesManager().showParticles(clickedPlayer.getLocation(), Particle.DAMAGE_INDICATOR, quantityParticles);

        if(!clickedPlayer.hasPotionEffect(PotionEffectType.SLOW))
            clickedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, timeSlowness, 10));
    }
}