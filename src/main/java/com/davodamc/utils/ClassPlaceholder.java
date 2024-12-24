package com.davodamc.utils;

import com.davodamc.managers.MySQLManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ClassPlaceholder extends PlaceholderExpansion {
    private final MySQLManager mySQLManager;

    public ClassPlaceholder(MySQLManager mySQLManager) {
        this.mySQLManager = mySQLManager;
    }

    @Override
    public String getIdentifier() {
        return "sop";
    }

    @Override
    public String getAuthor() {
        return "Davoda";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; // Mantener activo aunque se recargue el plugin
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Comprobamos si el placeholder es "clase"
        if (params.equalsIgnoreCase("clase")) {
            try {
                // Obtenemos la clase del jugador desde MySQL
                String playerClass = mySQLManager.getPlayerClass(player.getName());
                return playerClass != null ? playerClass : "Ninguna";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        if (params.equalsIgnoreCase("logoclase")) {
            try {
                // Obtenemos la clase del jugador desde MySQL
                String playerClass = mySQLManager.getPlayerClass(player.getName());
                if (playerClass == null) return "";
                switch (playerClass) {
                    case "Mago" -> {return "§8(§5⚡§8)";}
                    case "Asesino" -> {return "§8(§e⚔§8)";}
                    case "Guerrero" -> {return "§8(§b\uD83D\uDDE1§8)";}
                    case "Curandero" -> {return "§8(§c♥§8)";}
                    case "Centinela" -> {return "§8(§7⚖§8)";}
                    default -> {return "";}
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }

        return null; // Placeholder no reconocido
    }

}
