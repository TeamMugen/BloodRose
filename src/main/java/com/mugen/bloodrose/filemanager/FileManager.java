package com.mugen.bloodrose.filemanager;

import com.mugen.bloodrose.BloodRose;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static com.mugen.bloodrose.commands.triggers.Arena.loadArenaMenu;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.loadArenasFromFolder;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.loadConfig;
import static com.mugen.bloodrose.filemanager.loader.LangLoader.loadLang;
import static com.mugen.bloodrose.filemanager.loader.PlayerLoader.loadPlayers;

public class FileManager {
    private static BloodRose instance;
    private static File configFile;
    public static FileConfiguration config;

    public FileManager(BloodRose rosePvP) {
        instance = rosePvP;
    }

    public static void reloadConfig() throws IOException {
        loadFiles();
    }

    public static void loadFiles() throws IOException {
        instance.saveDefaultConfig();

        configFile = new File(instance.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        loadConfig();
        loadPlayers();
        loadLang();
        loadArenasFromFolder();
        loadArenaMenu();
    }

    public static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}