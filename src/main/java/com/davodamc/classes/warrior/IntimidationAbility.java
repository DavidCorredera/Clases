package com.davodamc.classes.warrior;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IntimidationAbility {

    private static final String ABILITY_NAME = "intimidación";

    public static void intimidationAbility(Player p, Integer cooldown, int slownessRadius, int slownessTier, int slownessTime, int timeToExplode, int radius) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();
        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location playerLoc = p.getLocation().clone();

        p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1.0f, 1.0f);

        particlesManager.spawnParticleSphere(p, playerLoc, Particle.VILLAGER_ANGRY, 0, 0, 0, radius, 20, timeToExplode);

        abilitiesManager.launchColoredFirework(p, timeToExplode);

        new BukkitRunnable() {
            @Override
            public void run() {

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(playerLoc, slownessRadius, slownessRadius, slownessRadius);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                List<String> affectedPlayerNames = new ArrayList<>();
                List<String> dodgePlayerNames = new ArrayList<>();

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playersLocation = nearbyPlayer.getLocation();
                        if(nearbyPlayer == p) continue;
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(abilitiesManager.isNPCOrStaff(nearbyPlayer)) continue;
                        if(abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;
                        double distanceToPlayer = playerLoc.distance(playersLocation);
                        if (distanceToPlayer <= slownessRadius) {
                            Vector directionToTarget = p.getLocation().toVector().subtract(playersLocation.toVector()).normalize();
                            Vector nearbyPlayerDirection = nearbyPlayer.getLocation().getDirection().normalize();
                            if (nearbyPlayerDirection.dot(directionToTarget) >= 0.97) { // ESTÁ MIRANDO AL JUGADOR
                                nearbyPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Has esquivado la intimidación porque estabas mirando a &b" + p.getName() + "&f!"));
                                dodgePlayerNames.add(nearbyPlayer.getName());
                                continue;
                            }
                            nearbyPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡No has esquivado la intimidación de &b" + p.getName() + " &fporque no estabas mirándole!"));
                            affectedPlayerNames.add(nearbyPlayer.getName());
                            p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
                            particlesManager.showParticles(nearbyPlayer.getLocation(), Particle.SMOKE_LARGE, 8);
                            if(nearbyPlayer.hasPotionEffect(PotionEffectType.SLOW)) continue;
                            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slownessTime, slownessTier));
                        }
                    }
                }
                if (!affectedPlayerNames.isEmpty()) {
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&fHas intimidado a: &b" + String.join(", ", affectedPlayerNames) + "&f."));
                } else {
                    if (dodgePlayerNames.isEmpty()) {
                        p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No has intimidado a ningún jugador y nadie te la ha esquivado!"));
                    }
                }

                if (!dodgePlayerNames.isEmpty()) {
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&fTe ha esquivado la intimidación: &b" + String.join(", ", dodgePlayerNames) + "&f."));
                }
            }
        }.runTaskLater(Main.getInstance(), timeToExplode);

    }
}
