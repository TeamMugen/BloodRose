package com.mugen.bloodrose;

import com.mugen.bloodrose.commands.CommandAliases;
import com.mugen.bloodrose.commands.CommandRosePvP;
import com.mugen.bloodrose.commands.TabComplete;
import com.mugen.bloodrose.commands.triggers.Arena;
import com.mugen.bloodrose.commands.triggers.GUI;
import com.mugen.bloodrose.commands.triggers.Spawn;
import com.mugen.bloodrose.filemanager.FileManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mugen.bloodrose.commands.triggers.GUI.initializeMenu;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.modes;
import static com.mugen.bloodrose.utils.Util.runTask;

public final class BloodRose extends JavaPlugin {
    private static Economy econ = null;
    private static BloodRose instance;

    @Override
    public void onEnable() {
        getLogger().info("Hello, World");

        if (!setupEconomy()) {
            getLogger().warning(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // config.ymlがプラグインデータフォルダに存在しない場合、デフォルトのconfig.ymlをコピーする。
        saveDefaultConfig();
        instance = this;
        FileManager fileManager = new FileManager(this);
        try {
            FileManager.loadFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule sendCommandFeedback false"));
        registerCommands();
        registerEvents();
        initializeMenu();
    }

    @Override
    public void onDisable() {
        getLogger().info("Good Bye ;)");
    }

    public static Plugin getInstance() {
        return instance;
    }

    private void registerCommands() {
        List<String> cmds = new ArrayList<>() {{add("leave");}};
        cmds.addAll(modes);
        for (String command : cmds) {
            Objects.requireNonNull(getCommand(command)).setExecutor(new CommandAliases());
            Objects.requireNonNull(getCommand(command)).setTabCompleter(new TabComplete());
        }
        Objects.requireNonNull(getCommand("rosepvp")).setExecutor(new CommandRosePvP());
        Objects.requireNonNull(getCommand("rosepvp")).setTabCompleter(new TabComplete());
        Objects.requireNonNull(getCommand("menu")).setExecutor(new GUI());
        Objects.requireNonNull(getCommand("menu")).setTabCompleter(new TabComplete());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ListenerEvent(), this);
        getServer().getPluginManager().registerEvents(new GUI(), this);
        getServer().getPluginManager().registerEvents(new Spawn(), this);
        getServer().getPluginManager().registerEvents(new Arena(), this);
//        getServer().getPluginManager().registerEvents(new SDEvent(), this);
    }

    public Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
