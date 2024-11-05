package com.keisa1333.rosepvp;

import com.keisa1333.rosepvp.filemanager.PlayerData;
import com.keisa1333.rosepvp.filemanager.SessionData;
import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class VariableMaps {

    public static Map<String, List<String>> arenas;
    public static List<UUID> uuids = new ArrayList<>();
    public static Map<UUID, Map<String, Integer>> pyml = new HashMap<>();
    public static Map<Player, FastBoard> boards = new HashMap<>();
    public static Map<String, String> reserveArena = new HashMap<>();
    public static Map<Player, PlayerData> playerManager = new HashMap<>();

    public static Map<String, SessionData> sessions = new HashMap<>();
    public static Map<String, Entity> tntMap = new HashMap<>();
    public static Map<Entity, GlowingEntities> geMap = new HashMap<>();
    public static List<Player> waitList = new ArrayList<>();

    public enum Mode {
        FFA, TDM, SD, DOM, CQ
    }

    public enum SDTeams {
        DESTROYER, SEARCHER
    }

    public enum PlayerStatus {
        UNDEFINED, RESERVING, PLAYING, SPAWN_SET
    }

    public enum StatusArena {
        UNDEFINED, ANNOUNCE, SKIP, FULL,
        INIT, GAMING, ABORT, FINALIZE
    }

    public enum StatusBomb {
        UNDEFINED,
        DEAD_SEARCHER, DEAD_DESTROYER,
        PLACE, PICK,
        HAVING
    }

    public enum PlayerRank {
        UNDEFINED,
        FFA1, FFA2, FFA3,
        TDM, SD, DOM, CQ
    }
}

