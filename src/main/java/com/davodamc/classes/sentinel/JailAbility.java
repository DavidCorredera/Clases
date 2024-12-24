package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JailAbility {

    private static final String ABILITY_NAME = "condena";
    public static final Map<UUID, List<Block>> jailedPlayers = new ConcurrentHashMap<>();

    public static void jailAbility(Player p, Integer cooldown, int quantityParticles, int maxDistance, int abilityLevel, int jailTime) {

        Player targetPlayer = Main.getInstance().getAbilitiesManager().getTargetPlayer(p, 20);

        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(targetPlayer)) return;

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Centinela", null, targetPlayer)) return;

        if (targetPlayer == null || targetPlayer.getLocation().distance(p.getLocation()) > maxDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Debes estar mirando a un jugador dentro de " + maxDistance + " bloques!"));
            return;
        }

        Location twoBlocksBelow = targetPlayer.getLocation().clone().subtract(0, 2, 0);
        if (twoBlocksBelow.getBlock().getType() == Material.AIR) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes encarcelar a alguien en el aire!"));
            return;
        }

        if(Main.getInstance().getAbilitiesManager().isClanAlly(p, targetPlayer)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes encarcelar a un aliado de tu clan!"));
            return;
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
        Main.getInstance().getParticlesManager().showParticles(targetPlayer.getLocation(), Particle.DRIPPING_OBSIDIAN_TEAR, quantityParticles);

        removeExistingJail(targetPlayer.getUniqueId());
        List<Block> jailBlocks = jailedPlayers.computeIfAbsent(targetPlayer.getUniqueId(), k -> new ArrayList<>());

        Location tpLocation = targetPlayer.getLocation().clone().add(0.5, 0, 0.5);
        buildJail(tpLocation, jailBlocks, abilityLevel);
        targetPlayer.teleport(tpLocation);

        new BukkitRunnable() {
            @Override
            public void run() {
                removeExistingJail(targetPlayer.getUniqueId());
            }
        }.runTaskLater(Main.getInstance(), jailTime);
    }

    private static void buildJail(Location center, List<Block> jailBlocks, int levelAbility) {
        // SECCIÓN 1 (TECHO)
        addBlock(center.clone().add(0, 2, 0), jailBlocks, Material.STONE_BRICKS);
        addBlock(center.clone().add(1, 2, 0), jailBlocks, Material.STONE_BRICKS);
        addBlock(center.clone().add(0, 2, 1), jailBlocks, Material.STONE_BRICKS);
        addBlock(center.clone().add(-1, 2, 0), jailBlocks, Material.STONE_BRICKS);
        addBlock(center.clone().add(0, 2, -1), jailBlocks, Material.STONE_BRICKS);

        // SECCIÓN 2 (MEDIO)
        addBlock(center.clone().add(1, 1, 0), jailBlocks, Material.IRON_BARS);
        addBlock(center.clone().add(-1, 1, 0), jailBlocks, Material.IRON_BARS);
        if(levelAbility >= 3) {
            addBlock(center.clone().add(0, 1, 1), jailBlocks, Material.IRON_BARS);
            addBlock(center.clone().add(0, 1, -1), jailBlocks, Material.IRON_BARS);
        }
        if(levelAbility == 2) {
            addBlock(center.clone().add(0, 1, 1), jailBlocks, Material.IRON_BARS);
            addBlock(center.clone().add(0, 1, -1), jailBlocks, Material.STONE_BRICKS);
        }
        if(levelAbility == 1) {
            addBlock(center.clone().add(0, 1, 1), jailBlocks, Material.STONE_BRICKS);
            addBlock(center.clone().add(0, 1, -1), jailBlocks, Material.STONE_BRICKS);
        }

        // SECCIÓN 3 (ABAJO)
        addBlock(center.clone().add(1, 0, 0), jailBlocks, Material.STONE_BRICKS);
        addBlock(center.clone().add(-1, 0, 0), jailBlocks, Material.STONE_BRICKS);
        if(levelAbility <= 3) {
            addBlock(center.clone().add(0, 0, -1), jailBlocks, Material.STONE_BRICKS);
            addBlock(center.clone().add(0, 0, 1), jailBlocks, Material.STONE_BRICKS);
        }
        if(levelAbility == 4) {
            addBlock(center.clone().add(0, 0, -1), jailBlocks, Material.IRON_BARS);
            addBlock(center.clone().add(0, 0, 1), jailBlocks, Material.STONE_BRICKS);
        }
        if(levelAbility == 5) {
            addBlock(center.clone().add(0, 0, -1), jailBlocks, Material.IRON_BARS);
            addBlock(center.clone().add(0, 0, 1), jailBlocks, Material.IRON_BARS);
        }
    }

    private static void addBlock(Location loc, List<Block> blocks, Material material) {
        Block block = loc.getBlock();
        if (block.getType() == Material.AIR) {
            block.setType(material);
            blocks.add(block);
        }
    }

    private static void removeExistingJail(UUID playerId) {
        List<Block> jailBlocks = jailedPlayers.remove(playerId);
        if (jailBlocks != null) {
            jailBlocks.forEach(block -> block.setType(Material.AIR));
        }
    }
}