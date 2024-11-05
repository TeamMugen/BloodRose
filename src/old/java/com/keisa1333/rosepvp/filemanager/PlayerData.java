package com.keisa1333.rosepvp.filemanager;

import com.keisa1333.rosepvp.VariableMaps;
import org.bukkit.entity.Player;

import static com.keisa1333.rosepvp.VariableMaps.playerManager;

public class PlayerData {
    private String mode;
    private String arena;
    private String session;
    private VariableMaps.PlayerStatus status;

    public PlayerData() {
        mode = null;
        arena = null;
        session = null;
        status = VariableMaps.PlayerStatus.UNDEFINED;
    }

    public static String getMode(Player p) {
        return playerManager.get(p).mode;
    }
    public static void setMode(Player p, String mode) {
        playerManager.get(p).mode = mode;
    }

    public static String getArena(Player p) {
        return playerManager.get(p).arena;
    }
    public static void setArena(Player p, String arena) {
        playerManager.get(p).arena = arena;
    }

    public static String getSession(Player p) {
        return playerManager.get(p).session;
    }
    public static void setSession(Player p, String session) {
        playerManager.get(p).session = session;
    }

    public static VariableMaps.PlayerStatus getPlayerStatus(Player p) {
        return playerManager.get(p).status;
    }
    public static void setPlayerStatus(Player p, VariableMaps.PlayerStatus stat) {
        playerManager.get(p).status = stat;
    }
}
