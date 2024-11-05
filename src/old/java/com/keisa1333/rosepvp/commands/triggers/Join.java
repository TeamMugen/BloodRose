package com.keisa1333.rosepvp.commands.triggers;

import com.keisa1333.rosepvp.Score;
import com.keisa1333.rosepvp.modes.FFA;
import com.keisa1333.rosepvp.modes.SD;
import com.keisa1333.rosepvp.modes.TDM;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

import static com.keisa1333.rosepvp.VariableMaps.*;
import static com.keisa1333.rosepvp.VariableMaps.PlayerStatus.PLAYING;
import static com.keisa1333.rosepvp.VariableMaps.StatusArena.*;
import static com.keisa1333.rosepvp.commands.triggers.Arena.updateArenaMenu;
import static com.keisa1333.rosepvp.filemanager.PlayerData.*;
import static com.keisa1333.rosepvp.filemanager.SessionData.*;
import static com.keisa1333.rosepvp.filemanager.loader.ArenaLoader.loadArenas;
import static com.keisa1333.rosepvp.filemanager.loader.ConfigLoader.*;
import static com.keisa1333.rosepvp.filemanager.loader.LangLoader.prefix;
import static com.keisa1333.rosepvp.utils.AssistantGame.*;
import static com.keisa1333.rosepvp.utils.FastTeam.joinTeam;
import static com.keisa1333.rosepvp.utils.Util.*;

public class Join {
    private String mode;
    private String arena;
    private String session;
    private Player p;

    public Join(String mode, String arena, Player p) {
        if (getPlayerStatus(p).equals(PLAYING)) {
            p.sendMessage("§c試合参加中は実行できません。");
            return;
        }
        //プレイヤーが既にアリーナに参加しているか
        if (getSession(p) != null) {
            if (getMode(p).equalsIgnoreCase(mode)) {
                checkLeaveArena(p);
                return;
            }
        }

        this.mode = mode;
        this.arena = arena;
        this.p = p;
        session = mode + this.arena;

        if (!isModeAllowed() || !arenaSelect()) {
            deinit();
            return;
        }

        register();

        if (checkStatusArena(session, ANNOUNCE)) {
            deinit();
            return;
        }

        progress();
    }

    private void deinit() {
        mode = null;
        arena = null;
        p = null;
        session = null;
    }

