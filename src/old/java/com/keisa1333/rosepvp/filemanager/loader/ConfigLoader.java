package com.keisa1333.rosepvp.filemanager.loader;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.keisa1333.rosepvp.filemanager.FileManager.config;
import static com.keisa1333.rosepvp.filemanager.FileManager.saveConfig;

public class ConfigLoader {
    private static String lang;
    public static List<String> modes;
    public static List<String> modeDevelop;
    private static Location globalSpawn;
    private static Boolean gameToggle;
    private static Location respawnSpawn;
    private static Integer spawnTry = 5;
    private static Integer spawnRadius = 5;
    private static Integer rewardKill = 100;
    private static Map<String, Integer> rewards = new HashMap<>();
    private static Integer waitAnnounce = 30;

    public static void loadConfig() {
        lang = config.getString("lang");
        gameToggle = config.getBoolean("enable_join_game");
        waitAnnounce = config.getInt("time_wait_game");

        spawnTry = config.getInt("amount_try_find_spawn");
        spawnRadius = config.getInt("find_spawn_radius");

        rewardKill = config.getInt("reward_kill");
        ConfigurationSection section = config.getConfigurationSection("rewards");
        if (section != null) {
            Map<String, Integer> configMap = new HashMap<>();
            for (String key : section.getKeys(false)) {
                configMap.put(key, (Integer) section.get(key));
            }
            rewards = configMap;
        }

        modes = config.getStringList("mode_list");
        modeDevelop = config.getStringList("mode_develop");

        globalSpawn = config.getLocation("location_global");
        respawnSpawn = config.getLocation("location_respawn");
    }

    public static Boolean getGameToggle() {
        return gameToggle;
    }

    public static void setGameToggle(Boolean bool) {
        gameToggle = bool;
        config.set("enable_join_game", gameToggle);
        saveConfig();
    }

    public static String getLang() {
        return lang;
    }

    public static Location getGlobalSpawn() {
        return globalSpawn;
    }

    public static void setGlobalSpawn(Location loc) {
        globalSpawn = loc;
        config.set("location_global", globalSpawn);
        saveConfig();
    }

    public static Location getRespawnSpawn() {
        return respawnSpawn;
    }

    public static void setRespawnSpawn(Location loc) {
        // arenaList を設定ファイルに保存
        respawnSpawn = loc;
        config.set("location_respawn", respawnSpawn);
        saveConfig(); // 設定ファイルを保存
    }

    public static Integer getSpawnRadius() {
        return spawnRadius;
    }
    public static Integer getSpawnTry() {
        return spawnTry;
    }
    public static Integer getWaitAnnounce() {
        return waitAnnounce;
    }
    public static Integer getRewardKill() {
        return rewardKill;
    }
    public static Map<String, Integer> getRewards() {
        return rewards;
    }
}
