package com.davodamc.classes.mage;

import com.davodamc.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ShadowCamouflageAbility {

    private static final String ABILITY_NAME = "camuflaje sombrío";

    public static void shadowCamouflageAbility(Player p, Integer cooldown, int duration, int blindnessTier, float speedWalk) {

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Mago", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, blindnessTier));

        p.setWalkSpeed(speedWalk);

        for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
            if (!loopPlayer.hasPermission("kingscraft.staff") && !Main.getInstance().getAbilitiesManager().isClanAlly(p, loopPlayer)) {
                loopPlayer.hidePlayer(Main.getInstance(), p);
            }
        }

        BukkitRunnable haloCamouflageTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createHalo(p, 10, 10, 10, 55, 0.75);
            }
        }; haloCamouflageTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                p.setWalkSpeed(0.2f);
                // Mostrar al jugador a los que no tienen permiso
                for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                    if (!loopPlayer.hasPermission("kingscraft.staff") && !Main.getInstance().getAbilitiesManager().isClanAlly(p, loopPlayer)) {
                        loopPlayer.showPlayer(Main.getInstance(), p);
                    }
                }
                // Cancelar la tarea del halo
                haloCamouflageTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);
    }
}
