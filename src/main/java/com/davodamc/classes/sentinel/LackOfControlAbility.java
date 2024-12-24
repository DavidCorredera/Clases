package com.davodamc.classes.sentinel;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LackOfControlAbility {

    private static final String ABILITY_NAME = "descontrol";
    public static final Map<UUID, String> controlledPlayers = new HashMap<>(); // Jugadores controlados

    public static Map<UUID, String> getControlledPlayers() {
        return controlledPlayers;
    }

    public static void lackOfControlAbility(Player p, Integer cooldown, int lackOfControlRadius, int timeWithoutAbilities) {

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Centinela", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location playerLocation = p.getLocation();
        Location lackOfControlLocation = playerLocation.clone().add(0.0, 5.0, 0.0);

        Main.getInstance().getParticlesManager().spawnParticleSphere(p, lackOfControlLocation, Particle.REDSTONE, 170, 0, 255, 1, 100, 30);

        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 0.8f, 1.0f);

        new BukkitRunnable() {
            @Override
            public void run() {

                Main.getInstance().getParticlesManager().spawnClosingParticleSphere(p, playerLocation, Particle.REDSTONE, lackOfControlRadius, 100, 60);
                Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.0f);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.0f);

                        // Obtención de las entidades de forma síncrona
                        Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(playerLocation, lackOfControlRadius, lackOfControlRadius, lackOfControlRadius);
                        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                        // Ejecución asincrónica para filtrar y procesar las entidades
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                List<Entity> filteredEntities = new ArrayList<>();
                                for (Entity entity : nearbyEntities) {
                                    if (entity instanceof Player nearbyPlayer) {
                                        if (nearbyPlayer == p) continue;
                                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                                        if (Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                                        if (Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                                        filteredEntities.add(nearbyPlayer);
                                    }
                                }

                                // Volver al hilo principal para interactuar con Bukkit
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        List<String> affectedPlayerNames = new ArrayList<>();

                                        for (Entity entity : filteredEntities) {
                                            if (entity instanceof Player nearbyPlayer) {
                                                Location playersLocation = nearbyPlayer.getLocation();
                                                double distanceToPlayer = playerLocation.distance(playersLocation);
                                                if (distanceToPlayer <= lackOfControlRadius) {
                                                    if (controlledPlayers.get(nearbyPlayer.getUniqueId()) != null) continue;

                                                    Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.FLAME, 10);
                                                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, timeWithoutAbilities, 0));
                                                    nearbyPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has sido controlado por &b" + p.getName() +
                                                            "&f, no podrás utilizar habilidades hasta dentro de &b" + timeWithoutAbilities / 20 + " &fsegundos."));
                                                    affectedPlayerNames.add(nearbyPlayer.getName());
                                                    UUID nearbyPlayerUUID = nearbyPlayer.getUniqueId();
                                                    controlledPlayers.put(nearbyPlayerUUID, p.getName());

                                                    new BukkitRunnable() {
                                                        @Override
                                                        public void run() {
                                                            nearbyPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Ya no estás controlado, puedes volver a utilizar habilidades!"));
                                                            controlledPlayers.remove(nearbyPlayerUUID);
                                                        }
                                                    }.runTaskLater(Main.getInstance(), timeWithoutAbilities);
                                                }
                                            }
                                        }

                                        if (!affectedPlayerNames.isEmpty()) {
                                            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&fHas controlado a: &b" + String.join(", ", affectedPlayerNames) + "&f."));
                                        } else {
                                            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No has controlado a ningún jugador!"));
                                        }
                                    }
                                }.runTask(Main.getInstance());
                            }
                        }.runTaskAsynchronously(Main.getInstance());
                    }
                }.runTaskLater(Main.getInstance(), 60L);

            }
        }.runTaskLater(Main.getInstance(), 30L);
    }
}