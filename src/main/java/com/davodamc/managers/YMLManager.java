package com.davodamc.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class YMLManager {

    public static String MYSQL_HOST;
    public static String MYSQL_PORT;
    public static String MYSQL_DATABASE;
    public static String MYSQL_USERNAME;
    public static String MYSQL_PASSWORD;

    public YMLManager(FileConfiguration config) {
        MYSQL_HOST = config.getString("mysql.host");
        MYSQL_PORT = config.getString("mysql.port");
        MYSQL_DATABASE = config.getString("mysql.database");
        MYSQL_USERNAME = config.getString("mysql.username");
        MYSQL_PASSWORD = config.getString("mysql.password");
    }

    public static YamlConfiguration getConfigYML() {
        File dataFolder = new File("plugins/Clases");

        if (!dataFolder.exists()) {dataFolder.mkdirs();}

        File file = new File(dataFolder, "config.yml");

        return YamlConfiguration.loadConfiguration(file);
    }
}