package com.davodamc.classes.healer;

import com.davodamc.Main;
import com.davodamc.managers.AbilitiesManager;
import com.davodamc.managers.ParticlesManager;
import com.davodamc.utils.ChatAPI;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HealingStickAbility {

    private static final String ABILITY_NAME = "bastón curativo";

    public static void healingStickAbility(Player p, Player clickedPlayer, Integer cooldown, int duration, int quantityParticles, int maxDistance, double heartsHeal) {

        AbilitiesManager abilitiesManager = Main.getInstance().getAbilitiesManager();
        ParticlesManager particlesManager = Main.getInstance().getParticlesManager();

        if(!abilitiesManager.playerCanUseAbility(p, ABILITY_NAME, "Curandero", null, clickedPlayer)) return;

        Main.getInstance().getCooldownManager().setCooldown(p, ABILITY_NAME, cooldown);

        if(abilitiesManager.isNPCOrStaff(clickedPlayer)) return;

        if (!abilitiesManager.isClanAlly(p, clickedPlayer)) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes utilizar el " + ABILITY_NAME + " &ccontra enemigos de tu clan!"));
            return;
        }

        p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Estás curando a &b" + clickedPlayer.getName() + " &fdurante &b" + duration / 20 + "&f segundos!"));
        clickedPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Estás siendo curado por &b" + p.getName() + " &fdurante &b" + duration / 20 + " &fsegundos!"));

        BukkitRunnable healStickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // DETECTAR SI ESTÁN CERCA PARA SEGUIR CURANDO
                if (clickedPlayer.getLocation().distance(p.getLocation()) > maxDistance) {
                    p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Te has alejado más de &b" + maxDistance + " &fbloques de " +
                            "&b" + clickedPlayer.getName() + " &fy se ha parado la curación, acércate a él de nuevo para reanudarla!"));
                    clickedPlayer.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&f¡Te has alejado más de &b" + maxDistance + " &fbloques de " +
                            "&b" + p.getName() + " &fy se ha parado la curación, acércate a él de nuevo para reanudarla!"));
                } else {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
                    clickedPlayer.playSound(clickedPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);

                    Main.getInstance().getParticlesManager().showParticles(clickedPlayer.getLocation(), Particle.HEART, quantityParticles);

                    double currentHealth = clickedPlayer.getHealth();
                    double maxHealth = clickedPlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                    // Calcular la nueva salud sin exceder el máximo
                    double newHealth = Math.min(currentHealth + heartsHeal, maxHealth);

                    // Establecer la nueva salud del jugador
                    clickedPlayer.setHealth(newHealth);
                }
            }
        }; healStickTask.runTaskTimer(Main.getInstance(), 0L, 30L);

        BukkitRunnable haloHealingStickTask = new BukkitRunnable() {
            @Override
            public void run() {
                // DOBLE AUREOLA, UNA PARA EL PLAYER Y OTRA PARA EL CLICKEDPLAYER
                Main.getInstance().getParticlesManager().createHalo(p, 0, 143, 47, 55, 0.75);
                Main.getInstance().getParticlesManager().createHalo(clickedPlayer, 0, 255, 0, 55, 0.75);
            }
        }; haloHealingStickTask.runTaskTimerAsynchronously(Main.getInstance(), 0L, 5L);

        new BukkitRunnable() {
            @Override
            public void run() {
                // CANCELAR TASK DE CURACIÓN Y DE AUREOLA
                healStickTask.cancel();
                haloHealingStickTask.cancel();
            }
        }.runTaskLaterAsynchronously(Main.getInstance(), duration);
    }
}