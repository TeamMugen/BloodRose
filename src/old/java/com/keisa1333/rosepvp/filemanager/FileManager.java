package com.keisa1333.rosepvp.filemanager;

import com.keisa1333.rosepvp.RosePvP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static com.keisa1333.rosepvp.commands.triggers.Arena.loadArenaMenu;
import static com.keisa1333.rosepvp.filemanager.loader.ArenaLoader.loadArenasFromFolder;
import static com.keisa1333.rosepvp.filemanager.loader.ConfigLoader.loadConfig;
import static com.keisa1333.rosepvp.filemanager.loader.LangLoader.loadLang;
import static com.keisa1333.rosepvp.filemanager.loader.PlayerLoader.loadPlayers;

public class FileManager {
    private static RosePvP instance;
    private static File configFile;
    public static FileConfiguration config;

    public FileManager(RosePvP rosePvP) {
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