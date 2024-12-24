package com.davodamc.managers;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SelectClassManager {
    public void handleItemClick(Player p, ItemStack clickedItem) {

        p.closeInventory();

        if (clickedItem.getType() == Material.BARRIER) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        String className = displayName.replace("Clase: ", "");

        try {

            String playerClass = Main.getInstance().getMySQLManager().getPlayerClass(p.getName());

            if (playerClass == null) {playerClass = "Ninguna";}

            if(!playerClass.equals("Ninguna")) {
                p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ya formas parte de una clase! Solo puedes cambiar si tienes un pergamino del olvido."));
                return;
            }

            Main.getInstance().getMySQLManager().insertPlayerClass(p.getName(), p.getUniqueId().toString(), className);

        } catch (Exception e) {
            e.printStackTrace();
        }

        p.sendMessage("");
        p.sendMessage(ChatAPI.cc("&9&lSURVIVALOP &8- &bClases"));
        p.sendMessage("");

        p.sendMessage(ChatAPI.cc("&fClase seleccionada: &b" + className));
        p.sendMessage("");
        p.sendMessage(ChatAPI.cc(classLore(className)));
        p.sendMessage(ChatAPI.cc(""));

        Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.CLOUD, 30);
    }

    private String classLore(String className) {
        return switch (className) {
            case "Mago" -> "&fEsta clase se caracteriza por &bconjuros &fy combate a &blarga&f distancia.";
            case "Asesino" -> "&fEsta clase se caracteriza por el combate &bcuerpo a cuerpo &fy habilidades de &bdaño&f.";
            case "Guerrero" -> "&fEsta clase se caracteriza por el combate &bcuerpo a cuerpo&f pero con &bmovilidad&f.";
            case "Curandero" -> "&fEsta clase se caracteriza por &bapoyar &fa tus compañeros y &bcurar&f.";
            case "Centinela" -> "&fEsta clase caracteriza por &bcontrolar &fzonas y &bproteger&f.";
            default -> "";
        };
    }
}