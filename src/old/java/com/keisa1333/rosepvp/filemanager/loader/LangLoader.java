package com.keisa1333.rosepvp.filemanager.loader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static com.keisa1333.rosepvp.filemanager.loader.ConfigLoader.getLang;
import static org.bukkit.Bukkit.getLogger;

public class LangLoader {

    public static String prefix;
    public static FileConfiguration lang;
    private static Map<String, String> messages;

    public static void loadLang() throws IOException {
        messages = new HashMap<>();
        String lang = getLang();

        if (lang == null) {
            getLogger().warning("config.lang is not set.");
        }

        String file = "lang_" + lang + ".yml";
        File langFile = new File("plugins/RosePvP", "lang_" + lang + ".yml");
        if (!langFile.exists()) {
            File dataFolder = new File("plugins/RosePvP");
            Path destination = dataFolder.toPath().resolve(file);
            InputStream resourceStream = LangLoader.class.getClassLoader().getResourceAsStream(file);
            Files.copy(resourceStream, destination, StandardCopyOption.REPLACE_EXISTING);

        } else {
            Yaml yaml = new Yaml();
            try (InputStream input = LangLoader.class.getClassLoader().getResourceAsStream(file)) {
                if (input != null) {
                    messages = yaml.load(input);
                    if (messages == null) {
                        getLogger().warning("Failed to load messages from YAML file.");
                    }
                } else {
                    getLogger().warning("lang_" + lang + ".yml not found.");
                }
            }

            LangLoader.lang = YamlConfiguration.loadConfiguration(langFile);
            prefix = getMessage("prefix");
        }
    }

    public static String getMessage(String key) {
        if (key.equals("prefix")) {
            return messages.getOrDefault(key, "");
        }
        return messages.getOrDefault(key, "")
                .replace("%prefix%", prefix);
    }
}