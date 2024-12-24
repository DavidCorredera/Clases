package com.davodamc;

import com.davodamc.classes.AbilitiesListener;
import com.davodamc.classes.PassivesListener;
import com.davodamc.classes.assassin.*;
import com.davodamc.classes.healer.*;
import com.davodamc.classes.mage.*;
import com.davodamc.classes.sentinel.*;
import com.davodamc.classes.warrior.*;
import com.davodamc.commands.SelectClassCommand;
import com.davodamc.listeners.GlobalListeners;
import com.davodamc.listeners.RemoveClass;
import com.davodamc.listeners.SelectClass;
import com.davodamc.managers.*;
import com.davodamc.menus.SelectClassMenu;
import com.davodamc.utils.ClassPlaceholder;
import com.davodamc.utils.MojangRequest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    private SelectClassManager selectClassManager;
    private SelectClassMenu selectClassMenu;

    private CooldownManager cooldownManager;
    private ParticlesManager particlesManager;
    private AbilitiesManager abilitiesManager;
    private WorldGuardManager worldGuardManager;

    private MojangRequest mojangRequest;

    private LackOfControlAbility lackOfControlAbility;

    private MySQLManager mySQLManager;

    @Override
    public void onEnable() {
        // INSTANCIAS

        instance = this;

        this.selectClassManager = new SelectClassManager();
        this.selectClassMenu = new SelectClassMenu();

        this.cooldownManager = new CooldownManager();
        this.abilitiesManager = new AbilitiesManager();
        this.worldGuardManager = new WorldGuardManager();
        this.particlesManager = new ParticlesManager(this);

        this.mojangRequest = new MojangRequest();

        this.mySQLManager = new MySQLManager();

        new YMLManager(YMLManager.getConfigYML());

        // AbilitiesListener
        AbilitiesListener abilitiesListener = new AbilitiesListener();
        // Pasivas
        PassivesListener passivesListener = new PassivesListener();

        // Otros
        GlobalListeners globalListeners = new GlobalListeners();
        SelectClass selectClass = new SelectClass();
        RemoveClass removeClass = new RemoveClass();

        // **LISTENERS**

        // No habilidades
        getServer().getPluginManager().registerEvents(globalListeners, this);
        getServer().getPluginManager().registerEvents(selectClass, this);
        getServer().getPluginManager().registerEvents(removeClass, this);

        // HABILIDADES
        getServer().getPluginManager().registerEvents(abilitiesListener, this);

        // Pasivas
        getServer().getPluginManager().registerEvents(passivesListener, this);

        // COMANDOS
        getCommand("clases").setExecutor(new SelectClassCommand());

        // CARGAR PLACEHOLDER

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new ClassPlaceholder(mySQLManager).register(); //
        }

        // CARGAR BASE DE DATOS
        try {
            getMySQLManager().connect();
            getLogger().info("Conexión a la base de datos establecida.");
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("No se pudo conectar a la base de datos.");
            getServer().getPluginManager().disablePlugin(this); // Desactivar el plugin si falla la conexión
        }


        // LOAD MSG
        getLogger().info("¡Las clases han cargado correctamente!");
    }

    @Override
    public void onDisable() {
        try {
            getMySQLManager().disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("¡Las clases se han deshabilitado correctamente!");
    }

    public static Main getInstance() {
        return instance;
    }
    public SelectClassManager getSelectClassManager() {return selectClassManager;}
    public SelectClassMenu getSelectClassMenu() {return selectClassMenu;}
    public CooldownManager getCooldownManager() {return cooldownManager;}
    public AbilitiesManager getAbilitiesManager() {return abilitiesManager;}
    public WorldGuardManager getWorldGuardManager() {return worldGuardManager;}
    public ParticlesManager getParticlesManager() {return particlesManager;}
    public MojangRequest getMojangRequest() {return mojangRequest;}
    public MySQLManager getMySQLManager() {return mySQLManager;}
}