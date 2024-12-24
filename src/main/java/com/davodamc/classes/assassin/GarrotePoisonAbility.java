package com.davodamc.classes.assassin;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GarrotePoisonAbility {

    private static final String ABILITY_NAME = "garrote venenoso";

    public static void garrotePoisonAbility(Player p, Player clickedPlayer, Integer cooldown, int poisonTime, int poisonTier, int quantityParticles) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, clickedPlayer)) return;

        if(abilitiesManager.isNPCOrStaff(clickedPlayer)) return;

        if(clickedPlayer.hasPotionEffect(PotionEffectType.POISON)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Tu víctima ya está envenenada!"));
            return;
        }

        if(abilitiesManager.isClanAlly(p, clickedPlayer)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes envenenar a un aliado de tu clan!"));
            return;
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has envenenado a &b" + clickedPlayer.getName() + " &fdurante &b" + poisonTime / 20 + "&f segundos!"));
        clickedPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has sido &benvenenado &fpor &b" + p.getName() + " &fdurante &b" + poisonTime / 20 + " &fsegundos!"));

        p.playSound(p.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 0.3f, 0.5f);
        Main.getInstance().getParticlesManager().showParticles(clickedPlayer.getLocation(), Particle.FALLING_SPORE_BLOSSOM, quantityParticles);

        if(!clickedPlayer.hasPotionEffect(PotionEffectType.POISON)) clickedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonTime, poisonTier));
    }
}