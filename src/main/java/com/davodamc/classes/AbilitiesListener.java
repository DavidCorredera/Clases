package com.davodamc.classes;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.davodamc.classes.assassin.BlindAbility.blindAbility;
import static com.davodamc.classes.assassin.EviscerateAbility.eviscerateAbility;
import static com.davodamc.classes.assassin.GarrotePoisonAbility.garrotePoisonAbility;
import static com.davodamc.classes.assassin.NeedleAbility.needleAbility;
import static com.davodamc.classes.assassin.SacrificeAbility.sacrificeAbility;
import static com.davodamc.classes.assassin.SprintAbility.sprintAbility;
import static com.davodamc.classes.assassin.TriumphantImmunityAbility.*;
import static com.davodamc.classes.healer.CycloneAbility.cycloneAbility;
import static com.davodamc.classes.healer.HealingEvasionAbility.healingEvasionAbility;
import static com.davodamc.classes.healer.HealingPlatformsAbility.carpetBlocks;
import static com.davodamc.classes.healer.HealingPlatformsAbility.healingPlatformsAbility;
import static com.davodamc.classes.healer.HealingStickAbility.healingStickAbility;
import static com.davodamc.classes.healer.HealingTotemAbility.healingTotemAbility;
import static com.davodamc.classes.healer.HealingTotemAbility.totemBlocks;
import static com.davodamc.classes.healer.RefugeProtectorAbility.refugeProtectorAbility;
import static com.davodamc.classes.healer.TangleAbility.tangleAbility;
import static com.davodamc.classes.mage.CorrosiveCurseAbility.corrosiveCurseAbility;
import static com.davodamc.classes.mage.CursedCloneAbility.cursedCloneAbility;
import static com.davodamc.classes.mage.ExplosiveRodAbility.explosiveRodAbility;
import static com.davodamc.classes.mage.LevitationAbility.levitationAbility;
import static com.davodamc.classes.mage.MeteorsAbility.meteorsAbility;
import static com.davodamc.classes.mage.ShadowCamouflageAbility.shadowCamouflageAbility;
import static com.davodamc.classes.mage.TeleportAbility.teleportAbility;
import static com.davodamc.classes.sentinel.CampfireAbility.campfireAbility;
import static com.davodamc.classes.sentinel.CampfireAbility.campfireBlocks;
import static com.davodamc.classes.sentinel.DeadlyFogAbility.deadlyFogAbility;
import static com.davodamc.classes.sentinel.DragAbility.dragAbility;
import static com.davodamc.classes.sentinel.JailAbility.jailAbility;
import static com.davodamc.classes.sentinel.JailAbility.jailedPlayers;
import static com.davodamc.classes.sentinel.LackOfControlAbility.lackOfControlAbility;
import static com.davodamc.classes.sentinel.SharpThornsAbility.sharpThornsAbility;
import static com.davodamc.classes.sentinel.SharpThornsAbility.thornsBlocks;
import static com.davodamc.classes.sentinel.ShockWaveAbility.shockWaveAbility;
import static com.davodamc.classes.warrior.AngerAbility.angerAbility;
import static com.davodamc.classes.warrior.AngerAbility.angerPlayers;
import static com.davodamc.classes.warrior.ChargerAbility.chargerAbility;
import static com.davodamc.classes.warrior.DevastatingBlowAbility.*;
import static com.davodamc.classes.warrior.DevastatingEarthquakeAbility.devastatingEarthquakeAbility;
import static com.davodamc.classes.warrior.DisarmAbility.disarmAbility;
import static com.davodamc.classes.warrior.IntimidationAbility.intimidationAbility;
import static com.davodamc.classes.warrior.LifeSupportAbility.lifeSupportAbility;

public class AbilitiesListener implements Listener {
    // EVENTOS PARA TODAS HABILIDADES
    @EventHandler
    public void onBuildAbility(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        if (p.getInventory().getItemInMainHand().getItemMeta() == null) return;

        String nameInHand = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName();

        if (nameInHand.contains("§8(§bI§8)") || (nameInHand.contains("§8(§bII§8)") || (nameInHand.contains("§8(§bIII§8)")
                || (nameInHand.contains("§8(§bIV§8)") || (nameInHand.contains("§8(§bV§8)")))))) e.setCancelled(true);
    }

