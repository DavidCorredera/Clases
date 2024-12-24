package com.davodamc.listeners;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SelectClass implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;

        String inventoryName = event.getView().getTitle();
        ItemStack clickedItem = event.getCurrentItem();

        if (!inventoryName.equals(ChatAPI.cc("&bSeleccionar clase"))) return;

        event.setCancelled(true);

        if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.PLAYER_HEAD
                || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        Main.getInstance().getSelectClassManager().handleItemClick(p, clickedItem);
    }
}