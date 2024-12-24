package com.davodamc.classes.warrior;

import com.davodamc.Main;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AngerAbility implements Listener {

    private static final String ABILITY_NAME = "ira";
    public static final Map<UUID, Double> angerPlayers = new ConcurrentHashMap<>();

    public static void angerAbility(Player p, Integer cooldown, int quantityParticles, double damageAnger, int duration) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        BukkitRunnable haloAngerTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createHalo(p, 100, 11, 11, 55, 0.75);
            }
        }; haloAngerTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

        angerPlayers.put(p.getUniqueId(), damageAnger);

        new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.SUSPENDED_DEPTH, quantityParticles);
                p.playSound(p.getLocation(), Sound.ENTITY_RABBIT_HURT, 0.5f, 1.0f);

                angerPlayers.remove(p.getUniqueId());

                // Cancelar la tarea del halo
                haloAngerTask.cancel();
            }
        }.runTaskLater(Main.getInstance(), duration);

    }
}