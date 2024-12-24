package com.davodamc.managers;

import com.davodamc.Main;
import com.davodamc.utils.ChatAPI;
import com.davodamc.utils.MojangRequest;
import me.ulrich.clans.Clans;
import me.ulrich.clans.api.PlayerAPIManager;
import me.ulrich.clans.data.ClanData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.SkinTrait;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.*;

import static com.davodamc.classes.sentinel.LackOfControlAbility.controlledPlayers;
import static com.davodamc.classes.sentinel.LackOfControlAbility.getControlledPlayers;

public class AbilitiesManager {

    public final Map<String, Set<Integer>> npcIds = new HashMap<>();

    public void sendActionBar(Player p, String message) {p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatAPI.cc(message)));}

    public boolean playerCanUseAbility(Player p, String abilityName, String className, Block targetBlock, Player clickedOrTargetPlayer) {
        String worldName = p.getWorld().getName();

        // MUNDOS PROHIBIDOS
        if (worldName.equals("Eventos") || worldName.equals("PlotsMe")) {
            if (worldName.equals("PlotsMe")) worldName = "parcelas";
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&cNo puedes usar " + abilityName + " en el mundo de " + worldName.toLowerCase() + "&c."));
            return false;
        }

        if (targetBlock != null) {
            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(targetBlock.getLocation())) {
                p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes utilizar " + abilityName + " en ese bloque porque está en una zona protegida!"));
                return false;
            }
        }

        if (clickedOrTargetPlayer != null) {
            if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(clickedOrTargetPlayer.getLocation())) {
                p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes utilizar " + abilityName + " sobre un jugador en una zona protegida!"));
                return false;
            }
        }

        if (Main.getInstance().getWorldGuardManager().isPvpDisabledInRegion(p.getLocation())) {
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes utilizar " + abilityName + " en esta zona protegida!"));
            return false;
        }

        // Verificar si el jugador está controlado
        if (isPlayerControlled(p)) return false;

        // Verificar cooldown
        if (Main.getInstance().getCooldownManager().isOnCooldown(p, abilityName)) {
            long cooldownRemaining = Main.getInstance().getCooldownManager().getCooldownTimeRemaining(p, abilityName);
            Main.getInstance().getAbilitiesManager().sendActionBar(p, "&c¡Tienes que esperar &l" + cooldownRemaining + (cooldownRemaining == 1 ? " segundo" : " segundos") + " &cpara poder usar " + abilityName + "!");
            return false;
        }

        // Verificar clase en la base de datos
        try {
            String playerClass = Main.getInstance().getMySQLManager().getPlayerClass(p.getName());
            if (playerClass == null) playerClass = "Ninguna";

            if (!className.equalsIgnoreCase(playerClass)) {
                p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡Debes ser " + className.toLowerCase() + " &cpara utilizar " + abilityName + "&c!"));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&cHubo un error al verificar tu clase. Inténtalo más tarde."));
            return false;
        }

        return true;
    }

    public boolean isProhibitedRegion(String regionId) {
        return regionId.equals("spawn") || regionId.equals("Entrada") || regionId.equals("Alrededores") ||
                regionId.equals("spawnminapvp") || regionId.equals("spawnend") || regionId.equals("elfos") ||
                regionId.equals("guerradeclanes1") || regionId.equals("guerradeclanes2") || regionId.equals("guerradeclanes3") ||
                regionId.equals("guerradeclanes4") || regionId.equals("Aerendal");
    }

    private boolean isPlayerControlled(Player p) {
        Map<UUID, String> controlled = getControlledPlayers();
        UUID playerUUID = p.getUniqueId();
        if(controlled.containsKey(playerUUID)) {
            PotionEffect effect = p.getPotionEffect(PotionEffectType.GLOWING);
            int durationInSeconds = (effect != null) ? effect.getDuration() / 20 : 0;
            p.sendMessage(ChatAPI.cc(ChatAPI.prefix + "&c¡No puedes utilizar habilidades porque estás controlado por " + controlled.get(playerUUID) + " durante " + durationInSeconds + " segundos más!"));
            return true;
        }
        return false;
    }


    public boolean playerIsInClass(Player p, String className) {
        try {
            String playerClass = Main.getInstance().getMySQLManager().getPlayerClass(p.getName());

            if (playerClass == null) return false;

            if(!className.equalsIgnoreCase(playerClass)) return false;

        } catch (Exception e) {e.printStackTrace();}

        return true;
    }

    public boolean isClanAlly(Player p, Player p2) {

        if (p == p2) return true;

        Clans clansPlugin = JavaPlugin.getPlugin(Clans.class);
        PlayerAPIManager playerAPI = clansPlugin.getPlayerAPI();

        // Obtener los clanes de ambos jugadores
        Optional<ClanData> clan1Opt = playerAPI.getPlayerClan(p.getUniqueId());
        Optional<ClanData> clan2Opt = playerAPI.getPlayerClan(p2.getUniqueId());

        // Verificar que ambos jugadores tienen clan
        if (clan1Opt.isEmpty() || clan2Opt.isEmpty()) return false;

        ClanData clan1 = clan1Opt.get();
        ClanData clan2 = clan2Opt.get();

        // Si tienen FF, que continue
        if (clan1.isFf() || clan2.isFf()) return false;

        UUID clanUUID1 = clan1.getId();
        UUID clanUUID2 = clan2.getId();

        // Si ambos jugadores son del mismo clan, devolver true
        if (clanUUID1.equals(clanUUID2)) return true;

        // Verificar si ambos clanes son aliados
        List<UUID> clanAlly1 = clan1.getRivalAlly().getAlly();
        List<UUID> clanAlly2 = clan2.getRivalAlly().getAlly();

        // Comprobar si cada clan es aliado del otro
        return clanAlly1.contains(clanUUID2) && clanAlly2.contains(clanUUID1); // No son aliados
    }


    public boolean randomProbability(double percentage) {
        if (percentage < 0 || percentage > 100) throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");

        double decimalProbability = percentage / 100.0;
        return Math.random() < decimalProbability;
    }

    // MÉTODO PARA SABER SI ES UN NPC O STAFF
    public boolean isNPCOrStaff(Player player) {
        if(player == null) return false;
        if(player.hasPermission("kingscraft.staff")) return true;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(player);
        return npc != null;
    }

    // MÉTODO PARA HACER DAÑO
    public void applySwordDamage(Player attacker, Player victim, double damage) {
        // Guarda el slot actual del jugador
        int originalSlot = attacker.getInventory().getHeldItemSlot();

        // Busca el slot que contiene la espada (ajusta según tu lógica de selección de espada)
        int swordSlot = findSwordSlot(attacker);

        if (swordSlot == 4000) {
            victim.damage(damage, attacker);
            return;
        }

        // Cambia al slot de la espada
        attacker.getInventory().setHeldItemSlot(swordSlot);

        // Aplica el daño como si fuera un ataque con espada
        victim.damage(damage, attacker);

        // Restaura el slot original del jugador
        attacker.getInventory().setHeldItemSlot(originalSlot);
    }

    // Método para encontrar el slot de la espada
    private int findSwordSlot(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            // Asegúrate de que el item no es null antes de hacer comparaciones
            if (item != null && (
                    item.getType().toString().endsWith("_SWORD") ||
                            item.getType().toString().endsWith("_AXE") ||
                            item.getType().toString().startsWith("TRIDENT"))
            ) {
                return i;
            }
        }
        return 4000; // No se encontró una espada, hacha o tridente
    }


    /* MÉTODO ANTIGUO PARA LA CARGA, FUNCIONA REGULAR
    public boolean isOnLineOfSight(Vector start, Vector end, Vector point) {
        double distance = start.distance(end);
        double startDistance = start.distance(point);
        double finalDistance = end.distance(point);

        // La suma de las distancias de inicio a punto y de fin a punto debe ser aproximadamente igual a la distancia total del rayo
        return Math.abs(startDistance + finalDistance - distance) < 0.1;
    }
     */

    public Player getTargetPlayer(Player player, double range) {
        // Traza un rayo desde la ubicación de la cabeza del jugador en la dirección de su mirada.
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),            // Punto de inicio (ojos del jugador)
                player.getEyeLocation().getDirection(), // Dirección de la mirada del jugador
                range,                              // Rango máximo del rayo
                entity -> entity instanceof Player && !entity.equals(player) // Filtro para solo detectar otros jugadores
        );

        // Verifica si se detectó un jugador en la trayectoria del rayo
        if (result != null) {
            Entity entity = result.getHitEntity();
            if (entity instanceof Player) {
                return (Player) entity; // Retorna el jugador objetivo
            }
        }

        return null; // Retorna null si no se encuentra ningún jugador en la línea de visión
    }

    public void soundInRadius(Player p, Sound sound, float v, float v1) {
        p.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distance(p.getLocation()) <= 26)
                .forEach(player -> player.playSound(player.getLocation(), sound, v, v1));
    }

    public void launchColoredFirework(Player p, int timeToExplode) {
        // Crear el fuego artificial en la ubicación del jugador
        Location location = p.getLocation();
        Firework firework = location.getWorld().spawn(location, Firework.class);

        // Configurar el efecto del fuego artificial
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(
                FireworkEffect.builder()
                        .withColor(Color.BLACK)      // Color inicial negro
                        .withFade(Color.RED)         // Cambia a rojo al desvanecerse
                        .with(FireworkEffect.Type.BALL_LARGE)       // Tipo de efecto de bola grande
                        .trail(true)                 // Añadir rastro al efecto
                        .build()
        );
        firework.setFireworkMeta(fireworkMeta);

        // Configurar el tiempo de explosión del fuego artificial
        fireworkMeta.setPower(timeToExplode);  // Establecer el tiempo de explosión en ticks
        firework.setFireworkMeta(fireworkMeta);
    }

    public void deleteNPCbyID(int npcId) {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
        if (npc != null) {
            npc.destroy();
        }
    }

    // Método para guardar el ID del NPC
    public void saveNPCID(int npcID, String playerName) {
        // Aquí usamos computeIfAbsent en npcIds, que es un Map<String, Set<Integer>>
        Set<Integer> npcSet = npcIds.computeIfAbsent(playerName, k -> new HashSet<>());
        npcSet.add(npcID);
    }

    // Método para eliminar NPCs de un jugador específico
    public void deleteNpcs(String playerName) {
        Set<Integer> npcSet = npcIds.get(playerName);
        if (npcSet != null) {
            for (int npcId : npcSet) {
                deleteNPCbyID(npcId);
            }
            removeNpcIds(playerName);
        }
    }


    public void removeNpcIds(String playerName) {npcIds.remove(playerName);}

    public void createNPC(Player p, Location spawnLocation, Location finalLocation) {

        Main.getInstance().getMojangRequest().getSkinDataAsync(p, Main.getInstance(), new MojangRequest.SkinDataCallback() {
            public void onSuccess(String skinData, String skinSignature) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, p.getName());
                    saveNPCID(npc.getId(), p.getName());

                    Location locNPC = spawnLocation.clone();

                    // PITCH Y YAW (DONDE MIRA EL NPC)
                    locNPC.setYaw(spawnLocation.getYaw());
                    locNPC.setPitch(spawnLocation.getPitch());

                    // APLICAR SKIN
                    npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(p.getName(), skinSignature, skinData);

                    // ARMADURA Y OBJETOS EN LA MANO
                    if (p.getEquipment().getHelmet() != null) npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HELMET, p.getEquipment().getHelmet());
                    if (p.getEquipment().getChestplate() != null) npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.CHESTPLATE, p.getEquipment().getChestplate());
                    if (p.getEquipment().getLeggings() != null) npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.LEGGINGS, p.getEquipment().getLeggings());
                    if (p.getEquipment().getBoots() != null) npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.BOOTS, p.getEquipment().getBoots());
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, p.getEquipment().getItemInMainHand());
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.OFF_HAND, p.getEquipment().getItemInOffHand());

                    // HACERLO VULNERABLE
                    npc.setProtected(false);
                    // SPAWN NPC
                    npc.spawn(locNPC);

                    //npc.getNavigator().setTarget(finalLocation);

                    npc.getNavigator().setStraightLineTarget(finalLocation);

                }, 2L);

            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}