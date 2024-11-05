package com.keisa1333.rosepvp.modes;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static com.keisa1333.rosepvp.RosePvP.getInstance;
import static com.keisa1333.rosepvp.VariableMaps.PlayerRank;
import static com.keisa1333.rosepvp.filemanager.SessionData.*;
import static com.keisa1333.rosepvp.filemanager.loader.LangLoader.prefix;
import static com.keisa1333.rosepvp.utils.Util.bcToList;

public class TDM extends Mode {
    public TDM(String arena) {
        super("tdm", arena);
    }

    @Override
     void rank() {
        String msg;
        int redP = getRedP(session);
        int blueP = getBlueP(session);

        if (blueP == 0 && redP == 0) {
            msg = "§c殺し合いが行われませんでした。";
        } else if (blueP > redP) {
            msg = "§9青チームの勝利です！";
            for (Player p : getBlues(session)) {
                setRank(session, p, PlayerRank.TDM);
            }
        } else if (redP > blueP) {
            msg = "§c赤チームの勝利です！";
            for (Player p : getReds(session)) {
                setRank(session, p, PlayerRank.TDM);
            }
        } else {
            msg = "§e引き分けです";
        }

        bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> " + msg);

        new BukkitRunnable() {
            @Override
            public void run() {
                deinit();
            }
        }.runTaskLater(getInstance(), 100);
    }
}
