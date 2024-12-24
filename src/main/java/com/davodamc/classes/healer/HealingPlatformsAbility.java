package com.davodamc.classes.healer;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HealingPlatformsAbility {

    private static final String ABILITY_NAME = "plataformas curativas";
    public static HashMap<UUID, HashMap<Location, Material>> platformsBlocks = new HashMap<>();
    public static final Map<UUID, List<Block>> carpetBlocks = new HashMap<>();

    public static void healingPlatformsAbility(Player p, Integer cooldown, int platformsRadius, Material platformsMaterial, int platformsTime, double heartsHeal) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Curandero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location centerLocation = p.getLocation().clone();

        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, 1.0f);

        UUID playerUUID = p.getUniqueId();
        HashMap<Location, Material> placedBlocks = new HashMap<>();


        for (int x = -platformsRadius; x <= platformsRadius; x++) {
            for (int z = -platformsRadius; z <= platformsRadius; z++) {
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
                            if (nearbyBlock.getType() == platformsMaterial) {
                                neighborCount++;
                            }
                        }
                    }

                    // Coloca el bloque solo si no tiene vecinos del mismo material
                    if (neighborCount == 0) {
                        if(Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(blockLocation)) continue; // QUE NO SE PONGAN BLOQUES EN ZONAS PROTEGIDAS

                        List<Block> playerCarpetBlocks = carpetBlocks.getOrDefault(playerUUID, new ArrayList<>());
                        playerCarpetBlocks.add(block);
                        carpetBlocks.put(playerUUID, playerCarpetBlocks);

                        placedBlocks.put(blockLocation, block.getType());
                        block.setType(platformsMaterial, false);
                        Main.getInstance().getParticlesManager().showParticles(blockLocation, Particle.VILLAGER_HAPPY, 6);
                    }
                }
            }
        }

        platformsBlocks.put(playerUUID, placedBlocks);

        // Define el radio de curación (puedes ajustar el valor)
        int healingRadius = platformsRadius + 10;
        // Programa la tarea para curar jugadores que estén sobre la plataforma dentro del radio
        BukkitRunnable healingTask = new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<Location, Material> blocksToCheck = platformsBlocks.get(playerUUID);

                if (blocksToCheck == null || blocksToCheck.isEmpty()) {
                    this.cancel(); // Cancela la tarea si no hay bloques para verificar
                    return;
                }

                // Recorre entidades cercanas
                for (Entity nearbyEntity : centerLocation.getWorld().getNearbyEntities(centerLocation, healingRadius, healingRadius, healingRadius)) {
                    if (nearbyEntity instanceof Player nearbyPlayer) {
                        Location playerLocation = nearbyPlayer.getLocation();
                        Location blockLocation = playerLocation.getBlock().getLocation();

                        // Verifica si el jugador está sobre un bloque de la plataforma
                        boolean isOnPlatform = blocksToCheck.keySet().stream().anyMatch(loc ->
                                loc.getBlockX() == blockLocation.getBlockX() &&
                                        loc.getBlockZ() == blockLocation.getBlockZ() &&
                                        playerLocation.getY() <= loc.getY() + 1 // Permite un rango de 1 bloque sobre la plataforma
                        );

                        if (isOnPlatform) {
                            if(p != nearbyPlayer) {if (!abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;}
                            
                            // Cura al jugador con la cantidad especificada de corazones
                            double newHealth = Math.min(nearbyPlayer.getHealth() + (heartsHeal * 2), nearbyPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                            nearbyPlayer.setHealth(newHealth);

                            // Muestra partículas en la posición del jugador
                            nearbyPlayer.getWorld().spawnParticle(Particle.HEART, nearbyPlayer.getLocation().add(0, 1, 0), 3);

                            nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.2f, 1.0f);
                        }
                    }
                }

            }
        };

        healingTask.runTaskTimer(Main.getInstance(), 0L, 20L);


        new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<Location, Material> blocksToRestore = platformsBlocks.get(playerUUID);
                if (blocksToRestore != null) {
                    for (Map.Entry<Location, Material> entry : blocksToRestore.entrySet()) {
                        Location loc = entry.getKey();
                        Material originalMaterial = entry.getValue();
                        loc.getBlock().setType(originalMaterial); // Restaura el tipo original
                        healingTask.cancel();
                    }
                    platformsBlocks.remove(playerUUID); // Limpia el mapa de este jugador
                    carpetBlocks.remove(playerUUID);
                }
            }
        }.runTaskLater(Main.getInstance(), platformsTime);

    }
}