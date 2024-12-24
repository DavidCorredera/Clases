package com.davodamc.classes;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PassivesListener implements Listener {

    private static final Map<UUID, Double> divineShieldPlayers = new ConcurrentHashMap<>();

    // PASIVA ASESINO
    @EventHandler
    public void onDeathAssassinPassive(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        Player attacker = e.getEntity().getKiller();
        if (!Main.getInstance().getAbilitiesManager().playerIsInClass(attacker, "Asesino")) return;
        LivingEntity victim = e.getEntity();

        if (victim instanceof Player) {
            healAssassin(attacker, "¡Te has curado &b3 corazones &fgracias a tu pasiva de asesino al matar un jugador!");
        } else if (victim.getType() != EntityType.PLAYER) {
            if (Main.getInstance().getAbilitiesManager().randomProbability(2.5)) {
                healAssassin(attacker, "¡Te has curado &b3 corazones &fgracias a tu pasiva de asesino al matar un mob!");
            }
        }
    }

    private void healAssassin(Player p, String message) {
        p.sendMessage(ChatAPI.cc(ChatAPI.prefix + message));
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.3f, 1.0f);
        p.setHealth(Math.min(p.getHealth() + 6.0, p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    // PASIVA MAGO
    @EventHandler
    public void onEntityDamageMagePassive(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (e.getEntity() instanceof Player victim) {
            if (!Main.getInstance().getAbilitiesManager().playerIsInClass(victim, "Mago")) return;
            if (victim.getHealth() >= 2.0) return;
            if (victim.hasPotionEffect(PotionEffectType.JUMP)) return;
            victim.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Se ha activado tu pasiva de mago, tienes salto durante &b3 &fsegundos!"));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 160, 7));
            victim.playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.3f, 1.0f);
        }
    }

    // PASIVA CENTINELA
    @EventHandler
    public void onEntityDamageSentinelPassive(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (e.getEntity() instanceof Player victim) {
            if (!Main.getInstance().getAbilitiesManager().playerIsInClass(victim, "Centinela")) return;
            if (victim.getHealth() >= 40) return;
            if (Main.getInstance().getAbilitiesManager().randomProbability(0.5)) {

                victim.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Tu pasiva de &bcentinela &fse ha activado!"));

                Location victimLocation = victim.getLocation();

                Main.getInstance().getParticlesManager().spawnParticleSphere(victim, victimLocation, Particle.TOWN_AURA, 0, 0, 0, 5, 100, 40);
                Main.getInstance().getParticlesManager().showParticles(victimLocation, Particle.CLOUD, 20);
                Main.getInstance().getAbilitiesManager().soundInRadius(victim, Sound.ENTITY_WITHER_DEATH, 0.1f, 1.0f);

                Collection<Entity> nearbyEntitiesCollection = victimLocation.getWorld().getNearbyEntities(victimLocation, 5, 5, 5);
                List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer) {
                        Location playerLocation = nearbyPlayer.getLocation();
                        if (nearbyPlayer == victim) continue;
                        double distanceToPlayer = victimLocation.distance(playerLocation);
                        if (distanceToPlayer <= 5) {
                            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                            if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                            if(Main.getInstance().getAbilitiesManager().isClanAlly(victim, nearbyPlayer)) continue;
                            if (nearbyPlayer.hasPotionEffect(PotionEffectType.SLOW)) continue;
                            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 4));
                        }
                    }
                }

            }
        }
    }

    // PASIVA GUERRERO
    @EventHandler
    public void onEntityDamageWarriorPassive(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (e.getEntity() instanceof Player victim) {
            if (!Main.getInstance().getAbilitiesManager().playerIsInClass(victim, "Guerrero")) return;
            if (victim.getHealth() >= 12) return;
            if (Main.getInstance().getAbilitiesManager().randomProbability(2)) {
                UUID victimUUID = victim.getUniqueId();
                if (divineShieldPlayers.containsKey(victimUUID)) return;
                divineShieldPlayers.put(victimUUID, e.getDamage());
                victim.playSound(victim.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
                BukkitRunnable spherePassiveWarriorTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(victim.getLocation())) return;
                        Main.getInstance().getParticlesManager().spawnParticleSphere(victim, victim.getLocation(), Particle.REDSTONE,
                                255, 255, 0, 2.5, 100, 5);
                    }
                }; spherePassiveWarriorTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        victim.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Tu pasiva de &bguerrero&f se ha acabado!"));
                        spherePassiveWarriorTask.cancel();
                        divineShieldPlayers.remove(victimUUID);
                    }
                }.runTaskLater(Main.getInstance(), 120L);
            }
        }
    }

    @EventHandler
    public void onEntityDamageWarriorPassive2(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) return;
        if (e.getEntity() instanceof Player victim) {
            if (!divineShieldPlayers.containsKey(victim.getUniqueId())) return;
            if (!(e.getDamager() instanceof Player attacker)) return;
            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(victim.getLocation())) return;
            if (Main.getInstance().getAbilitiesManager().randomProbability(66)) {
                e.setCancelled(true);
                victim.playSound(victim.getLocation(), Sound.BLOCK_CHAIN_HIT, 3.0f, 1.0f);
                attacker.playSound(attacker.getLocation(), Sound.BLOCK_CHAIN_HIT, 3.0f, 1.0f);
                Main.getInstance().getParticlesManager().showParticles(victim.getLocation(), Particle.VILLAGER_ANGRY, 1);
                Main.getInstance().getAbilitiesManager().sendActionBar(victim, "&f¡Has evitado el golpe de &b" + attacker.getName() + " &fgracias a tu escudo divino!");
                Main.getInstance().getAbilitiesManager().sendActionBar(attacker, "&c¡&n" + victim.getName() + "&c te ha evitado el golpe con su escudo divino!");
            }
        }
    }

    // PASIVA CURANDERO
    @EventHandler
    public void onEntityDamageHealerPassive(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (e.getEntity() instanceof Player victim) {
            if (!Main.getInstance().getAbilitiesManager().playerIsInClass(victim, "Curandero")) return;
            if (victim.getHealth() >= 40) return;
            if (Main.getInstance().getAbilitiesManager().randomProbability(0.5)) {

                Location victimLocation = victim.getLocation();

                Main.getInstance().getAbilitiesManager().soundInRadius(victim, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

                Main.getInstance().getParticlesManager().spawnParticleSphere(victim, victim.getLocation(), Particle.REDSTONE,
                        0, 255, 0, 5, 100, 200);

                BukkitRunnable healerPassiveTask = new BukkitRunnable() {
                    @Override
                    public void run() {

                        Collection<Entity> nearbyEntitiesCollection = victimLocation.getWorld().getNearbyEntities(victimLocation, 5, 5, 5);
                        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

                        for (Entity entity : nearbyEntities) {
                            if (entity instanceof Player nearbyPlayer) {
                                Location playersLocation = nearbyPlayer.getLocation();
                                double distanceToPlayer = victimLocation.distance(playersLocation);
                                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                                if (victim != nearbyPlayer) {if(!Main.getInstance().getAbilitiesManager().isClanAlly(victim, nearbyPlayer)) continue;}
                                if (distanceToPlayer <= 5) {
                                    Main.getInstance().getParticlesManager().showParticles(victim.getLocation(), Particle.HEART, 20);
                                    nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 0.8f);
                                    Main.getInstance().getParticlesManager().showParticles(nearbyPlayer.getLocation(), Particle.HEART, 3);

                                    double currentHealth = nearbyPlayer.getHealth();
                                    double maxHealth = nearbyPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                                    // Calcular la nueva salud sin exceder el máximo
                                    double newHealth = Math.min(currentHealth + 1, maxHealth);

                                    // Establecer la nueva salud del jugador
                                    nearbyPlayer.setHealth(newHealth);
                                }
                            }
                        }
                    }
                }; healerPassiveTask.runTaskTimer(Main.getInstance(), 0L, 40L);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        healerPassiveTask.cancel();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                victim.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Tu pasiva de &bcurandero&f se ha acabado!"));
                            }
                        }.runTask(Main.getInstance());
                    }
                }.runTaskLaterAsynchronously(Main.getInstance(), 200L);
            }
        }
    }
}