package com.keisa1333.rosepvp.modes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.keisa1333.rosepvp.RosePvP.getInstance;
import static com.keisa1333.rosepvp.VariableMaps.PlayerRank;
import static com.keisa1333.rosepvp.filemanager.SessionData.*;
import static com.keisa1333.rosepvp.filemanager.loader.LangLoader.prefix;
import static com.keisa1333.rosepvp.utils.FastTeam.initializeTeam;
import static com.keisa1333.rosepvp.utils.FastTeam.joinTeam;
import static com.keisa1333.rosepvp.utils.Util.bcToList;

public class FFA extends Mode {

    public FFA(String arena) {
        super("ffa", arena);
    }

    @Override
    void team() {
        initializeTeam(session, ChatColor.YELLOW, true, Team.OptionStatus.NEVER);
        for (Player p : getPlayers(session)) {
            joinTeam(session, p);
        }
        init();
    }

    @Override
    void rank() {
        List<Player> players = getPlayers(session);
        List<Map.Entry<Player, Integer>> kills = new ArrayList<>(getTmpkills(session).entrySet());
        kills.sort((obj1, obj2) -> obj2.getValue().compareTo(obj1.getValue()));

        int n = 0;
        int rank = 0;
        int lastKillCount = -1;
        boolean hasKills = false; // キルが1つもない場合を判定するための変数

        for (Map.Entry<Player, Integer> entry : kills) {
            int currentKillCount = entry.getValue(); // 現在のプレイヤーのキル数

            if (currentKillCount == 0) {
                break; // キル数が0になった時点でループを終了
            }

            hasKills = true; // キル数が0より大きい場合、hasKillsをtrueに設定
            n++;

            if (currentKillCount != lastKillCount) {
                if (n > 3) {
                    break;
                }
                rank = n;
            }

            switch (rank) {
                case 1:
                    setRank(session, entry.getKey(), PlayerRank.FFA1);
                    break;
                case 2:
                    setRank(session, entry.getKey(), PlayerRank.FFA2);
                    break;
                case 3:
                    setRank(session, entry.getKey(), PlayerRank.FFA3);
                    break;
            }
            bcToList(players, prefix + "§f<§7" + session + "§f> §7第%rank%位は%kill%キルの%player%！"
                    .replace("%rank%", String.valueOf(rank))
                    .replace("%kill%", String.valueOf(currentKillCount))
                    .replace("%player%", entry.getKey().getName()));
            lastKillCount = currentKillCount;
        }

        if (!hasKills) {
            bcToList(players, prefix + "§f<§7" + session + "§f> §c殺し合いが行われませんでした。");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                deinit();
            }
        }.runTaskLater(getInstance(), 100);
    }
}

