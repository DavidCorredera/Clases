package com.davodamc.classes.healer;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HealingTotemAbility {

    private static final String ABILITY_NAME = "tótem curativo";
    public static final Map<UUID, List<Block>> totemBlocks = new ConcurrentHashMap<>();

    public static void healingTotemAbility(Player p, Integer cooldown, int radius, int quantityParticles, int duration, double heartsHeal, int timeBetweenHeal) {

        Block targetBlock = p.getTargetBlock(null, 5);

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Curandero", targetBlock, null)) return;

        Location playerLocation = p.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();

        double distance = playerLocation.distance(targetBlockLocation);

        if (distance > 5) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡El " + ABILITY_NAME + " solo puedes colocarlo a 5 bloques de distancia!"));
            return;
        }

        if(targetBlock.getLocation().getBlockY() > p.getLocation().getY()) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado alto para generar " + ABILITY_NAME + "!"));
            return;
        }

        if(targetBlock.getType() == Material.AIR) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes colocar el " + ABILITY_NAME + " &cen el aire!"));
            return;
        }

        Location locationTemp = targetBlockLocation.clone().add(0.0, 2.0, 0.0);
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location loopLocation = locationTemp.clone().add(x, y, z);
                    if (loopLocation.getY() > targetBlockLocation.getY()) {
                        if (loopLocation.getBlock().getType() != Material.AIR) {
                            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&cNo hay suficiente espacio para colocar el " + ABILITY_NAME + "&c."));
                            return;
                        }
                    }
                }
            }
        }

        // COMPROBAR SI SOBRE EL TARGET BLOCK HAY UN JUGADOR
        double targetY = targetBlockLocation.getY() + 1;

        for (Entity entity : targetBlock.getWorld().getNearbyEntities(targetBlockLocation, radius, 1, radius)) {
            if (entity instanceof Player nearbyPlayer) {
                // Verificar si el jugador está exactamente sobre el bloque
                if (nearbyPlayer.getLocation().getY() >= targetY && nearbyPlayer.getLocation().getY() < targetY + 1) {
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes colocar el " + ABILITY_NAME + " sobre un jugador!"));
                    return;
                }
            }
        }

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ITEM_TOTEM_USE, 0.4f, 0.4f);
        // GENERAR TOTEM
        UUID playerUUID = p.getUniqueId();
        removeTotem(playerUUID);
        List<Block> totemBlocksList = totemBlocks.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        createTotem(targetBlockLocation, totemBlocksList);

        // PARTÍCULAS
        Main.getInstance().getParticlesManager().spawnParticleSphere(p, targetBlockLocation.clone().add(0.0, 0.5, 0.0),
                Particle.REDSTONE, 0, 255, 0, radius, 80, duration);

        BukkitRunnable healTotemTask = new BukkitRunnable() {
            @Override
            public void run() {

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(targetBlockLocation, radius, radius, radius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playersLocation = nearbyPlayer.getLocation();
                        double distanceToPlayer = targetBlockLocation.distance(playersLocation);
                        if (distanceToPlayer <= radius) {
                            if(nearbyPlayer != p) if (!Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                            nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.8f);
                            Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.VILLAGER_HAPPY, quantityParticles);

                            double currentHealth = nearbyPlayer.getHealth();
                            double maxHealth = nearbyPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                            // Calcular la nueva salud sin exceder el máximo
                            double newHealth = Math.min(currentHealth + heartsHeal, maxHealth);

                            // Establecer la nueva salud del jugador
                            nearbyPlayer.setHealth(newHealth);
                        }
                    }
                }
                Main.getInstance().getParticlesManager().showParticles(targetBlockLocation.clone().add(0.5, 4.0, 0.5), Particle.FLAME, 20);
                Main.getInstance().getParticlesManager().showParticles(targetBlockLocation.clone().add(0.5, 1.0, 0.5), Particle.HEART, 30);
            }
        }; healTotemTask.runTaskTimer(Main.getInstance(), 0L, timeBetweenHeal);

        new BukkitRunnable() {
            @Override
            public void run() {
                healTotemTask.cancel();
                removeTotem(playerUUID);
            }
        }.runTaskLater(Main.getInstance(), duration);

    }

    private static void createTotem(Location center, List<Block> totemBlocksList) {
        // COLUMNA DEL MEDIO
        addBlock(center.clone().add(0, 1, 0), totemBlocksList, Material.LIME_WOOL, null, false);
        addBlock(center.clone().add(0, 2, 0), totemBlocksList, Material.LIME_WOOL, null, false);
        addBlock(center.clone().add(0, 3, 0), totemBlocksList, Material.NETHERRACK, null, false);
        addBlock(center.clone().add(0, 4, 0), totemBlocksList, Material.FIRE, null, false);

        // ESCALERAS BOCA ABAJO (mirando hacia fuera)
        addBlock(center.clone().add(1, 3, 0), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.EAST, true);
        addBlock(center.clone().add(0, 3, 1), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.SOUTH, true);
        addBlock(center.clone().add(-1, 3, 0), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.WEST, true);
        addBlock(center.clone().add(0, 3, -1), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.NORTH, true);

        // BARAS DE IRON
        addBlock(center.clone().add(1, 4, 0), totemBlocksList, Material.IRON_BARS, null, false);
        addBlock(center.clone().add(0, 4, 1), totemBlocksList, Material.IRON_BARS, null, false);
        addBlock(center.clone().add(-1, 4, 0), totemBlocksList, Material.IRON_BARS, null, false);
        addBlock(center.clone().add(0, 4, -1), totemBlocksList, Material.IRON_BARS, null, false);

        // ESCALERAS BOCA ARRIBA (mirando hacia fuera)
        addBlock(center.clone().add(1, 5, 0), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.EAST, false);
        addBlock(center.clone().add(0, 5, 1), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.SOUTH, false);
        addBlock(center.clone().add(-1, 5, 0), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.WEST, false);
        addBlock(center.clone().add(0, 5, -1), totemBlocksList, Material.QUARTZ_STAIRS, BlockFace.NORTH, false);
    }

    private static void addBlock(Location loc, List<Block> blocks, Material material, BlockFace facing, boolean upsideDown) {
        Block block = loc.getBlock();
        if (block.getType() == Material.AIR) {
            block.setType(material);
            if (material == Material.QUARTZ_STAIRS) {
                Stairs stairs = (Stairs) block.getBlockData();
                stairs.setFacing(facing);
                stairs.setHalf(upsideDown ? Stairs.Half.TOP : Stairs.Half.BOTTOM);
                block.setBlockData(stairs); // Aplica la orientación y posición
            }
            blocks.add(block);
        }
    }

    private static void removeTotem(UUID playerId) {
        List<Block> totemBlocksList = totemBlocks.remove(playerId);
        if (totemBlocksList != null) {
            totemBlocksList.forEach(block -> block.setType(Material.AIR));
        }
    }
}
