package com.mugen.bloodrose;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.List;

import static com.mugen.bloodrose.VariableMaps.StatusArena.GAMING;
import static com.mugen.bloodrose.VariableMaps.boards;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.utils.Util.checkStatusArena;

public class Score {
    private final String mode;
    private final String arena;
    private final String session;
    private final String order;
    private final Player player;
    private final List<Player> players;
    private final int timeRest;

    public Score(String mode, String arena, String order, Player player, int timeRest) {
        this.mode = mode;
        this.arena = arena;
        session = mode + arena;
        this.order = order;
        this.player = player;
        this.players = getPlayers(session);
        this.timeRest = timeRest;

        scoreOfBranch();
    }

    void scoreOfBranch() {
        switch (order.toLowerCase()) {
            case "setup", "delete":
                if (boards.get(player) != null && boards.get(player).isDeleted()) {
                    FastBoard board = boards.get(player);
                    board.delete();
                    boards.remove(player);
                }
                if (order.equalsIgnoreCase("delete")) return;

                if (checkStatusArena(session, GAMING)) {
                    scorePlayingSetup();
                } else if (getStatusArena(session) == null) {
                    scoreLobbySetup();
                }
                break;

            case "update":
                if (checkStatusArena(session, GAMING)) {
                    scorePlayingUpdate();
                }
                break;

            case "count":
                String sidebarMessage = (timeRest % 2 == 0)
                        ? "§c≫ §7残り時間: §b" + timeRest
                        : "§c≫ §7残り時間: §3" + timeRest;

                for (Player p : players) {
                    FastBoard board = boards.get(p);
                    if (board != null) {
                        if (mode.equalsIgnoreCase("ffa")) {
                            board.updateLine(1, sidebarMessage);
                        } else {
                            board.updateLine(2, sidebarMessage);
                        }
                    }
                }
                break;

            default:
                scoreLobbyUpdate(order);
        }
    }

    void scoreLobbySetup() {
        if (player != null) {
            var board = new FastBoard(player);
            board.updateTitle("§b§lMugen §8§l>>> §c§lGuR");
            board.updateLines("§7§m |                     | ",
                    "§b§lYOUR STATS",
                    "§c≫ §7状態: §bぶらぶら散歩",
                    "§7§m |                     | ");
            boards.put(player, board);
        }
    }

    void scoreLobbyUpdate(String order) {
        var board = boards.get(player);

        if (order.equalsIgnoreCase("waiting")) {
            board.updateLines("§7§m |                     | ",
                    "§b§lYOUR STATS",
                    "§c≫ §7状態: §b試合待機中",
                    "§c≫ §7予約ﾓｰﾄﾞ: §b" + mode.toUpperCase(),
                    "§c≫ §7予約ｱﾘｰﾅ: §b" + arena.toUpperCase(),
                    "§7§m |                     | ");
        } else if (order.equalsIgnoreCase("spawn")) {
            board.updateLines("§7§m |                     | ",
                    "§b§lYOUR STATS",
                    "§c≫ §7状態: §bスポーン設定中",
                    "§c≫ §7設定ﾓｰﾄﾞ: §b" + mode.toUpperCase(),
                    "§c≫ §7設定ｱﾘｰﾅ: §b" + arena.toUpperCase(),
                    "§7§m |                     | ");
        }
        boards.put(player, board);
    }

    void scorePlayingSetup() {
        if (player != null) {
            playingSetup(mode, arena, player);
            return;
        }

        for (Player p : players) {
            playingSetup(mode, arena, p);
        }
    }

    private static void playingSetup(String mode, String arena, Player p) {
        String session = mode + arena;
        var board = new FastBoard(p);

        if (mode.equalsIgnoreCase("ffa")) {
            board.updateTitle("§8§lMODE: §c§l" + mode.toUpperCase());
            board.updateLine(0, "§7§m |                     | ");
            board.updateLine(1, "§c≫ §7残り時間: §b開始前");
            board.updateLine(2, "§c≫ §7モード: §b" + mode.toUpperCase());
            board.updateLine(3, "§c≫ §7マップ: §b" + arena.toUpperCase());
            board.updateLine(4, "§c≫ §7あなたのキル数: §b0");

        } else {
            board.updateTitle("§8§lMODE: §c§l" + mode.toUpperCase());
            board.updateLine(0, "§7§m |                     | ");
            board.updateLine(2, "§c≫ §7残り時間: §b開始前");
            board.updateLine(3, "§c≫ §7モード: §b" + mode.toUpperCase());
            board.updateLine(4, "§c≫ §7マップ: §b" + arena.toUpperCase());
            board.updateLine(5, "§c≫ §7あなたのキル数: §b0");
        }

        switch (mode.toLowerCase()) {
            case "ffa":
                board.updateLine(5, "§7§m |                     | ");
                break;

            case "tdm":
                if (getReds(session).contains(p)) {
                    board.updateLine(1, "§cあなたは赤チームです！");
                } else if (getBlues(session).contains(p)) {
                    board.updateLine(1, "§9あなたは青チームです！");
                }
                board.updateLine(6, "§c≫ §c◆§7赤チームのキル数: §b0");
                board.updateLine(7, "§c≫ §9◆§7青チームのキル数: §b0");
                board.updateLine(8, "§7§m |                     | ");
                break;
//
//                case "sd":
//                    if (getReds(session).contains(p)) {
//                        board.updateLine(1, "§cあなたは赤チームです！");
//                    } else if (getBlues(session).contains(p)) {
//                        board.updateLine(1, "§9あなたは青チームです！");
//                    }
//                    board.updateLine(6, "§c≫ §7ステータス: §c§l開始前");
//                    board.updateLine(7, "§c≫ §7あなたの勝利数: §b0");
//                    board.updateLine(8, "§7§m |                     | ");
//                    break;
//
//                case "dom":
//                    if (getReds(session).contains(p)) {
//                        board.updateLine(1, "§cあなたは赤チームです！");
//                    } else if (getBlues(session).contains(p)) {
//                        board.updateLine(1, "§9あなたは青チームです！");
//                    }
//                    board.updateLine(6, "§c≫ §f◆§7" + getPoints(session).get(0) + ": §f ||||||||||||||||||||||||||||||||||||||||");
//                    board.updateLine(7, "§c≫ §f◆§7" + getPoints(session).get(1) + ": §f ||||||||||||||||||||||||||||||||||||||||");
//                    board.updateLine(8, "§c≫ §f◆§7" + getPoints(session).get(2) + ": §f ||||||||||||||||||||||||||||||||||||||||");
//                    board.updateLine(9, "§c≫ §c◆§7赤チーム: §b0");
//                    board.updateLine(10, "§c≫ §9◆§7青チーム: §b0");
//                    board.updateLine(11, "§7§m |                     | ");
//                    break;
        }
        boards.put(p, board);
    }

