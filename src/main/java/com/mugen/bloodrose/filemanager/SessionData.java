package com.mugen.bloodrose.filemanager;

import com.mugen.bloodrose.VariableMaps;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.StatusArena.UNDEFINED;
import static com.mugen.bloodrose.VariableMaps.sessions;

public class SessionData {
    private VariableMaps.StatusArena status;
    private List<Player> players;
    private List<Location> spawns;
    private final Map<Player, Integer> tmpkills;
    private final Map<Player, Integer> killstreak;
    private final Map<Player, VariableMaps.PlayerRank> rank;
    private int needP;
    private int maxP;
    private int time;


    List<Player> reds;
    List<Player> blues;
    int redpoint;
    int bluepoint;


    int round;
    VariableMaps.StatusBomb statusBomb;
    Map<Player, Boolean> teamChat;
    List<Player> deadReds;
    List<Player> deadBlues;
    Player bomber;
    VariableMaps.SDTeams roleOfRed;
    VariableMaps.SDTeams roleOfBlue;
    int countOfWin;
    int timeBomb;
    int countPlace;
    int countPick;
    String locDestroy;
    String locSearch;
    String locBombDrop;
    List<String> bombPoints;
    Map<String, Location> points;

//    SD.InformOfBomb informBomb;


    public SessionData(String mode) {
        this.status = UNDEFINED;
        this.players = new ArrayList<>();
        this.spawns = new ArrayList<>();
        this.tmpkills = new HashMap<>();
        this.killstreak = new HashMap<>();
        this.rank = new HashMap<>();
        this.needP = 2;
        this.maxP = 12;
        this.time = 180;

        if (!mode.equalsIgnoreCase("ffa")) {
            this.reds = new ArrayList<>();
            this.blues = new ArrayList<>();
            this.redpoint = 0;
            this.bluepoint = 0;
        }

        switch (mode.toLowerCase()) {
            case "sd":
                this.deadReds = new ArrayList<>();
                this.deadBlues = new ArrayList<>();
                this.round = 1;
                this.statusBomb = VariableMaps.StatusBomb.UNDEFINED;
                this.teamChat = new HashMap<>();
                this.bomber = null;
                this.roleOfBlue = SEARCHER;
                this.roleOfRed = DESTROYER;
                this.countOfWin = 5;
                this.countPlace = 20;
                this.countPick = 30;
                this.locBombDrop = null;
                this.locDestroy = null;
                this.locSearch = null;
                this.bombPoints = new ArrayList<>();
//                this.informBomb = new SD.InformOfBomb();
            case "dom":
            case "cq":
        }
    }


    public static int getNeedP(String session) {
        return sessions.get(session).needP;
    }

    public static void setNeedP(String session, int amount) {
        sessions.get(session).needP = amount;
    }


    public static int getMaxP(String session) {
        return sessions.get(session).maxP;
    }

    public static void setMaxP(String session, int amount) {
        sessions.get(session).maxP = amount;
    }


    public static int getTime(String session) {
        return sessions.get(session).time;
    }

    public static void setTime(String session, int second) {
        sessions.get(session).time = second;
    }


    public static VariableMaps.StatusArena getStatusArena(String session) {
        if (sessions.get(session) == null) {
            return null;
        }
        return sessions.get(session).status;
    }

    public static void setStatusArena(String session, VariableMaps.StatusArena status) {
        if (sessions.get(session) != null) {
            sessions.get(session).status = status;
        }
    }


    public static List<Player> getPlayers(String session) {
        if (sessions.get(session) == null) {
            return new ArrayList<>();
        }
        return sessions.get(session).players;
    }

    public static void setPlayers(String session, List<Player> players) {
        sessions.get(session).players = players;

    }


    public static List<Location> getSpawns(String session) {
        if (sessions.get(session) == null) {
            return new ArrayList<>();
        }
        return sessions.get(session).spawns;
    }

    public static void setSpawns(String session, List<Location> spawns) {
        sessions.get(session).spawns = spawns;
    }


    public static Map<Player, Integer> getTmpkills(String session) {
        if (sessions.get(session) == null) {
            new HashMap<>();
        }
        return sessions.get(session).tmpkills;
    }

    public static void setTmpkills(String session, Player p, int tmpkills) {
        sessions.get(session).tmpkills.put(p, tmpkills);
    }


    public static Map<Player, Integer> getKillstreak(String session) {
        if (sessions.get(session) == null) {
            new HashMap<>();
        }
        return sessions.get(session).killstreak;
    }

    public static void setKillstreak(String session, Player p, int killstreak) {
        sessions.get(session).killstreak.put(p, killstreak);
    }


    public static Map<Player, VariableMaps.PlayerRank> getRank(String session) {
        if (sessions.get(session) == null) {
            new HashMap<>();
        }
        return sessions.get(session).rank;
    }

