package com.davodamc.listeners;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RemoveClass implements Listener {

    @EventHandler
    public void onUseScrollOfOblivion(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        if (p.getInventory().getItemInMainHand().getItemMeta() == null) return;

        if (p.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("§9Pergamino del olvido")) {
            try {
                String playerClass = Main.getInstance().getMySQLManager().getPlayerClass(p.getName());

                if (playerClass == null) {
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No perteneces a ninguna clase y por ello no puedes utilizar el pergamino del olvido! Selecciona tu favorita en /clases."));
                    return;
                }

                p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "¡Has olvidado tu clase, ahora puedes volver a aprender una nueva con &b/clases&f!"));

                // REMOVE 1 FROM HAND
                if (p.getInventory().getItemInHand().getAmount() == 1) {
                    p.getInventory().setItemInHand(new ItemStack(Material.AIR));
                } else {
                    p.getInventory().getItemInHand().setAmount(p.getInventory().getItemInHand().getAmount() - 1);}

                Main.getInstance().getMySQLManager().deletePlayerClass(p.getName(), p.getUniqueId().toString(), playerClass);

                Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.SCRAPE, 40);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
