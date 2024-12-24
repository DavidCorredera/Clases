package com.davodamc.classes.assassin;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SprintAbility {

    private static final String ABILITY_NAME = "esprintar";

    public static void sprintAbility(Player p, Integer cooldown, int quantityParticles, float speedWalk, int runningTime) {

        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        p.setWalkSpeed(speedWalk);
        particlesManager.showParticles(p.getLocation(), Particle.CLOUD, quantityParticles);

        new BukkitRunnable() {
            @Override
            public void run() {
                p.setWalkSpeed(0.2f);
                particlesManager.showParticles(p.getLocation(), Particle.FLAME, quantityParticles);
            }
        }.runTaskLater(Main.getInstance(), runningTime);
    }
}