    public static void setRank(String session, Player p, VariableMaps.PlayerRank rank) {
        sessions.get(session).rank.put(p, rank);
    }


    public static List<Player> getReds(String session) {
        return sessions.get(session).reds;
    }

    public static void setReds(String session, List<Player> reds) {
        sessions.get(session).reds = reds;
    }


    public static List<Player> getBlues(String session) {
        return sessions.get(session).blues;
    }

    public static void setBlues(String session, List<Player> blues) {
        sessions.get(session).blues = blues;
    }


    public static int getRedP(String session) {
        return sessions.get(session).redpoint;
    }

    public static void setRedP(String session, int redpoint) {
        sessions.get(session).redpoint = redpoint;
    }


    public static int getBlueP(String session) {
        return sessions.get(session).bluepoint;
    }

    public static void setBlueP(String session, int bluepoint) {
        sessions.get(session).bluepoint = bluepoint;
    }


    public static int getRound(String session) {
        return sessions.get(session).round;
    }

    public static void setRound(String session, int round) {
        sessions.get(session).round = round;
    }


    public static VariableMaps.StatusBomb getStatusBomb(String session) {
        return sessions.get(session).statusBomb;
    }

    public static void setStatusBomb(String session, VariableMaps.StatusBomb status) {
        sessions.get(session).statusBomb = status;
    }


    public static List<Player> getDeads(String session, String team) {
        if (team.equals("red")) {
            return sessions.get(session).deadReds;
        } else {
            return sessions.get(session).deadBlues;
        }
    }

    public static void setDeads(String session, String team, List<Player> deads) {
        if (team.equals("red")) {
            sessions.get(session).deadReds = deads;
        } else {
            sessions.get(session).deadBlues = deads;
        }
    }


    public static Player getBomber(String session) {
        return sessions.get(session).bomber;
    }

    public static void setBomber(String session, Player p) {
        sessions.get(session).bomber = p;
    }


    public static boolean getTeamChat(String session, Player p) {
        return sessions.get(session).teamChat.get(p);
    }

    public static void setTeamChat(String session, Player p, Boolean bool) {
        sessions.get(session).teamChat.put(p, bool);
    }


    public static VariableMaps.SDTeams getRole(String session, String team) {
        if (team.equals("red")) {
            return sessions.get(session).roleOfRed;
        } else {
            return sessions.get(session).roleOfBlue;
        }
    }

    public static void setRole(String session, String team, VariableMaps.SDTeams role) {
        if (team.equals("red")) {
            sessions.get(session).roleOfRed = role;
        } else {
            sessions.get(session).roleOfBlue = role;
        }
    }


    public static int getCountOfWin(String session) {
        return sessions.get(session).countOfWin;
    }

    public static void setCountOfWin(String session, int count) {
        sessions.get(session).countOfWin = count;
    }


    public static int getTimeBomb(String session) {
        return sessions.get(session).timeBomb;
    }

    public static void setTimeBomb(String session, int time) {
        sessions.get(session).timeBomb = time;
    }


    public static int getCountPlace(String session) {
        return sessions.get(session).countPlace;
    }

    public static void setCountPlace(String session, int count) {
        sessions.get(session).countPlace = count;
    }


    public static int getCountPick(String session) {
        return sessions.get(session).countPick;
    }

    public static void setCountPick(String session, int count) {
        sessions.get(session).countPick = count;
    }


    public static String getLocSD(String session, VariableMaps.SDTeams team) {
        if (team.equals(DESTROYER)) {
            return sessions.get(session).locDestroy;
        } else {
            return sessions.get(session).locSearch;
        }
    }
    public static void setLocSD(String session, VariableMaps.SDTeams team, String point) {
        if (team.equals(DESTROYER)) {
            sessions.get(session).locDestroy = point;
        } else {
            sessions.get(session).locSearch = point;
        }
    }


//    public static SD.InformOfBomb getInformBomb(String session) {
//        return sessions.get(session).informBomb;
//    }
//    public static void setInformBomb(String session, SD.InformOfBomb inform) {
//        sessions.get(session).informBomb = inform;
//    }


    public static String getLocBomb(String session) {
        return sessions.get(session).locBombDrop;
    }
    public static void setLocBomb(String session, String point) {
        sessions.get(session).locBombDrop = point;
    }


    public static List<String> getBombPoints(String session) {
        return sessions.get(session).bombPoints;
    }
    public static void setBombPoints(String session, List<String> points) {
        sessions.get(session).bombPoints = points;
    }

    public static Map<String, Location> getPoints(String session) {
        return sessions.get(session).points;
    }

    public static Location getLocPoint(String session, String point) {
        return sessions.get(session).points.get(point);
    }
    public static void setPoints(String session, Map<String, Location> points) {
        sessions.get(session).points = points;
    }
    public static void setLocPoint(String session, String point, Location loc) {
        sessions.get(session).points.put(point, loc);
    }
}