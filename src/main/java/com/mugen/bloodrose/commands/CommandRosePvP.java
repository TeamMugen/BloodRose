package com.mugen.bloodrose.commands;

import com.mugen.bloodrose.VariableMaps;
import com.mugen.bloodrose.commands.triggers.*;
import com.mugen.bloodrose.filemanager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

import static com.mugen.bloodrose.ListenerEvent.playerJoin;
import static com.mugen.bloodrose.VariableMaps.PlayerStatus.PLAYING;
import static com.mugen.bloodrose.VariableMaps.StatusArena.*;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.filemanager.PlayerData.*;
import static com.mugen.bloodrose.filemanager.SessionData.setStatusArena;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.getGameToggle;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.setGameToggle;
import static com.mugen.bloodrose.utils.AssistantGame.checkLeaveArena;
import static com.mugen.bloodrose.utils.FastBossbar.removeBossBar;
import static com.mugen.bloodrose.utils.FastTeam.removeTeam;
import static com.mugen.bloodrose.utils.Util.checkStatusArena;
import static com.mugen.bloodrose.utils.Util.sendError;

public class CommandRosePvP implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String a, String[] args) {
        String cmdName = cmd.getName();
        if (!cmdName.equalsIgnoreCase("rosepvp")) return false;

        //プレイヤーのみ実行可
        if (!(sender instanceof Player p)) {
            return sendError((Player) sender, "§cこのコマンドはプレイヤーのみが使用できます。");
        }

        //opじゃなかったら終了
        if (!sender.isOp() && !sender.hasPermission("rose.admin")) {
            return sendError(p, "§cこのコマンドを実行する権限がありません。");
        }

        if (args.length == 0) {
            return sendError(p, "§c引数が足りません。/rose help");
        }

        String subCmd = args[0].toLowerCase();
        String session = null;
        String arena = "";
        String mode = "";

        switch (subCmd) {
            case "status":
                CheckStatus checkStatus = new CheckStatus();
                checkStatus.onCommand(sender, cmd, a, args);
                return true;

            case "team":
                if (!getPlayerStatus(p).equals(PLAYING)) {
                    return sendError(p, "§cゲーム中のみ使用可能です。");
                }
                CheckTeam checkTeam = new CheckTeam();
                checkTeam.onCommand(sender, cmd, a, args);
                return true;

            case "join":
                if (args.length < 2) {
                    return sendError(p, "§c引数が足りません。/rose join <mode>");
                }
                mode = args[1].toLowerCase();
                arena = null;
                if (args.length > 2) {
                    arena = args[2];
                }
                new Join(mode, arena, p);
                return true;

            case "cancel":
                if (args.length < 2) {
                    return sendError(p, "§c引数が足りません。/rose cancel <mode>");
                }
                Player target = Bukkit.getPlayer(args[1]);
                cancel(p, target);
                return true;

            case "arena":
                new Arena().onCommand(sender, cmd, a, args);
                return true;

            case "spawn":
                new Spawn().onCommand(sender, cmd, a, args);
                return true;

            case "reload":
                try {
                    reload(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;

            case "help":
                help(p);
                return true;

            case "gametoggle":
                gameToggle((Player) sender);
                return true;

            case "end":
            case "skip":
                if (args.length > 2) {
                    mode = args[1].toLowerCase();
                    arena = args[2].toLowerCase();
                } else if (getSession(p) != null) {
                    mode = getMode(p);
                    arena = getArena(p);
                }
                skipend(subCmd, mode, arena, p);
                return true;

            case "point":
                new Point().onCommand(sender, cmd, a, args);
                return true;

            default:
                return sendError(p, "§cそのようなサブコマンドは登録されていません。/rose helpを用いて確認してください。");
        }
    }

    private void cancel(Player p, Player target) {
        if (getSession(target) == null) {
            p.sendMessage("§c指定のプレイヤーはアリーナに参加していません。");
            return;
        }
        if (getPlayerStatus(target).equals(PLAYING)) {
            p.sendMessage("§c指定のプレイヤーは既に開始されたアリーナに参加しています。");
            return;
        }
        checkLeaveArena(target);
    }

    private void help(Player p) {
        p.sendMessage("§6§l/rose ");
        p.sendMessage("§e..spawn .. §7: スポーン関連の設定");
        p.sendMessage("§e..arena .. §7: アリーナ関連の設定");
        p.sendMessage("§e..join <mode> §7: 試合参加コマンド");
        p.sendMessage("§e..status [<player>] §7: ステータス確認");
        p.sendMessage("§e..team §7: 所属してるゲームのチームを確認");
        p.sendMessage("§e..reload §7: §4試合中に使用しないでください");
        p.sendMessage("§e..cancel §7: 試合参加予約をキャンセルする");
        p.sendMessage("§e..help §7: ヘルプ確認");
    }

    public static boolean skipend(String subCmd, String mode, String arena, Player p) {
        String session = mode + arena;
        if (session.isEmpty()) {
            return sendError(p, "§c無効なアリーナを指定しないでください。");
        }

        if (!arenas.get(mode).contains(arena)) {
            return sendError(p, "§cアリーナ" + session + "は存在しません。");
        }

        if (subCmd.equals("end")) {
            if (!checkStatusArena(session, GAMING)) {
                return sendError(p, "§cアリーナが試合中のときのみに使用できます。");
            }
            setStatusArena(session, ABORT);
        } else {
            if (!checkStatusArena(session, ANNOUNCE)) {
                return sendError(p, "§c指定されたアリーナは待機状態にありません。");
            }
            setStatusArena(session, SKIP);
        }
        return true;
    }

    private void reload(Player p) throws IOException {
        if (!p.hasPermission("rose.reload")) {
            p.sendMessage("§cコマンドを実行する権限がありません。");
            return;
        }

        for (String session : sessions.keySet()) {
            if (VariableMaps.sessions.containsKey(session)) {
                removeBossBar(session);
                removeTeam(session);
            }
        }

        uuids.clear();
        arenas.clear();
        sessions.clear();
        reserveArena.clear();

        FileManager.reloadConfig();

        for (Player $ : Bukkit.getOnlinePlayers()) {
            playerJoin($);
        }

        p.sendMessage("§7リロードしました！");
    }

    public static void gameToggle(Player p) {
        setGameToggle(!getGameToggle());
        if (getGameToggle()) {
            p.sendMessage("§aゲームの参加を許可しています。");
        } else {
            p.sendMessage("§aゲームの参加を中止しています。");
        }
    }
}
