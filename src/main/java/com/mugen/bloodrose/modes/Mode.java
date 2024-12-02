package com.mugen.bloodrose.modes;

import com.mugen.bloodrose.Score;
import com.mugen.bloodrose.VariableMaps;
import com.mugen.bloodrose.filemanager.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mugen.bloodrose.BloodRose.getInstance;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.commands.triggers.Arena.updateArenaMenu;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.getGlobalSpawn;
import static com.mugen.bloodrose.filemanager.loader.LangLoader.prefix;
import static com.mugen.bloodrose.utils.AssistantGame.*;
import static com.mugen.bloodrose.utils.FastBossbar.*;
import static com.mugen.bloodrose.utils.FastTeam.*;
import static com.mugen.bloodrose.utils.Util.*;

class Mode {
    int RATE_TIME = 20;
    String mode;
    String arena;
    String session;
    Mode(String mode, String arena) {
        this.mode = mode;
        this.arena = arena;
        session = mode + arena;

        team();
    }

    void team() {
        initializeTeam(session + "red", ChatColor.RED, false, Team.OptionStatus.FOR_OTHER_TEAMS);
        initializeTeam(session + "blue", ChatColor.BLUE, false, Team.OptionStatus.FOR_OTHER_TEAMS);

        var players = getPlayers(session);
        var tmpPlayers = new ArrayList<>(players);
        int loop = players.size() / 2;

        //赤チーム選出
        var reds = new ArrayList<Player>();
        for (int i = 0; i < loop; i++) {
            int randomIndexP = new Random().nextInt(tmpPlayers.size());
            Player p = tmpPlayers.get(randomIndexP);

            if (waitList.contains(p)) continue;

            reds.add(p);
            tmpPlayers.remove(p);
            joinTeam(session + "red", p);
        }
        setReds(session, reds);

        //残りから青チーム設定
        var blues = new ArrayList<Player>();
        for (Player p : tmpPlayers) {
            if (waitList.contains(p)) continue;

            blues.add(p);
            joinTeam(session + "blue", p);
        }
        setBlues(session, blues);

        init();
    }

    void init() {
        var players = getPlayers(session);
        for (Player p : players) {
            initPlayerMode(mode, arena, p);
        }

        setStatusArena(session, StatusArena.GAMING);
        updateArenaMenu(mode, arena, true);
        new Score(mode, arena, "setup", null, 0);
        createBossBar(session, "§e§l" + mode.toUpperCase() + "モード開催中！", BarColor.BLUE, BarStyle.SEGMENTED_6, 1.0);

        bcToList(players, prefix + "§f<§7" + session + "§f> §bモード" + mode.toUpperCase() + "、アリーナ" + arena.toUpperCase() + "開始！");
        progress();
    }
    void progress() {
        new BukkitRunnable() {
            boolean bool = false;
            int ticks = 0;
            final int totalSeconds = RATE_TIME * getTime(session);

            @Override
            public void run() {
                ticks++;
                if (ticks % RATE_TIME == 0) {
                    countdown(mode, arena, ticks / RATE_TIME, totalSeconds / RATE_TIME);
                }

                if (bool && ticks >= totalSeconds) {
                    cancel();
                    rank();
                }

                if (!bool && (checkStatusArena(session, StatusArena.ABORT)
                        || ticks >= totalSeconds)) {
                    bool = true;
                    ticks = totalSeconds - (3 * RATE_TIME);
                    setTitle(session, "§b試合終了！");
                    bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §bゲーム終了！");
                }
            }
        }.runTaskTimer(getInstance(), 0L, (1L / RATE_TIME));
    }
    void rank() { }
    void deinit() {
        removeBossBar(session);
        removeTeam(session);
        removeTeam(session + "red");
        removeTeam(session + "blue");
        setStatusArena(session, StatusArena.FINALIZE);

        List<Player> players = getPlayers(session);
        for (Player p : players) {
            giveReward(p);
            initPlayer(p);
            playerManager.put(p, new PlayerData());
            runTask(() -> p.teleport(getGlobalSpawn()));
            new Score(null, null, "setup", p, 0);
        }

        if (session.equals(mode + reserveArena.get(mode))) {
            reserveArena.remove(mode);
        }
        updateArenaMenu(mode, arena, false);
        sessions.remove(session);
    }
}
