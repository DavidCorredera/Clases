package com.davodamc.classes.warrior;

import com.davodamc.Main;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeSupportAbility {

    private static final String ABILITY_NAME = "refuerzo vital";

    public static void lifeSupportAbility(Player p, Integer cooldown, int healthBoostTier, int healthBoostTime) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.CHERRY_LEAVES, 10);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.2f, 0.2f);

        p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, healthBoostTime, healthBoostTier));

        BukkitRunnable haloLifeSupportTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createHalo(p, 255, 0, 0, 55, 0.75);
            }
        }; haloLifeSupportTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Cancelar la tarea del halo
                haloLifeSupportTask.cancel();
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), healthBoostTime);

    }
}
