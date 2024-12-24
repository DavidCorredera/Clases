package com.davodamc.menus;

import com.davodamc.utils.ChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectClassMenu {

    public void openClassSelectionMenu(Player p) {
        Inventory inventory = Bukkit.createInventory(null, 45, ChatAPI.cc("&bSeleccionar clase"));

        setItem(inventory, 12, "Mago", Material.BLAZE_ROD, "&f¿Te gustan los conjuros y la magia en general?\n&fEsta es tu clase perfecta, pero recuerda:\n&fUn buen mago, nunca revela sus trucos.\n&5\n&fClic para formar parte de los &bMagos&f.");
        setItem(inventory, 14, "Asesino", Material.IRON_SWORD, "&fLa sangre es lo tuyo por lo visto.\n&fSumérgete en la aventura siendo el que mejor mata.\n&fY recuerda que primero matas, luego preguntas.\n&5\n&fClic para formar parte de los &bAsesinos&f.");
        setItem(inventory, 16, "Guerrero", Material.STONE_AXE, "&f¿Eres un luchador nato? Que lucha por cualquier cosa\n&fHaz tus sueños realidad, y pelea por todo.\n&fNo te olvides de cubrir tus espaldas.\n&5\n&fClic para formar parte de los &bGuerreros&f.");
        setItem(inventory, 31, "Curandero", Material.SPLASH_POTION, "&f¿Te gusta ser el que salva el pellejo a tus amigos?\n&fSé bueno con tus compañeros y salva sus vidas.\n&fY considérate el salvaguarda de tus aliados\n&5\n&fClic para formar parte de los &bCuranderos&f.");
        setItem(inventory, 33, "Centinela", Material.OBSERVER, "&f¿Te gusta tener control de adversario?\n&f¡Ya basta de que siempre tus enemigos se queden a un toque\n&fy se escapen con la suya, enciérralos en prisiones y hazte con ellos!\n&5\n&fClic para formar parte de los &bCentinelas&f.");

        inventory.setItem(9, createPlayerHeadItem(p));
        setItem(inventory, 27, "&cCerrar el menú", Material.BARRIER, "&fClic para cerrar el menú de clases.");

        int panelSlot = 1;
        for(int i=0; i<=4; i++ ) {
            inventory.setItem(panelSlot, createSeparatorPanelItem());
            panelSlot += 9;
        }

        p.openInventory(inventory);
    }

    private void setItem(Inventory inventory, int slot, String className, Material material, String loreText) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatAPI.cc("&fClase: &b&n" + className));

        if (material == Material.BARRIER) {
            setBarrierItemMeta(meta);
        } else if (isClassMaterial(material)) {
            setClassItemMeta(meta, loreText);
        }

        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void setBarrierItemMeta(ItemMeta meta) {
        meta.setDisplayName(ChatAPI.cc("&cCerrar el menú"));
        meta.setLore(Collections.singletonList(ChatAPI.cc("&fClic para cerrar el menú de clases.")));
    }

    private boolean isClassMaterial(Material material) {
        return Arrays.asList(Material.BLAZE_ROD, Material.IRON_SWORD, Material.STONE_AXE, Material.SPLASH_POTION, Material.OBSERVER).contains(material);
    }

    private void setClassItemMeta(ItemMeta meta, String loreText) {
        String[] loreLines = loreText.split("\n");
        List<String> loreList = ChatAPI.ccList(Arrays.asList(loreLines));
        meta.setLore(loreList);
    }

    private ItemStack createPlayerHeadItem(Player p) {
        ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();

        String loreTextPlayerHead = ChatAPI.cc("&e&l⚠ &c&lUNA VEZ SELECCIONADA LA CLASE, NO PODRÁS CAMBIARLA &e&l⚠\n&6\n&f¡Solo con el pergamino del olvido! &7-> &bwww.kingscraft.shop");
        String[] loreLinesPlayerHead = loreTextPlayerHead.split("\n");
        List<String> loreListPlayerHead = Arrays.asList(loreLinesPlayerHead);

        skullMeta.setDisplayName(ChatAPI.cc("&f¿Qué clase eliges? &b" + p.getName()));
        skullMeta.setLore(loreListPlayerHead);
        skullMeta.setOwningPlayer(p);

        headItem.setItemMeta(skullMeta);

        return headItem;
    }

    private ItemStack createSeparatorPanelItem() {
        ItemStack panelItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta panelMeta = panelItem.getItemMeta();

        panelMeta.setDisplayName(ChatAPI.cc("&b"));
        panelItem.setItemMeta(panelMeta);

        return panelItem;
    }
}
