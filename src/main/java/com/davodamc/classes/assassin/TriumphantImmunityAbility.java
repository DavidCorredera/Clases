package com.davodamc.classes.assassin;

import com.davodamc.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriumphantImmunityAbility {

    private static final String ABILITY_NAME = "inmunidad triunfal";
    public static final Map<UUID, Long> activeAuraPlayers = new HashMap<>(); // Jugadores con aureola activa
    public static final Map<UUID, Long> invinciblePlayers = new HashMap<>(); // Jugadores con inmortalidad activa
    public static final Map<UUID, Integer> playerInvincibleTime = new HashMap<>(); // Tiempo de inmortalidad para cada jugador

    public static void triumphantImmunityAbility(Player p, Integer cooldown, int auraTime, int invincibleTime) {

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Asesino", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        UUID playerId = p.getUniqueId();

        // Almacenar tiempo de aureola e inmortalidad para el jugador
        activeAuraPlayers.put(playerId, System.currentTimeMillis() + auraTime * 1000L);
        playerInvincibleTime.put(playerId, invincibleTime);

        // Crear la aureola y configurar el cooldown
        BukkitRunnable haloImmunityTask = new BukkitRunnable() {
            @Override
            public void run() {
                Main.getInstance().getParticlesManager().createHalo(p, 255, 255, 0, 55, 0.75);
            }
        }; haloImmunityTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

        // Remover la aureola al finalizar el tiempo de aura
        new BukkitRunnable() {
            @Override
            public void run() {
                activeAuraPlayers.remove(playerId);
                haloImmunityTask.cancel();
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), auraTime * 20L);
    }
}
