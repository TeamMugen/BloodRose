package com.keisa1333.rosepvp.commands;

import com.keisa1333.rosepvp.commands.triggers.Join;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.keisa1333.rosepvp.ListenerEvent.playerJoin;
import static com.keisa1333.rosepvp.filemanager.PlayerData.getSession;
import static com.keisa1333.rosepvp.filemanager.loader.ConfigLoader.modes;
import static com.keisa1333.rosepvp.utils.AssistantGame.checkLeaveArena;

public class CommandAliases implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String a, String[] args) {
        String cmdName = cmd.getName();
        if (!modes.contains(cmdName)
                && !cmdName.equalsIgnoreCase("leave")) return false;


        //プレイヤーのみ実行可
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみが使用できます。");
            return false;
        }

        //leave専用処理
        if (cmdName.equalsIgnoreCase("leave")) {
            if (getSession(p) == null) {
                p.sendMessage("§c現在試合に参加していないため使用できませんでした。");
                return false;
            }
            checkLeaveArena(p);
            playerJoin(p);
//        checkTeamEmpty(session, p);
            return true;
        }

        //各mode専用処理
        // 例:/ffa /tdm
        for (String mode : modes) {
            //入力コマンドと登録モードが一致してるか
            if (mode.equalsIgnoreCase(cmdName)) {

                String arena = null;
                // /ffa shoothouse
                if (args.length > 0) {
                    if (p.isOp()) {
                        arena = args[0];
                    }
                }
                new Join(mode, arena, p);
                return true;
            }
        }

        return false;
    }
}