    void scorePlayingUpdate() {
        if (player != null) {
            var board = boards.get(player);
            int kill = getTmpkills(session).get(player);
            if (mode.equalsIgnoreCase("ffa")) {
                board.updateLine(4, "§c≫ §7あなたのキル数: §b" + kill);
            } else {
                board.updateLine(5, "§c≫ §7あなたのキル数: §b" + kill);
            }
            boards.put(player, board);
        }

        switch (mode.toLowerCase()) {
            case "tdm":
                int killRed = getRedP(session);
                int killBlue = getBlueP(session);
                for (Player p : players) {
                    var board = boards.get(p);
                    board.updateLine(6, "§c≫ §c◆§7赤チームのキル数: §b" + killRed);
                    board.updateLine(7, "§c≫ §9◆§7青チームのキル数: §b" + killBlue);
                    boards.put(p, board);
                }
                break;
//
//            case "sd":
//                for (Player p : getReds(session)) {
//                    board = boards.get(p);
//                    board.updateLine(7, "§c≫ §7あなたの勝利数: §b" + getRedPoint(session));
//                }
//
//                for (Player p : getBlues(session)) {
//                    board = boards.get(p);
//                    board.updateLine(7, "§c≫ §7あなたの勝利数: §b" + getBluePoint(session));
//                }
//
//                for (Player p : players) {
//                    board = boards.get(p);
//                    switch (getBombStatus(session)) {
//                        case having:
//                            board.updateLine(6, "§c≫ §7ステータス: §b未設置");
//                            break;
//                        case place:
//                            setTitle(session, "§c§l爆弾が設置されました");
//                            board.updateLine(6, "§c≫ §7ステータス: §b爆弾設置済み");
//                            break;
//                        case pick:
//                            setTitle(session, "§3爆弾解除成功");
//                            board = boards.get(p);
//                            board.updateLine(6, "§c≫ §7ステータス: §b爆弾解除成功");
//                            break;
//                        case deadSearcher:
//                            setTitle(session, "§c防御チーム殲滅されました");
//                            board.updateLine(6, "§c≫ §7ステータス: §b防御チーム全滅");
//                            break;
//                        case deadDestroyer:
//                            setTitle(session, "§3攻撃チーム殲滅されました");
//                            board.updateLine(6, "§c≫ §7ステータス: §b攻撃チーム全滅");
//                            break;
//                    }
//                }
//                break;
        }
    }

//    public static void changeProgressScore(String session) {
//        for (Player p : getPlayers(session)) {
//            FastBoard board = boards.get(p);
//            if (board != null) {
//                board.updateLine(9,   "§c≫ §c◆§7赤チーム: §b" + pointOfDisplay.get(session + "red"));
//                board.updateLine(10,   "§c≫ §9◆§7青チーム: §b" + pointOfDisplay.get(session + "blue"));
//
//                int i = 0;
//                for (String point : getPoints(session).keySet()) {
//                    Double currentPoint = getProgressDominate(session, point);
//                    double count = Math.max(-200, Math.min(200, currentPoint)) + 200;
//
//                    int TOTAL_LENGTH = 40;
//                    int filledLength = (int) Math.max(0, (double) count / 400 * TOTAL_LENGTH);
//                    int unfilledLength = TOTAL_LENGTH - filledLength;
//
//                    String displayBar = "§c|".repeat(filledLength) + "§9|".repeat(unfilledLength);
//                    if (currentPoint >= MAX_POINT_DOM) {
//                        board.updateLine(6 + i, "§c≫ §c◆§7" + point + ": §b" + displayBar + " §7/ §b" + POINT_OF_WIN);
//                    } else if (currentPoint <= -MAX_POINT_DOM) {
//                        board.updateLine(6 + i, "§c≫ §9◆§7" + point + ": §b" + displayBar + " §7/ §b" + POINT_OF_WIN);
//                    } else {
//                        board.updateLine(6 + i, "§c≫ §f◆§7" + point + ": §b" + displayBar + " §7/ §b" + POINT_OF_WIN);
//                    }
//                    i++;
//                }
//            }
//        }
//    }
}
