package com.davodamc.classes.healer;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HealingEvasionAbility {

    private static final String ABILITY_NAME = "evasión curativa";

    public static void healingEvasionAbility(Player p, Integer cooldown, int healingRadius, double heartsHeal, int quantityParticles, float upwardsForce, float backwardsForce) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();
        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Curandero", null, null)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        Location healingLocation = p.getLocation().clone();

        Collection<Entity> nearbyEntitiesCollection = p.getWorld().getNearbyEntities(healingLocation, healingRadius, healingRadius, healingRadius);
        List<Entity> nearbyEntities = new ArrayList<>(nearbyEntitiesCollection);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player nearbyPlayer) {
                Location playersLocation = nearbyPlayer.getLocation();
                if(nearbyPlayer == p) continue;
                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                if(abilitiesManager.isNPCOrStaff(nearbyPlayer)) continue;
                double distanceToPlayer = healingLocation.distance(playersLocation);
                if (distanceToPlayer <= healingRadius) {
                    if (!abilitiesManager.isClanAlly(p, nearbyPlayer)) continue;
                    double currentHealth = nearbyPlayer.getHealth();
                    double maxHealth = nearbyPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                    // Calcular la nueva salud sin exceder el máximo
                    double newHealth = Math.min(currentHealth + heartsHeal, maxHealth);

                    // Establecer la nueva salud del jugador
                    nearbyPlayer.setHealth(newHealth);
                    nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.BLOCK_BEEHIVE_WORK, 0.4f, 1.0f);
                    particlesManager.showParticles(nearbyPlayer.getLocation(), Particle.VILLAGER_HAPPY, quantityParticles - 15);
                }
            }
        }

        particlesManager.showParticles(healingLocation, Particle.CLOUD, quantityParticles);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, 0.8f, 1.0f);

        Vector direction = p.getLocation().getDirection().normalize();
        Vector launchVector = direction.multiply(-backwardsForce); // Multiplicar por -1 para que vaya hacia atrás
        launchVector.setY(upwardsForce); // Establecer la componente vertical de fuerza hacia arriba

        // Aplicar la velocidad al jugador
        p.setVelocity(launchVector);
    }
}