    @EventHandler
    public void onBreakAbility(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block block = e.getBlock();
        Material blockType = block.getType();

        // HEALING TOTEM
        // CAMPFIRE

        if (blockType.equals(Material.IRON_BARS) || blockType.equals(Material.STONE_BRICKS)) {
            // Recorrer el mapa de jugadores prisioneros
            for (Map.Entry<UUID, List<Block>> entry : jailedPlayers.entrySet()) {
                // Verificar si el bloque está en la lista de bloques de la prisión
                List<Block> jailBlocks = entry.getValue();
                if (jailBlocks.contains(block)) {
                    // Cancelar el evento para evitar que cualquier jugador rompa el bloque
                    e.setCancelled(true);
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes romper bloques de la cárcel!"));
                    return;
                }
            }
        }

        if (blockType.equals(Material.DEAD_BUSH)) {
            for (Map.Entry<UUID, List<Block>> entry : thornsBlocks.entrySet()) {
                List<Block> thornsBlocksList = entry.getValue();
                if (thornsBlocksList.contains(block)) {
                    e.setCancelled(true);
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes romper las espinas punzantes!"));
                    return; // Salir del evento
                }
            }
        }

        if (blockType.name().endsWith("_CARPET")) {
            for (Map.Entry<UUID, List<Block>> entry : carpetBlocks.entrySet()) {
                List<Block> carpetBlocksList = entry.getValue();
                if (carpetBlocksList.contains(block)) {
                    e.setCancelled(true);
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes romper las plataformas curativas!"));
                    return; // Salir del evento
                }
            }
        }

        if(blockType.equals(Material.LIME_WOOL) || blockType.equals(Material.QUARTZ_STAIRS)
                || blockType.equals(Material.IRON_BARS) || blockType.equals(Material.FIRE) || blockType.equals(Material.NETHERRACK)) {
            for (Map.Entry<UUID, List<Block>> entry : totemBlocks.entrySet()) {
                List<Block> totemBlocksList = entry.getValue();
                if (totemBlocksList.contains(block)) {
                    e.setCancelled(true);
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes romper el tótem curativo!"));
                    return; // Salir del evento
                }
            }
        }
        if(blockType.equals(Material.SOUL_CAMPFIRE) || blockType.equals(Material.GRASS_BLOCK)) {
            for (Map.Entry<UUID, List<Block>> entry : campfireBlocks.entrySet()) {
                List<Block> campfireList = entry.getValue();
                if (campfireList.contains(block)) {
                    e.setCancelled(true);
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes romper el campamento!"));
                    return; // Salir del evento
                }
            }
        }
    }

    @EventHandler
    public void onConsumeAbility(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();

        if (p.getInventory().getItemInMainHand().getItemMeta() == null) return;

        String nameInHand = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName();

        if (nameInHand.contains("§8(§bI§8)") || (nameInHand.contains("§8(§bII§8)") || (nameInHand.contains("§8(§bIII§8)")
                || (nameInHand.contains("§8(§bIV§8)") || (nameInHand.contains("§8(§bV§8)")))))) e.setCancelled(true);
    }
    //

    //
    // EVENTOS ESPECÍFICOS HABILIDADES
    //

    // INMUNIDAD TRIUNFAL

    // Evento al matar a un jugador mientras la aureola está activa
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player attacker = e.getEntity().getKiller();

