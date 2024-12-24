package com.davodamc.classes.warrior;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.bukkit.Bukkit.getServer;

public class ChargerAbility {

    private static final String ABILITY_NAME = "carga";

    public static void chargerAbility(Player p, Integer cooldown, Integer quantityParticles, Integer maxDistance, Double speed, Double immobilizeRadius, Integer immobilizeTime) {

        Player targetPlayer = Main.getInstance().getAbilitiesManager().getTargetPlayer(p, 35);
        if (targetPlayer == null) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡La carga debes utilizarla apuntando hacia un jugador!"));
            return;
        }

        if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(targetPlayer)) return;

        if (!Main.getInstance().getAbilitiesManager().playerCanUseAbility(p, ABILITY_NAME, "Guerrero", null, targetPlayer)) return;

        Vector direction = p.getLocation().getDirection(); // Obtener la dirección hacia donde está mirando el jugador
        Vector start = p.getEyeLocation().toVector(); // Obtener el punto de inicio del rayo desde los ojos del jugador

        // Multiplicar la dirección por una distancia suficientemente grande para cubrir la distancia máxima de visión
        Vector end = start.clone().add(direction.multiply(maxDistance));

        double immobilizeDistance = maxDistance / 2.0;
        double distance = end.distance(start);

        // Iterar a través de los jugadores y verificar si están en la línea de visión
        for (Player tp : getServer().getOnlinePlayers()) {
            if (tp.equals(p)) continue; // No queremos verificar la línea de visión del jugador consigo mismo
            if(Main.getInstance().getAbilitiesManager().isClanAlly(p, tp)) continue;

            if (distance >= immobilizeDistance) {
                Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.CLOUD, 15);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Entity entity : p.getNearbyEntities(immobilizeRadius, immobilizeRadius, immobilizeRadius)) {
                            // Verificar si la entidad es un jugador
                            if (entity instanceof Player nearbyPlayer) {
                                if(nearbyPlayer == p) continue;
                                if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(nearbyPlayer.getLocation())) continue;
                                if(Main.getInstance().getAbilitiesManager().isNPCOrStaff(nearbyPlayer)) continue;
                                if(Main.getInstance().getAbilitiesManager().isClanAlly(p, nearbyPlayer)) continue;
                                if(nearbyPlayer.hasPotionEffect(PotionEffectType.SLOW)) continue;
                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, immobilizeTime, 10));
                            }
                        }
                    }
                }.runTaskLater(Main.getInstance(), 30L);
            }

            Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

            p.setVelocity(p.getLocation().getDirection().multiply(speed));

            Main.getInstance().getParticlesManager().showParticles(p.getLocation(), Particle.WAX_OFF, quantityParticles);

        }
    }
}