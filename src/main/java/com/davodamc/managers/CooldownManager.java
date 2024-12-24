package com.davodamc.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private final Map<String, Map<String, Long>> abilitiesCooldowns;

    public CooldownManager() {abilitiesCooldowns = new HashMap<>();}

    public void setCooldown(Player player, String ability, int seconds) {
        long cooldownExpiration = System.currentTimeMillis() + (seconds * 1000L);

        abilitiesCooldowns.computeIfAbsent(player.getName(), k -> new HashMap<>()).put(ability, cooldownExpiration);
    }

    public long getCooldownTimeRemaining(Player player, String ability) {
        if (abilitiesCooldowns.containsKey(player.getName()) && abilitiesCooldowns.get(player.getName()).containsKey(ability)) {
            long currentTime = System.currentTimeMillis();
            long cooldownExpiration = abilitiesCooldowns.get(player.getName()).get(ability);

            if (currentTime < cooldownExpiration) {
                return (cooldownExpiration - currentTime) / 1000; // Convertir a segundos
            }
        }

        return 0;
    }

    public boolean isOnCooldown(Player player, String ability) {
        return getCooldownTimeRemaining(player, ability) > 0;
    }
}