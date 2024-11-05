package com.keisa1333.rosepvp.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class FastBossbar {

    private static final Map<String, BossBar> bossBars = new HashMap<>();

    public static void createBossBar(String id, String title, BarColor color, BarStyle style, double progress) {
        BossBar bossBar = Bukkit.createBossBar(title, color, style);
        bossBar.setProgress(progress);
        bossBar.setVisible(true);
        bossBars.put(id, bossBar);
    }

    public static void removeBossBar(String id) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBars.remove(id);
            bossBar.setVisible(false);
        }
    }

    public static void addPlayerBossBar(String id, Player player) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    public static void leavePlayerBossBar(String id, Player player) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    public static void setTitle(String id, String title) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.setTitle(title);
        }
    }

    public static void setColor(String id, BarColor color) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.setColor(color);
        }
    }

    public static void setStyle(String id, BarStyle style) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.setStyle(style);
        }
    }

    public static void setProgress(String id, double progress) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.setProgress(progress);
        }
    }

    public static void setVisible(String id, boolean visible) {
        BossBar bossBar = bossBars.get(id);
        if (bossBar != null) {
            bossBar.setVisible(visible);
        }
    }

    public static boolean hasBossBar(String id) {
        return bossBars.containsKey(id);
    }
}