    private boolean isModeAllowed() {
        if (getPlayerStatus(p).equals(PLAYING)) {
            p.sendMessage("§c既にゲームに参加中です。");
            return false;
        }

        if (!modes.contains(mode)) {
            p.sendMessage("§c存在しないゲームモードです");
            return false;
        }

        if (modeDevelop.contains(mode)) {
            p.sendMessage("§cこのモードは開発中です");
            return false;
        }

        if (!getGameToggle()) {
            p.sendMessage("§c現在、ゲームは閉じられています。");
            return false;
        }

        if (p.isOp()) {
            if (arena != null) {
                if (!arenas.get(mode).contains(arena)) {
                    p.sendMessage("§cそのアリーナ名は存在しません。");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean arenaSelect() {
        //予約アリーナがある場合
        if (reserveArena.get(mode) != null) {
            //アリーナが指定されてる場合
            if (arena != null) {
                //指定アリーナと予約アリーナが一致した場合
                if (reserveArena.get(mode).equalsIgnoreCase(arena)) {
                    arena =  reserveArena.get(mode);
                    return true;
                //指定アリーナと予約アリーナが一致しない場合、キャンセル
                }
                p.sendMessage("§c指定されたモードに予約済みのアリーナがあるため、処理を完了できませんでした。");
                return false;
            }
            //アリーナが指定されてない場合
            arena = reserveArena.get(mode);
            session = mode + arena;
            return true;
        }


        //予約アリーナがなく、指定アリーナがある場合
        if (arena != null) {
            if (arenaChecker(mode, arena).isEmpty()) {
                session = mode + arena;

                loadArenas(mode, arena);
                reserveArena.put(mode, arena);
                return true;
            }
            p.sendMessage("§c登録に不備があります。/rose arena checkで確認してください。");
            return false;
        }


        //指定モードのアリーナが存在するか
        if (arenas.get(mode).isEmpty()) {
            p.sendMessage("§cアリーナが登録されていません。");
            return false;
        }

        List<String> availableArenas = new ArrayList<>(arenas.get(mode));
        while (!availableArenas.isEmpty()) {
            int index = new Random().nextInt(availableArenas.size());
            String selectedArena = availableArenas.get(index);

            List<String> list = arenaChecker(mode, selectedArena);
            if (!list.isEmpty()) {
                availableArenas.remove(index);
            } else {
                arena = selectedArena;
                session = mode + arena;

                loadArenas(mode, arena);
                reserveArena.put(mode, arena);
                return true;
            }
        }
        p.sendMessage("§c指定モードのすべてのアリーナの登録に不備があります。");
        return false;
    }

    private void register() {
        List <Player> players = new ArrayList<>(getPlayers(session));
        players.add(p);
        setPlayers(session, players);

        updateArenaMenu(mode, arena, true);

        if (players.size() >= getMaxP(session)) {
            setStatusArena(session, FULL);
            reserveArena.remove(mode);
        }

        setSession(p, session);
        setArena(p, arena);
        setMode(p, mode);
        setPlayerStatus(p, PlayerStatus.RESERVING);
        p.sendMessage("§a" + session + "アリーナに参加予約しました。");

        new Score(mode, arena, "waiting", p, 0);

        if (checkStatusArena(session, GAMING)) {
            switch (mode.toLowerCase()) {
                case "ffa":
                    joinTeam(session, p);
                    bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §e" + p.getName() + "§eが途中参加しました。");
                    break;

                case "tdm":
                case "dom":
                case "cq":
                    midJoin(session, p);
                    break;

                case "sd":
                    waitList.add(p);
                    bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §e" + p.getName() + "§eが途中参加しました。\n次ラウンドから参加します。");
                    break;
            }
            initPlayerMode(mode, arena, p);
            new Score(mode, arena, "setup", p, 0);
        }
    }

    private void progress() {
        if (getPlayers(session).size() < getNeedP(session)) return;
        if (checkStatusArena(session, GAMING)) return;

        setStatusArena(session, ANNOUNCE);
        final int totalSeconds = getWaitAnnounce();

        int needP = getNeedP(session);
        int maxP = getMaxP(session);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int ticks = 0;

            @Override
            public void run() {
                if (getPlayers(session).size() >= maxP) {
                    reserveArena.remove(mode);
                    announceMessage("full", totalSeconds);
                    timer.cancel();
                }

                //スタート時のアナウンス
                if (ticks == 0) {
                    announceMessage("start", totalSeconds);
                }

                //半分経過時のアナウンス
                if (ticks == totalSeconds / 2) {
                    announceMessage("half", totalSeconds);
                }

                ticks++;

                //スキップ時、時間終了時のアナウンス
                if (checkStatusArena(session, SKIP) || ticks >= totalSeconds) {
                    if (getPlayers(session).isEmpty()) {
                        announceMessage("empty", totalSeconds);
                        timer.cancel();

                    } else if (getPlayers(session).size() < needP) {
                        announceMessage("cancel", totalSeconds);
                        setStatusArena(session, INIT);
                        timer.cancel();

                    } else {
                        announceMessage("end", totalSeconds);
                        initializeGame();
                        timer.cancel();
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000); // 0ミリ秒から始めて、1000ミリ秒（＝1秒）ごとに実行
    }

    private void initializeGame() {
        switch (mode) {
            case "ffa" -> new FFA(arena);
            case "tdm" -> new TDM(arena);
            case "sd" -> new SD(arena);
        }
    }

    private void announceMessage(String status, int time) {
        String msg = "";
        status = status.toLowerCase();

        msg = switch (status) {
            case "half" -> prefix + "§a" + session + "アリーナが" + time / 2 + "秒後に開催されます！参加する方は §6/" + mode + " §aより！";
            case "start" -> prefix + "§a" + session + "アリーナが" + time + "秒後に開催されます！参加する方は §6/" + mode + " §aより！";
            case "cancel" -> prefix + "§a欠員が出たため、" + session + "の開催が延期されました。 §6/" + mode + "§aから参加しよう！";
            case "end" -> prefix + "§a" + session + "アリーナの募集を締め切りました。";
            case "full" -> prefix + "§a最大参加人数に達したため、" + session + "の受付が終了しました。";
            case "empty" -> prefix + "§a参加プレイヤーがいなくなったので、" + session + "の開催が中止されました。";
            default -> msg;
        };
        Bukkit.broadcastMessage(msg);
    }
}
