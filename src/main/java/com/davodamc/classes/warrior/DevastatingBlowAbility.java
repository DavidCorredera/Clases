package com.davodamc.classes.warrior;

import com.davodamc.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DevastatingBlowAbility implements Listener {

    private static final String ABILITY_NAME = "golpe devastador";
    public static final Map<UUID, Integer> activePlayers = new HashMap<>(); // Jugadores con la habilidad activada
    public static final Map<UUID, Integer> activePlayersDamage = new HashMap<>(); // Jugadores con la habilidad activada

    public static void devastatingBlowAbility(Player p, Integer cooldown, int radius, int damage) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        activePlayers.put(p.getUniqueId(), radius);
        activePlayersDamage.put(p.getUniqueId(), damage);

        // Empujar hacia arriba
        Vector upwardsPush = new Vector(0, 1.8, 0);
        p.setVelocity(upwardsPush);

        // Empujar hacia adelante
        Vector forwardsPush = p.getLocation().getDirection().multiply(1.8);
        p.setVelocity(p.getVelocity().add(forwardsPush));

        new BukkitRunnable() {
            @Override
            public void run() {
                // Empujar hacia abajo con más fuerza
                Vector downwardsPush = new Vector(0, -3, 0); // La fuerza hacia abajo
                p.setVelocity(p.getVelocity().add(downwardsPush));
            }
        }.runTaskLater(Main.getInstance(), 20L); // 20 ticks = 1 segundo

    }
}