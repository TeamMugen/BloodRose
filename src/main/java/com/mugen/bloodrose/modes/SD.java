package com.mugen.bloodrose.modes;

import com.mugen.bloodrose.Score;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

import static com.mugen.bloodrose.RosePvP.getInstance;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.StatusArena.ABORT;
import static com.mugen.bloodrose.VariableMaps.StatusBomb.*;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.LangLoader.prefix;
import static com.mugen.bloodrose.utils.AssistantGame.*;
import static com.mugen.bloodrose.utils.FastBossbar.setProgress;
import static com.mugen.bloodrose.utils.FastTeam.joinTeam;
import static com.mugen.bloodrose.utils.Util.bcToList;
import static com.mugen.bloodrose.utils.Util.checkStatusArena;

public class SD extends Mode {

    public static class InformOfBomb {
        String point;
        Location location;
    }
    public SD(String arena) {
        super("sd", arena);
    }

    private void initRound() {
        var players = getPlayers(session);
        for (Player p : players) {
            if (waitList.contains(p)) {
                midJoin(session, p);
                waitList.remove(p);
            }
        }

        //爆弾設置
//                if (getBelong(p) == null) return false;
//                String name = getBelong(p);
//                if (!name.startsWith("sd")) return false;
//                if (getBomber(name) != null) {
//                    sender.sendMessage("§c既にボンバーが決まっています。");
//                    return false;
//                }
//                if (getStatusArena(name) != null && getStatusArena(name).equals(goon)) {
//                    if (getSDPlayers(name, destroyer).contains(p)) {
//                        giveTNT(p, name);
//                    }
//                }

        int n = getRound(session) - 1;
        //1,2, 6,7,8
        if (Math.floor((double) n / 3) % 2 == 0) {
            if (Math.floorMod(n, 3) == 0) {
                setRole(session, "red", DESTROYER);
                setRole(session, "blue", SEARCHER);
            }
            //3,4,5, 9,10
        } else {
            if (Math.floorMod(n, 3) == 0) {
                setRole(session, "red", SEARCHER);
                setRole(session, "blue", DESTROYER);
            }
        }

        setDeads(session, "red", new ArrayList<>());
        setDeads(session, "blue", new ArrayList<>());
        setBomber(session, null);
        setStatusBomb(session, UNDEFINED);
        getInformBomb(session).point = null;
        getInformBomb(session).location = null;

        setProgress(session, 1.0);
        new Score(mode, arena, "playing", null, 0);

        //開始合図
        bcToList(players, "§bラウンド" + getRound(session) + "開始！");
        bcToList(players, prefix + "§f<§7" + session + "§f> §7ゲーム中はチームチャットが有効になります！有効/無効を切り替えるには §fchat §7とチャットを送信してください。");
        for (Player p : players) {
            initPlayerMode(mode, arena, p);
        }

        for (Player p : getSDPlayers(session, DESTROYER)) {
            p.teleport(getPoints(session).get(getLocSD(session, DESTROYER)));
        }
        for (Player p : getSDPlayers(session, SEARCHER)) {
            p.teleport(getPoints(session).get(getLocSD(session, SEARCHER)));
        }
        for (Player p : getReds(session)) {
            joinTeam(session + "red", p);
        }
        for (Player p : getBlues(session)) {
            joinTeam(session + "blue", p);
        }
    }

    void progress() {
        initRound();
        new BukkitRunnable() {
            boolean isBombing = false;
            int ticks = 0;
            int totalSeconds = RATE_TIME * getTime(session);

            @Override
            public void run() {
                ticks++;
                if (ticks % RATE_TIME == 0) {
                    countdown(mode, arena, ticks / RATE_TIME, totalSeconds / RATE_TIME);
                }

                if (checkStatusArena(session, ABORT) || isDeadTeam()) {
                    cancel();
                    endRound(null, null);
                }

                if (ticks >= totalSeconds) {
                    cancel();
                    if (isBombing) {
                        endRound(DESTROYER, "§a§l爆弾が爆発しました！ドカーン！");
                    }
                    endRound(SEARCHER, "§a§l防衛に成功しました！");
                }

                if (getStatusBomb(session).equals(PICK)) {
                    cancel();
                    endRound(SEARCHER, "§a§l爆弾が解除されました！");
                }

                if (getStatusBomb(session).equals(PLACE) && !isBombing) {
                    bcToList(getSDPlayers(session, DESTROYER), "§a爆弾が設置されました！爆弾が爆発するまで防衛しよう");
                    bcToList(getSDPlayers(session, SEARCHER), "§a爆弾が設置されました！爆発を防ぐために解除しよう");

                    isBombing = true;
                    totalSeconds = RATE_TIME * getTimeBomb(session);
                    ticks++;
                }
            }
        }.runTaskTimer(getInstance(), 0, (1L / RATE_TIME));
    }

    private void endRound(SDTeams team, String msg) {
        var players = getPlayers(session);
        if (team != null && msg != null) {
            if (getSDTeam(session, team).equalsIgnoreCase("red")) {
                setRedP(session, getRedP(session) + 1);
            } else if (getSDTeam(session, team).equalsIgnoreCase("blue")) {
                setBlueP(session, getBlueP(session) + 1);
            }
            bcToList(players, msg);
        }
        removeDroppedBomb(session);

        new Score(mode, arena, "playing", null, 3);
        bcToList(players, "§bラウンド" + getRound(session) + "終了！");

        setRound(session, getRound(session) + 1);

        if (Math.max(getRedP(session), getBlueP(session)) >= getRound(session)) {
            new Score(mode, arena, "playing", null, 0);
            rank();
        } else {
            progress();
        }
    }

    @Override
    void rank() {
        var players = getPlayers(session);

        bcToList(players, "§a青チーム" + getBlueP(session) + "点、赤チーム" + getRedP(session) + "点で...");

        if (getBlueP(session) > getRedP(session)) {
            bcToList(getBlues(session), prefix + "§f<§7" + session + "§f> §aあなたのチームの勝ちです！");
            bcToList(getReds(session), prefix + "§f<§7" + session + "§f> §cあなたのチームの負けです");
            for (Player p : getBlues(session)) {
                setRank(session, p, PlayerRank.SD);
            }

        } else {
            bcToList(getReds(session), prefix + "§f<§7" + session + "§f> §aあなたのチームの勝ちです！");
            bcToList(getBlues(session), prefix + "§f<§7" + session + "§f> §cあなたのチームの負けです");
            for (Player p : getReds(session)) {
                setRank(session, p, PlayerRank.SD);
            }

        }

        new BukkitRunnable() {
            @Override
            public void run() {
                deinit();
            }
        }.runTaskLater(getInstance(), 100);
    }






    private boolean isDeadTeam() {
        if (getStatusBomb(session).equals(DEAD_DESTROYER)) {
            endRound(SEARCHER, "§a§l攻撃チームが殲滅されました！");
            return true;
        }
        if (getStatusBomb(session).equals(DEAD_SEARCHER)) {
            endRound(DESTROYER, "§a§l防御チームが殲滅されました！");
            return true;
        }
        return false;
    }
}
