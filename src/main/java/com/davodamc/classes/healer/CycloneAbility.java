package com.davodamc.classes.healer;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CycloneAbility {

    private static final String ABILITY_NAME = "ciclón";

    public static void cycloneAbility(Player p, Integer cooldown, int quantityParticles, int reachDistance, int radius) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();
        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();
        Block targetBlock = p.getTargetBlock(null, reachDistance);

        if (!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Curandero", targetBlock, null)) return;

        Location playerLocation = p.getLocation();
        Location targetBlockLocation = targetBlock.getLocation();

        double distance = playerLocation.distance(targetBlockLocation);

        if (distance > reachDistance) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado lejos para generar " + ABILITY_NAME + "!"));
            return;
        }

        if (targetBlock.getLocation().getBlockY() > p.getLocation().getY()) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Ese bloque está demasiado alto para generar " + ABILITY_NAME + "!"));
            return;
        }

        Location cycloneLocation = targetBlock.getLocation().clone().add(0.0, 0.5, 0.0);

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        abilitiesManager.soundInRadius(p, Sound.ENTITY_HORSE_BREATHE, 0.8f, 1.0f);

        particlesManager.showParticles(cycloneLocation, Particle.CLOUD, quantityParticles);

        Vector[] pushDirections = {
                new Vector(1, 1, 0),
                new Vector(-1, 1, 0),
                new Vector(0, 1, 1),
                new Vector(0, 1, -1)
        };

        // Encuentra a los jugadores cercanos al área del ciclón una vez
        Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(targetBlockLocation, radius, radius, radius);
        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

        List<Player> affectedPlayers = new ArrayList<>();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player nearbyPlayer) {
                Location nearbyPlayerLocation = nearbyPlayer.getLocation();
                if(nearbyPlayer == p) continue;
                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                if(abilitiesManager.isNPCOrStaff(nearbyPlayer)) continue;
                if (abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;
                double distanceToPlayer = targetBlockLocation.distance(nearbyPlayerLocation);
                if (distanceToPlayer <= radius) {
                    if (nearbyPlayerLocation.distance(cycloneLocation) <= radius) {
                        if (abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;
                        affectedPlayers.add(nearbyPlayer);
                    }
                }
            }
        }

        // Aplica los empujes en diferentes direcciones con retrasos
        for (int i = 0; i < pushDirections.length; i++) {
            final Vector direction = pushDirections[i].normalize().multiply(1.5);

            // Retrasa el empuje para que ocurra en intervalos diferentes
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                for (Player player : affectedPlayers) {
                    player.setVelocity(direction);
                    particlesManager.showParticles(player.getLocation(), Particle.SPELL_INSTANT, quantityParticles);
                }
            }, i * 8L);
        }
    }
}
