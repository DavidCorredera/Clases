package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SharpThornsAbility {

    private static final String ABILITY_NAME = "espinas punzantes";
    public static HashMap<UUID, HashMap<Location, Material>> sharpBlocks = new HashMap<>();
    public static final Map<UUID, List<Block>> thornsBlocks = new HashMap<>();

    public static void sharpThornsAbility(Player p, Integer cooldown, int sharpRadius, int sharpTime, double damage) {

        if(!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Centinela", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location centerLocation = p.getLocation().clone();

        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, 1.0f);

        UUID playerUUID = p.getUniqueId();
        HashMap<Location, Material> placedBlocks = new HashMap<>();


        for (int x = -sharpRadius; x <= sharpRadius; x++) {
            for (int z = -sharpRadius; z <= sharpRadius; z++) {
                Location blockLocation = centerLocation.clone().add(x, 0, z);
                Block block = blockLocation.getBlock();

                if (block.getType() != Material.AIR) continue;
                if(block.getRelative(BlockFace.UP).getType() != Material.AIR) continue;
                Block blockBelow = block.getRelative(BlockFace.DOWN);
                Material blockMaterialBelow = blockBelow.getType();
                if(blockMaterialBelow == Material.AIR) continue;
                if(blockMaterialBelow.name().endsWith("_CARPET")) continue;
                if(blockMaterialBelow.name().endsWith("_BUSH")) continue;

                // Solo coloca un bloque aleatoriamente
                if (Math.random() > 0.7) { // Ajusta el valor para cambiar la densidad de bloques
                    int neighborCount = 0;

                    // Recorre bloques adyacentes en X y Z
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dz == 0) continue; // Ignora el bloque actual

                            Block nearbyBlock = blockLocation.clone().add(dx, 0, dz).getBlock();
                            if (nearbyBlock.getType() == Material.DEAD_BUSH) {
                                neighborCount++;
                            }
                        }
                    }

                    // Coloca el bloque solo si no tiene vecinos del mismo material
                    if (neighborCount == 0) {
                        if(Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(blockLocation)) continue; // QUE NO SE PONGAN BLOQUES EN ZONAS PROTEGIDAS

                        List<Block> sharpBlocksList = thornsBlocks.getOrDefault(playerUUID, new ArrayList<>());
                        sharpBlocksList.add(block);
                        thornsBlocks.put(playerUUID, sharpBlocksList);

                        placedBlocks.put(blockLocation, block.getType());
                        block.setType(Material.DEAD_BUSH);
                        Main.getInstance().getParticlesManager().showParticles(blockLocation, Particle.VILLAGER_HAPPY, 6);
                    }
                }
            }
        }

        sharpBlocks.put(playerUUID, placedBlocks);

        // Programa la tarea para curar jugadores que estén sobre la plataforma dentro del radio
        BukkitRunnable sharpTask = new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<Location, Material> blocksToCheck = sharpBlocks.get(playerUUID);

                if (blocksToCheck == null || blocksToCheck.isEmpty()) {
                    this.cancel(); // Cancela la tarea si no hay bloques para verificar
                    return;
                }

                // Recorre entidades cercanas
                for (Entity nearbyEntity : centerLocation.getWorld().getNearbyEntities(centerLocation, sharpRadius, sharpRadius, sharpRadius)) {
                    if (nearbyEntity instanceof Player nearbyPlayer) {
                        if (nearbyPlayer == p) continue;
                        if(Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                        Location playerLocation = nearbyPlayer.getLocation();
                        Location blockLocation = playerLocation.getBlock().getLocation();

                        // Verifica si el jugador está sobre un bloque de la plataforma
                        boolean isOnPlatform = blocksToCheck.keySet().stream().anyMatch(loc ->
                                loc.getBlockX() == blockLocation.getBlockX() &&
                                        loc.getBlockZ() == blockLocation.getBlockZ() &&
                                        playerLocation.getY() <= loc.getY() + 1 // Permite un rango de 1 bloque sobre la plataforma
                        );

                        if (isOnPlatform) {
                            nearbyPlayer.damage(damage, p);

                            // Muestra partículas en la posición del jugador
                            nearbyPlayer.getWorld().spawnParticle(Particle.SQUID_INK, nearbyPlayer.getLocation().add(0, 1, 0), 10);

                            nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 0.2f, 1.0f);
                        }
                    }
                }

            }
        };

        sharpTask.runTaskTimer(Main.getInstance(), 0L, 5L);


        new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<Location, Material> blocksToRestore = sharpBlocks.get(playerUUID);
                if (blocksToRestore != null) {
                    for (Map.Entry<Location, Material> entry : blocksToRestore.entrySet()) {
                        Location loc = entry.getKey();
                        Material originalMaterial = entry.getValue();
                        loc.getBlock().setType(originalMaterial); // Restaura el tipo original
                        sharpTask.cancel();
                    }
                    sharpBlocks.remove(playerUUID); // Limpia el mapa de este jugador
                    thornsBlocks.remove(playerUUID);
                }
            }
        }.runTaskLater(Main.getInstance(), sharpTime);

    }
}