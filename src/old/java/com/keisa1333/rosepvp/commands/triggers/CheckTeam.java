package com.keisa1333.rosepvp.commands.triggers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

import static com.keisa1333.rosepvp.VariableMaps.PlayerStatus.PLAYING;
import static com.keisa1333.rosepvp.filemanager.PlayerData.*;
import static com.keisa1333.rosepvp.filemanager.SessionData.*;

public class CheckTeam implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player player = (Player) sender;
        if (!getPlayerStatus(player).equals(PLAYING)) {
            sender.sendMessage("§c試合中のみ使用できます。");
            return false;
        }
        checkTeam(player);
        return true;
    }

    public static void checkTeam(Player player) {
        if (getSession(player) == null) return;
        var session = getSession(player);
        var mode = getMode(player);

        switch (mode.toLowerCase()) {
            case "ffa":
                var players = getPlayers(session);
                var playersStr = players.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));

                player.sendMessage("§6FFAのプレイヤー一覧§f: " + playersStr);
                break;

            case "tdm":
            case "dom":
                var reds = getReds(session);
                var blues = getBlues(session);

                var redsStr = reds.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));
                var bluesStr = blues.stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));

                if (reds.contains(player)) {
                    player.sendMessage("§6あなたは§c赤チーム§6です");
                } else if (blues.contains(player)) {
                    player.sendMessage("§6あなたは§b青チーム§6です");
                }

                player.sendMessage("§c赤チーム§f: " + redsStr);
                player.sendMessage("§b青チーム§f: " + bluesStr);
                break;

//            case "sd":
//                String dList = getSDPlayers(name, destroyer).stream()
//                        .map(Player::getName)
//                        .collect(Collectors.joining(", "));
//                String sList = getSDPlayers(name, searcher).stream()
//                        .map(Player::getName)
//                        .collect(Collectors.joining(", "));
//
//                if (getSDPlayers(name, destroyer).contains(player)) {
//                    if (getSDTeam(name, destroyer).equalsIgnoreCase("red")) {
//                        player.sendMessage("§6あなたは§c攻撃チーム§6です");
//                    } else {
//                        player.sendMessage("§6あなたは§3攻撃チーム§6です");
//                    }
//                } else if (getSDPlayers(name, searcher).contains(player)) {
//                    if (getSDTeam(name, searcher).equalsIgnoreCase("red")) {
//                        player.sendMessage("§6あなたは§c防衛チーム§6です");
//                    } else {
//                        player.sendMessage("§6あなたは§3防衛チーム§6です");
//                    }
//                }
//                player.sendMessage("§e攻撃側のプレイヤー一覧§f: %destroy_list%"
//                        .replace("%destroy_list%", dList));
//                player.sendMessage("§e防衛側のプレイヤー一覧§f: %search_list%"
//                        .replace("%search_list%", sList));
//                break;
        }
    }
}