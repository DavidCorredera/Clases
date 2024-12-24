package com.davodamc.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GlobalListeners implements Listener {

    // Lista de nombres base de habilidades
    private final List<String> abilityNames = Arrays.asList(
            "§eAguja de asesino",
            "§aCampamento",
            "§6Carga"
    );

    @EventHandler
    public void onBuildAbility(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.hasItemMeta()) {
            ItemMeta itemMeta = itemInHand.getItemMeta();
            String itemName = itemMeta.getDisplayName();

            // Verificar si el nombre del objeto contiene el nombre base de alguna habilidad
            for (String abilityName : abilityNames) {
                if (itemName.contains(abilityName)) {
                    event.setCancelled(true);
                    break; // Salir del bucle si se encuentra una coincidencia
                }
            }
        }
    }
}