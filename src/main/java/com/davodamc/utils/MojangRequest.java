package com.davodamc.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangRequest {

    public interface SkinDataCallback {
        void onSuccess(String skinData, String skinSignature);
        void onError(Throwable throwable);
    }

    public void getSkinDataAsync(Player player, JavaPlugin plugin, SkinDataCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + player.getName()).openConnection();

                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!response.has("id")) {
                        // Player is not premium, load skin of "Davoda" from Mojang's servers
                        loadSkinFromMojang("Davoda", plugin, callback);
                        return;
                    }

                    loadSkinFromMojang(player.getName(), plugin, callback);
                }
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.onError(e));
            }
        });
    }

    private void loadSkinFromMojang(String playerName, JavaPlugin plugin, SkinDataCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName).openConnection();

                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
                    String uuid = response.get("id").getAsString();

                    HttpURLConnection connection2 = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").openConnection();

                    try (InputStreamReader reader2 = new InputStreamReader(connection2.getInputStream())) {
                        JsonObject response2 = JsonParser.parseReader(reader2).getAsJsonObject();
                        JsonObject properties = response2.get("properties").getAsJsonArray().get(0).getAsJsonObject();

                        String skinData = properties.get("value").getAsString();
                        String skinSignature = properties.get("signature").getAsString();

                        Bukkit.getScheduler().runTask(plugin, () -> callback.onSuccess(skinData, skinSignature));
                    }
                }
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> callback.onError(e));
            }
        });
    }
}