        if (attacker != null && activeAuraPlayers.containsKey(attacker.getUniqueId())) {
            UUID attackerUUID = attacker.getUniqueId();
            int invincibleTime = playerInvincibleTime.get(attackerUUID); // Obtener el tiempo de invencibilidad específico

            // Activa inmortalidad temporal para el atacante
            invinciblePlayers.put(attackerUUID, System.currentTimeMillis() + invincibleTime * 1000L);
            Main.getInstance().getParticlesManager().showParticles(attacker.getLocation(), Particle.TOTEM, 25);

            // Remover aureola activa y limpiar tiempo de inmortalidad guardado
            activeAuraPlayers.remove(attackerUUID);
            playerInvincibleTime.remove(attackerUUID);
        }
    }

    // Evento de daño para bloquear ataques a jugadores con inmortalidad
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        UUID victimUUID = victim.getUniqueId();

        // Verifica si el jugador tiene inmortalidad activa
        if (invinciblePlayers.containsKey(victimUUID)) {
            long endTime = invinciblePlayers.get(victimUUID);

            if (System.currentTimeMillis() < endTime) {
                // Mostrar mensaje al atacante y cancelar el evento de daño
                if (e.getDamager() instanceof Player attacker) {
                    attacker.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&cNo puedes golpear a " + victim.getName() + " porque tiene activada la protección de la inmunidad triunfal."));
                    attacker.spawnParticle(Particle.VILLAGER_ANGRY, victim.getLocation(), 1);
                }
                e.setCancelled(true);
            } else {
                invinciblePlayers.remove(victimUUID); // Expira la inmortalidad
            }
        }
    }

    // GOLPE DEVASTADOR
    // Evento de daño al caer
    @EventHandler
    public void onDamageDevastatingBlow(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        UUID playerUUID = p.getUniqueId();
        Integer radius = activePlayers.get(playerUUID);

        // Verifica que el jugador está en activePlayers y que el radio no es nulo
        if (radius != null) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                activePlayers.remove(playerUUID);
                e.setCancelled(true);
                Main.getInstance().getAbilitiesManager().soundInRadius(p, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1);

                Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(p.getLocation(), radius, radius, radius);
                for (Entity entity : nearbyEntitiesCollection) {
                    if (entity instanceof Player nearbyPlayer && nearbyPlayer != p) {
                        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                        if(Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                        double distanceToPlayer = p.getLocation().distance(nearbyPlayer.getLocation());
                        if (distanceToPlayer <= radius) {
                            // VECTOR HACIA ATRÁS EN DIRECCIÓN CONTRARIA AL JUGADOR
                            Vector toCenter = p.getLocation().toVector().subtract(nearbyPlayer.getLocation().toVector()).normalize();
                            Vector backwardsKnockback = toCenter.multiply(-2.2);
                            Vector upwardsPush = new Vector(0, 1.8, 0);
                            Vector combinedVelocity = backwardsKnockback.add(upwardsPush);

                            // PUSH Y DAÑO
                            nearbyPlayer.setVelocity(combinedVelocity);
                            Integer damage = activePlayersDamage.get(playerUUID);
                            Main.getInstance().getAbilitiesManager().applySwordDamage(p, nearbyPlayer, damage);
                            //nearbyPlayer.damage(damage, p);
                        }
                    }
                }

                activePlayersDamage.remove(playerUUID);
                Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.CLOUD, 70);
            }
        }
    }
    // IRA
    // Evento al golpear
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageAnger(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        Material itemInHand = attacker.getInventory().getItemInMainHand().getType();
        if(itemInHand == Material.NETHERITE_SWORD || itemInHand == Material.DIAMOND_SWORD || itemInHand == Material.TRIDENT
                || itemInHand == Material.NETHERITE_AXE || itemInHand == Material.DIAMOND_AXE) {
            UUID attackerUUID = attacker.getUniqueId();

            // Verifica si el jugador tiene inmortalidad activa
            if (angerPlayers.containsKey(attackerUUID)) {
                double damage = angerPlayers.get(attackerUUID);
                if (e.getEntity() instanceof Player victim) {
                    e.setDamage(e.getDamage() + damage);
                    Main.getInstance().getParticlesManager().showParticles(victim.getLocation(), Particle.FLASH, 1);
                }
            }
        }
    }
    //
    // DESARMAR
    // Evento al golpear desarmado
    @EventHandler
    public void onDamageWithDisarm(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;

        Entity damager = e.getDamager();
        Entity damaged = e.getEntity();

        Player attacker = null;
        Player victim = null;

        if ((damager instanceof Player) && (damaged instanceof Player)) {
            attacker = (Player) damager;
            victim = (Player) damaged;
        }

        if (attacker != null) {
            if (attacker.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) { // SOLO CUANDO TIENE MINING FATIGUE
                if (Main.getInstance().getAbilitiesManager().randomProbability(95.0)) { // 95% FALLA EL GOLPE SI ESTÁ CON DESARMAR
                    e.setCancelled(true);
                    attacker.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Fallaste el golpe por culpa del &bdesarmar&f!"));
                }
            }
        }

    }
    //

    // ACTIVAR HABILIDADES
    @EventHandler
    public void onUseAbility(PlayerInteractEvent e) {

        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();

        if (p.getInventory().getItemInMainHand().getItemMeta() == null) return;

        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            String itemName = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName();

            switch (itemName.split(" ")[0]) { // Obtener el primer segmento antes del nivel
                // ASESINO
                case "§7Ceguera":
                    switch (itemName) {
                        case "§7Ceguera §8(§bI§8)" -> blindAbility(p, 60, 5, 0, 80);
                        case "§7Ceguera §8(§bII§8)" -> blindAbility(p, 55, 6, 1, 100);
                        case "§7Ceguera §8(§bIII§8)" -> blindAbility(p, 50, 7, 1, 120);
                        case "§7Ceguera §8(§bIV§8)" -> blindAbility(p, 45, 8, 2, 140);
                        case "§7Ceguera §8(§bV§8)" -> blindAbility(p, 40, 9, 2, 160);
                    }
                    break;

                case "§4Destripar":
                    switch (itemName) {
                        case "§4Destripar §8(§bI§8)" -> eviscerateAbility(p, 24, 15, 20);
                        case "§4Destripar §8(§bII§8)" -> eviscerateAbility(p, 22, 20, 25);
                        case "§4Destripar §8(§bIII§8)" -> eviscerateAbility(p, 20, 25, 30);
                        case "§4Destripar §8(§bIV§8)" -> eviscerateAbility(p, 18, 30, 35);
                        case "§4Destripar §8(§bV§8)" -> eviscerateAbility(p, 16, 35, 40);
                    }
                    break;
                case "§aEsprintar":
                    switch (itemName) {
                        case "§aEsprintar §8(§bI§8)" -> sprintAbility(p, 60, 15, 0.30f, 60);
                        case "§aEsprintar §8(§bII§8)" -> sprintAbility(p, 55, 20, 0.35f, 70);
                        case "§aEsprintar §8(§bIII§8)" -> sprintAbility(p, 50, 25, 0.40f, 80);
                        case "§aEsprintar §8(§bIV§8)" -> sprintAbility(p, 45, 30, 0.45f, 90);
                        case "§aEsprintar §8(§bV§8)" -> sprintAbility(p, 40, 35, 0.50f, 100);
                    }
                    break;
                case "§6Inmunidad": // SE TIENE QUE QUEDAR ASÍ PORQUE ES HASTA EL PRIMER ESPACIO
                    switch (itemName) {
                        case "§6Inmunidad triunfal §8(§bI§8)" -> triumphantImmunityAbility(p, 60, 7, 4);
                        case "§6Inmunidad triunfal §8(§bII§8)" -> triumphantImmunityAbility(p, 55, 8, 4);
                        case "§6Inmunidad triunfal §8(§bIII§8)" -> triumphantImmunityAbility(p, 50, 9, 5);
                        case "§6Inmunidad triunfal §8(§bIV§8)" -> triumphantImmunityAbility(p, 45, 10, 6);
                        case "§6Inmunidad triunfal §8(§bV§8)" -> triumphantImmunityAbility(p, 40, 12, 6);
                    }
                    break;
                // CURANDERO
                case "§7Ciclón":
                    switch (itemName) {
                        case "§7Ciclón §8(§bI§8)" -> cycloneAbility(p, 60, 20, 15, 3);
                        case "§7Ciclón §8(§bII§8)" -> cycloneAbility(p, 55, 30, 20, 4);
                        case "§7Ciclón §8(§bIII§8)" -> cycloneAbility(p, 50, 40, 25, 5);
                        case "§7Ciclón §8(§bIV§8)" -> cycloneAbility(p, 45, 50, 30, 6);
                        case "§7Ciclón §8(§bV§8)" -> cycloneAbility(p, 40, 60, 35, 7);
                    }
                    break;
                case "§aEvasión":
                    switch (itemName) {
                        case "§aEvasión curativa §8(§bI§8)" -> healingEvasionAbility(p, 50, 5, 2, 30, 0.5f, 1.7f);
                        case "§aEvasión curativa §8(§bII§8)" -> healingEvasionAbility(p, 45, 6, 2.5, 35, 0.6f, 1.8f);
                        case "§aEvasión curativa §8(§bIII§8)" -> healingEvasionAbility(p, 40, 7, 3, 40, 0.6f, 1.9f);
                        case "§aEvasión curativa §8(§bIV§8)" -> healingEvasionAbility(p, 35, 8, 3.5, 50, 0.7f, 2.0f);
                        case "§aEvasión curativa §8(§bV§8)" -> healingEvasionAbility(p, 30, 9, 4, 60, 0.8f, 2.1f);
                    }
                    break;
                case "§aPlataformas":
                    switch (itemName) {
                        case "§aPlataformas curativas §8(§bI§8)" -> healingPlatformsAbility(p, 60, 1, Material.LIME_CARPET, 160, 1);
                        case "§aPlataformas curativas §8(§bII§8)" -> healingPlatformsAbility(p, 55, 2, Material.YELLOW_CARPET, 180, 1.25);
                        case "§aPlataformas curativas §8(§bIII§8)" -> healingPlatformsAbility(p, 50, 3, Material.ORANGE_CARPET, 200, 1.5);
                        case "§aPlataformas curativas §8(§bIV§8)" -> healingPlatformsAbility(p, 45, 4, Material.RED_CARPET, 220, 1.75);
                        case "§aPlataformas curativas §8(§bV§8)" -> healingPlatformsAbility(p, 40, 5, Material.PURPLE_CARPET, 240, 2);
                    }
                    break;
                case "§aTótem":
                    switch (itemName) {
                        case "§aTótem curativo §8(§bI§8)" -> healingTotemAbility(p, 60, 5, 10, 80, 1.5, 35);
                        case "§aTótem curativo §8(§bII§8)" -> healingTotemAbility(p, 55, 6, 15, 100, 1.75, 30);
                        case "§aTótem curativo §8(§bIII§8)" -> healingTotemAbility(p, 50, 7, 20, 120, 2.0, 30);
                        case "§aTótem curativo §8(§bIV§8)" -> healingTotemAbility(p, 45, 8, 25, 140, 2.25, 25);
                        case "§aTótem curativo §8(§bV§8)" -> healingTotemAbility(p, 40, 9, 30, 160, 2.5, 20);
                    }
                    break;
                case "§3Refugio":
                    switch (itemName) {
                        case "§3Refugio protector §8(§bI§8)" -> refugeProtectorAbility(p, 110, 4, 50);
                        case "§3Refugio protector §8(§bII§8)" -> refugeProtectorAbility(p, 100, 5, 75);
                        case "§3Refugio protector §8(§bIII§8)" -> refugeProtectorAbility(p, 90, 6, 100);
                        case "§3Refugio protector §8(§bIV§8)" -> refugeProtectorAbility(p, 80, 7, 125);
                        case "§3Refugio protector §8(§bV§8)" -> refugeProtectorAbility(p, 70, 8, 150);
                    }
                    break;
                case "§3Enredar":
                    switch (itemName) {
                        case "§3Enredar §8(§bI§8)" -> tangleAbility(p, 65, 20, 15, 5, 40);
                        case "§3Enredar §8(§bII§8)" -> tangleAbility(p, 60, 30, 20, 6, 40);
                        case "§3Enredar §8(§bIII§8)" -> tangleAbility(p, 55, 40, 25, 7, 60);
                        case "§3Enredar §8(§bIV§8)" -> tangleAbility(p, 50, 50, 30, 8, 80);
                        case "§3Enredar §8(§bV§8)" -> tangleAbility(p, 45, 60, 35, 9, 100);
                    }
                    break;
                // MAGO
                case "§7Maldición":
                    switch (itemName) {
                        case "§7Maldición corrosiva §8(§bI§8)" -> corrosiveCurseAbility(p, 55, 5, 1, 10, 100);
                        case "§7Maldición corrosiva §8(§bII§8)" -> corrosiveCurseAbility(p, 50, 6, 2, 20, 120);
                        case "§7Maldición corrosiva §8(§bIII§8)" -> corrosiveCurseAbility(p, 45, 7, 3,  30, 140);
                        case "§7Maldición corrosiva §8(§bIV§8)" -> corrosiveCurseAbility(p, 40, 7, 3,  40, 160);
                        case "§7Maldición corrosiva §8(§bV§8)" -> corrosiveCurseAbility(p, 35, 7, 3, 50, 180);
                    }
                    break;
                case "§7Clon":
                    switch (itemName) {
                        case "§7Clon maldito §8(§bI§8)" -> cursedCloneAbility(p, 55, 60, 10, 5, 2, 60);
                        case "§7Clon maldito §8(§bII§8)" -> cursedCloneAbility(p, 50, 60, 10, 5,2, 60);
                        case "§7Clon maldito §8(§bIII§8)" -> cursedCloneAbility(p, 45, 55, 9, 6, 2,80);
                        case "§7Clon maldito §8(§bIV§8)" -> cursedCloneAbility(p, 40, 55, 9, 6, 3, 100);
                        case "§7Clon maldito §8(§bV§8)" -> cursedCloneAbility(p, 35, 50, 8, 7, 2, 120);
                    }
                    break;
                case "§5Varita":
                    switch (itemName) {
                        case "§5Varita explosiva §8(§bI§8)" -> explosiveRodAbility(p, 60, 50.0, 20, 5.0);
                        case "§5Varita explosiva §8(§bII§8)" -> explosiveRodAbility(p, 55, 55.0, 25, 6.0);
                        case "§5Varita explosiva §8(§bIII§8)" -> explosiveRodAbility(p, 50, 65.0, 30, 7.0);
                        case "§5Varita explosiva §8(§bIV§8)" -> explosiveRodAbility(p, 45, 75.0, 35, 8.0);
                        case "§5Varita explosiva §8(§bV§8)" -> explosiveRodAbility(p, 40, 85.0, 40, 9.0);
                    }
                    break;
                case "§fLevitación":
                    switch (itemName) {
                        case "§fLevitación §8(§bI§8)" -> levitationAbility(p, 45, 15, 15, 4, 40);
                        case "§fLevitación §8(§bII§8)" -> levitationAbility(p, 40, 20, 20, 5, 60);
                        case "§fLevitación §8(§bIII§8)" -> levitationAbility(p, 35, 25, 25, 6, 80);
                        case "§fLevitación §8(§bIV§8)" -> levitationAbility(p, 30, 30, 30, 7, 100);
                        case "§fLevitación §8(§bV§8)" -> levitationAbility(p, 25, 35, 35, 8, 120);
                    }
                    break;
                case "§cMeteoritos":
                    switch (itemName) {
                        case "§cMeteoritos §8(§bI§8)" -> meteorsAbility(p, 65, 20, 15, 5, 40, 2);
                        case "§cMeteoritos §8(§bII§8)" -> meteorsAbility(p, 60, 25, 20, 6, 45, 3);
                        case "§cMeteoritos §8(§bIII§8)" -> meteorsAbility(p, 55, 35, 25, 7, 50, 3);
                        case "§cMeteoritos §8(§bIV§8)" -> meteorsAbility(p, 50, 40, 30, 8, 55, 4);
                        case "§cMeteoritos §8(§bV§8)" -> meteorsAbility(p, 45, 45, 35, 9, 60, 5);
                    }
                    break;
                case "§5Camuflaje":
                    switch (itemName) {
                        case "§5Camuflaje sombrío §8(§bI§8)" -> shadowCamouflageAbility(p, 50, 40, 5, 0.26f);
                        case "§5Camuflaje sombrío §8(§bII§8)" -> shadowCamouflageAbility(p, 45, 40, 4, 0.26f);
                        case "§5Camuflaje sombrío §8(§bIII§8)" -> shadowCamouflageAbility(p, 40, 60, 3, 0.27f);
                        case "§5Camuflaje sombrío §8(§bIV§8)" -> shadowCamouflageAbility(p, 35, 80, 2, 0.27f);
                        case "§5Camuflaje sombrío §8(§bV§8)" -> shadowCamouflageAbility(p, 30, 100, 1, 0.28f);
                    }
                    break;
                case "§9Teletransporte":
                    switch (itemName) {
                        case "§9Teletransporte §8(§bI§8)" -> teleportAbility(p, 45, 15, 15);
                        case "§9Teletransporte §8(§bII§8)" -> teleportAbility(p, 40, 20, 20);
                        case "§9Teletransporte §8(§bIII§8)" -> teleportAbility(p, 35, 25, 25);
                        case "§9Teletransporte §8(§bIV§8)" -> teleportAbility(p, 30, 30, 30);
                        case "§9Teletransporte §8(§bV§8)" -> teleportAbility(p, 25, 35, 35);
                    }
                    break;
                // CENTINELA
                case "§aCampamento":
                    switch (itemName) {
                        case "§aCampamento §8(§bI§8)" -> campfireAbility(p, 3, 130, 3, 3, 0.5, 65);
                        case "§aCampamento §8(§bII§8)" -> campfireAbility(p, 4, 120, 4, 4, 1.0, 60);
                        case "§aCampamento §8(§bIII§8)" -> campfireAbility(p, 5, 110, 5, 5, 1.0, 55);
                        case "§aCampamento §8(§bIV§8)" -> campfireAbility(p, 6, 100, 6, 6, 1.5, 50);
                        case "§aCampamento §8(§bV§8)" -> campfireAbility(p, 7, 90, 7, 7, 2.0, 45);
                    }
                    break;
                case "§fNiebla":
                    switch (itemName) {
                        case "§fNiebla mortal §8(§bI§8)" -> deadlyFogAbility(p, 55, 20, 15, 3, 120, 5);
                        case "§fNiebla mortal §8(§bII§8)" -> deadlyFogAbility(p, 50, 30, 20, 3, 140, 6);
                        case "§fNiebla mortal §8(§bIII§8)" -> deadlyFogAbility(p, 45, 40, 25, 4, 160, 8);
                        case "§fNiebla mortal §8(§bIV§8)" -> deadlyFogAbility(p, 40, 50, 30, 4, 180, 10);
                        case "§fNiebla mortal §8(§bV§8)" -> deadlyFogAbility(p, 35, 60, 35, 5, 200, 12);
                    }
                    break;
                case "§aArrastre":
                    switch (itemName) {
                        case "§aArrastre §8(§bI§8)" -> dragAbility(p, 50, 5, 10, 20, 0.5f, 100);
                        case "§aArrastre §8(§bII§8)" -> dragAbility(p, 45, 6, 12, 25, 0.5f, 110);
                        case "§aArrastre §8(§bIII§8)" -> dragAbility(p, 40, 7, 15, 30, 0.6f, 120);
                        case "§aArrastre §8(§bIV§8)" -> dragAbility(p, 35, 8, 17, 35, 0.6f, 140);
                        case "§aArrastre §8(§bV§8)" -> dragAbility(p, 30, 9, 20, 40, 0.7f, 160);
                    }
                    break;
                case "§7Condena":
                    switch (itemName) {
                        case "§7Condena §8(§bI§8)" -> jailAbility(p, 80, 15, 7, 1, 80);
                        case "§7Condena §8(§bII§8)" -> jailAbility(p, 75, 20, 10, 2, 90);
                        case "§7Condena §8(§bIII§8)" -> jailAbility(p, 70, 25, 13, 3, 100);
                        case "§7Condena §8(§bIV§8)" -> jailAbility(p, 65, 30, 16, 4, 110);
                        case "§7Condena §8(§bV§8)" -> jailAbility(p, 60, 35, 20, 5, 120);
                    }
                    break;
                case "§eDescontrol":
                    switch (itemName) {
                        case "§eDescontrol §8(§bI§8)" -> lackOfControlAbility(p, 80, 8, 200);
                        case "§eDescontrol §8(§bII§8)" -> lackOfControlAbility(p, 75, 9, 240);
                        case "§eDescontrol §8(§bIII§8)" -> lackOfControlAbility(p, 70, 10, 280);
                        case "§eDescontrol §8(§bIV§8)" -> lackOfControlAbility(p, 65, 11, 320);
                        case "§eDescontrol §8(§bV§8)" -> lackOfControlAbility(p, 60, 12, 360);
                    }
                    break;
                case "§6Espinas":
                    switch (itemName) {
                        case "§6Espinas punzantes §8(§bI§8)" -> sharpThornsAbility(p, 60, 6, 160, 5);
                        case "§6Espinas punzantes §8(§bII§8)" -> sharpThornsAbility(p, 55, 7, 180, 8);
                        case "§6Espinas punzantes §8(§bIII§8)" -> sharpThornsAbility(p, 50, 8, 200, 10);
                        case "§6Espinas punzantes §8(§bIV§8)" -> sharpThornsAbility(p, 45, 9, 220, 12.5);
                        case "§6Espinas punzantes §8(§bV§8)" -> sharpThornsAbility(p, 40, 10, 240, 15);
                    }
                    break;
                case "§aOnda":
                    switch (itemName) {
                        case "§aOnda de choque §8(§bI§8)" -> shockWaveAbility(p, 60, 2, 1, 12, 1.0, 1.6);
                        case "§aOnda de choque §8(§bII§8)" -> shockWaveAbility(p, 55, 3, 2, 11, 1.2, 1.8);
                        case "§aOnda de choque §8(§bIII§8)" -> shockWaveAbility(p, 50,  4, 3, 11, 1.4, 2.0);
                        case "§aOnda de choque §8(§bIV§8)" -> shockWaveAbility(p, 45,  5, 4, 10, 1.6, 2.2);
                        case "§aOnda de choque §8(§bV§8)" -> shockWaveAbility(p, 40,  6, 5, 10, 1.8, 2.4);
                    }
                    break;
                // GUERRERO
                case "§4Ira":
                    switch (itemName) {
                        case "§4Ira §8(§bI§8)" -> angerAbility(p, 60, 10, 2.5, 40);
                        case "§4Ira §8(§bII§8)" -> angerAbility(p, 55, 15, 3.0, 60);
                        case "§4Ira §8(§bIII§8)" -> angerAbility(p, 50, 20, 3.5, 80);
                        case "§4Ira §8(§bIV§8)" -> angerAbility(p, 45, 25, 4.0, 100);
                        case "§4Ira §8(§bV§8)" -> angerAbility(p, 40, 30, 4.5, 120);
                    }
                    break;
                case "§6Carga":
                    switch (itemName) {
                        case "§6Carga §8(§bI§8)" -> chargerAbility(p, 40, 15, 20, 4.0, 4.0, 10);
                        case "§6Carga §8(§bII§8)" -> chargerAbility(p, 35, 20, 25, 4.25, 5.0, 20);
                        case "§6Carga §8(§bIII§8)" -> chargerAbility(p, 30, 25, 30, 4.5, 6.0, 30);
                        case "§6Carga §8(§bIV§8)" -> chargerAbility(p, 25, 30, 35, 4.75, 7.0, 40);
                        case "§6Carga §8(§bV§8)" -> chargerAbility(p, 20, 35, 40, 5.0, 8.0, 50);
                    }
                    break;
                case "§eGolpe":
                    switch (itemName) {
                        case "§eGolpe devastador §8(§bI§8)" -> devastatingBlowAbility(p, 60, 5, 55);
                        case "§eGolpe devastador §8(§bII§8)" -> devastatingBlowAbility(p, 55, 6, 60);
                        case "§eGolpe devastador §8(§bIII§8)" -> devastatingBlowAbility(p, 50, 7, 65);
                        case "§eGolpe devastador §8(§bIV§8)" -> devastatingBlowAbility(p, 45, 8, 70);
                        case "§eGolpe devastador §8(§bV§8)" -> devastatingBlowAbility(p, 40, 9, 75);
                    }
                    break;
                case "§6Terremoto":
                    switch (itemName) {
                        case "§6Terremoto devastador §8(§bI§8)" -> devastatingEarthquakeAbility(p, 60, 50, 4, 40, 8);
                        case "§6Terremoto devastador §8(§bII§8)" -> devastatingEarthquakeAbility(p, 55, 70, 4, 45, 10);
                        case "§6Terremoto devastador §8(§bIII§8)" -> devastatingEarthquakeAbility(p, 50, 80, 5, 50, 12);
                        case "§6Terremoto devastador §8(§bIV§8)" -> devastatingEarthquakeAbility(p, 45, 90, 5, 55, 14);
                        case "§6Terremoto devastador §8(§bV§8)" -> devastatingEarthquakeAbility(p, 3, 100, 6, 60, 16);
                    }
                    break;
                case "§fDesarmar":
                    switch (itemName) {
                        case "§fDesarmar §8(§bI§8)" -> disarmAbility(p, 60, 5, 1, 80);
                        case "§fDesarmar §8(§bII§8)" -> disarmAbility(p, 55, 6, 2, 100);
                        case "§fDesarmar §8(§bIII§8)" -> disarmAbility(p, 50, 7, 2, 120);
                        case "§fDesarmar §8(§bIV§8)" -> disarmAbility(p, 45, 8, 3, 140);
                        case "§fDesarmar §8(§bV§8)" -> disarmAbility(p, 40, 9, 3, 160);
                    }
                    break;
                case "§cIntimidación":
                    switch (itemName) {
                        case "§cIntimidación §8(§bI§8)" -> intimidationAbility(p, 60, 5, 1, 80, 60, 5);
                        case "§cIntimidación §8(§bII§8)" -> intimidationAbility(p, 55, 6, 2, 100, 60, 6);
                        case "§cIntimidación §8(§bIII§8)" -> intimidationAbility(p, 50, 7, 2, 120, 40, 7);
                        case "§cIntimidación §8(§bIV§8)" -> intimidationAbility(p, 45, 8, 3, 140, 40, 8);
                        case "§cIntimidación §8(§bV§8)" -> intimidationAbility(p, 40, 9, 3, 160, 35, 9);
                    }
                    break;
                case "§cRefuerzo":
                    switch (itemName) {
                        case "§cRefuerzo vital §8(§bI§8)" -> lifeSupportAbility(p, 60, 2, 120);
                        case "§cRefuerzo vital §8(§bII§8)" -> lifeSupportAbility(p, 55, 3, 140);
                        case "§cRefuerzo vital §8(§bIII§8)" -> lifeSupportAbility(p, 50, 3, 160);
                        case "§cRefuerzo vital §8(§bIV§8)" -> lifeSupportAbility(p, 45, 4, 180);
                        case "§cRefuerzo vital §8(§bV§8)" -> lifeSupportAbility(p, 40, 4, 200);
                    }
                    break;
            }
        }
    }

    // ACTIVAR HABILIDADES CON RIGHTCLICK
    @EventHandler
    public void onUseClickedAbility(PlayerInteractEntityEvent e) {

        if (e.getHand() != EquipmentSlot.HAND) return;

        Player p = e.getPlayer();

        if (p.getInventory().getItemInMainHand().getItemMeta() == null) return;

        if (e.getRightClicked() instanceof Player) {

            String itemName = p.getInventory().getItemInMainHand().getItemMeta().getDisplayName();

            switch (itemName.split(" ")[0]) {
                // ASESINO
                case "§2Garrote":
                    switch (itemName) {
                        case "§2Garrote venenoso §8(§bI§8)" -> garrotePoisonAbility(p, (Player) e.getRightClicked(), 55, 20, 0, 20);
                        case "§2Garrote venenoso §8(§bII§8)" -> garrotePoisonAbility(p, (Player) e.getRightClicked(), 50, 25, 1, 25);
                        case "§2Garrote venenoso §8(§bIII§8)" -> garrotePoisonAbility(p, (Player) e.getRightClicked(), 45, 30, 2, 30);
                        case "§2Garrote venenoso §8(§bIV§8)" -> garrotePoisonAbility(p, (Player) e.getRightClicked(), 40, 35, 2, 35);
                        case "§2Garrote venenoso §8(§bV§8)" -> garrotePoisonAbility(p, (Player) e.getRightClicked(), 35, 40, 2, 40);
                    }
                    break;
                case "§eAguja":
                    switch (itemName) {
                        case "§eAguja de asesino §8(§bI§8)" -> needleAbility(p, (Player) e.getRightClicked(), 50, 40, 20);
                        case "§eAguja de asesino §8(§bII§8)" -> needleAbility(p, (Player) e.getRightClicked(), 45, 50, 25);
                        case "§eAguja de asesino §8(§bIII§8)" -> needleAbility(p, (Player) e.getRightClicked(), 40, 60, 30);
                        case "§eAguja de asesino §8(§bIV§8)" -> needleAbility(p, (Player) e.getRightClicked(), 35, 70, 35);
                        case "§eAguja de asesino §8(§bV§8)" -> needleAbility(p, (Player) e.getRightClicked(), 30, 80, 40);
                    }
                    break;
                case "§cSacrificio":
                    switch (itemName) {
                        case "§cSacrificio §8(§bI§8)" -> sacrificeAbility(p, (Player) e.getRightClicked(), 50, 60, 20);
                        case "§cSacrificio §8(§bII§8)" -> sacrificeAbility(p, (Player) e.getRightClicked(), 45, 65, 25);
                        case "§cSacrificio §8(§bIII§8)" -> sacrificeAbility(p, (Player) e.getRightClicked(), 40, 70, 30);
                        case "§cSacrificio §8(§bIV§8)" -> sacrificeAbility(p, (Player) e.getRightClicked(), 35, 75, 35);
                        case "§cSacrificio §8(§bV§8)" -> sacrificeAbility(p, (Player) e.getRightClicked(), 30, 80, 40);
                    }
                    break;

                // CURANDERO
                case "§aBastón":
                    switch (itemName) {
                        case "§aBastón curativo §8(§bI§8)" -> healingStickAbility(p, (Player) e.getRightClicked(), 60, 120, 20, 15, 0.25);
                        case "§aBastón curativo §8(§bII§8)" -> healingStickAbility(p, (Player) e.getRightClicked(), 55, 140, 25, 20, 0.25);
                        case "§aBastón curativo §8(§bIII§8)" -> healingStickAbility(p, (Player) e.getRightClicked(), 50, 160, 30, 25, 0.5);
                        case "§aBastón curativo §8(§bIV§8)" -> healingStickAbility(p, (Player) e.getRightClicked(), 45, 180, 35, 30, 0.75);
                        case "§aBastón curativo §8(§bV§8)" -> healingStickAbility(p, (Player) e.getRightClicked(), 40, 200, 40, 35, 1.0);
                    }
                    break;
            }
        }
    }
}