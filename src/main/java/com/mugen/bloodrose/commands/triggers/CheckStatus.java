package com.mugen.bloodrose.commands.triggers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.mugen.bloodrose.VariableMaps.pyml;
import static com.mugen.bloodrose.VariableMaps.uuids;


public class CheckStatus implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player target;

        if (args.length == 2 && sender.isOp()) {
            target = Bukkit.getPlayer(args[1]);

        } else if (sender instanceof Player) {
            target = (Player) sender;

        } else {
            sender.sendMessage("§c指定されたプレイヤーは存在しません。");
            return false;
        }

        if (target == null) {
            sender.sendMessage("§c現在、その対象のステータスは見れません。");
            return false;
        } else if (!uuids.contains(target.getUniqueId())) {
            sender.sendMessage("§c不明なエラーが発生しました(ファイルなし)");
            return false;
        }
        checkStatus(target, sender);
        return true;
    }

    public static void checkStatus(Player target, CommandSender sender) {
        Map<String, Integer> data = pyml.get(target.getUniqueId());
        if (data == null) {
            sender.sendMessage("§c指定されたプレイヤーは存在しません。");
        }

        int kill = data.get("kill");
        int death = data.get("death");
        int winFFA = data.get("win_ffa");
        int winTDM = data.get("win_tdm");
        int winSD = data.get("win_sd");
        int winDOM = data.get("win_dom");
        int winCQ = data.get("win_cq");
        int winINF = data.get("win_inf");
        int loseFFA = data.get("lose_ffa");
        int loseTDM = data.get("lose_tdm");
        int loseSD = data.get("lose_sd");
        int loseDOM = data.get("lose_dom");
        int loseCQ = data.get("lose_cq");
        int loseINF = data.get("lose_inf");
        int allFFA = winFFA + loseFFA;
        int allTDM = winFFA + loseTDM;
        int allSD = winFFA + loseSD;
        int allDOM = winFFA + loseDOM;
        int allCQ = winFFA + loseCQ;
        int allINF = winFFA + loseINF;

        sender.sendMessage("§6%player%'s status"
                .replace("%player%", target.getName()));
//        sender.sendMessage("§ekill§f:%kill%"
//                .replace("%kill%", String.valueOf(kill)));
//        sender.sendMessage("§edeath§f:%death%"
//                .replace("%death%", String.valueOf(death)));
//        sender.sendMessage("§eK/D§f:%kd%(%kill%/%death%)"
//                .replace("%kd%", String.valueOf(death == 0 ? "Infinity" : ((double) Math.round((float) kill / death * 100)) / 100))
//                .replace("%kill%", String.valueOf(kill))
//                .replace("%death%", String.valueOf(death)));
        String rateKill = String.valueOf(death == 0 ? "Infinity" : ((double) Math.round((float) kill / death * 100)) / 100);
        sender.sendMessage("§e<All> キル数§f:" + kill + ", §eデス数§f:" + death + ", §eKD率§f:" + rateKill);
        String rateFFA = String.valueOf(allFFA == 0 ? "Infinity" : ((double) Math.round((float) winFFA / allFFA * 100)) / 100);
        sender.sendMessage("§e<FFA> 勝利数§f:" + winFFA + ", §e敗北数§f:" + loseFFA + ", §e勝率§f:" + rateFFA);
        String rateTDM = String.valueOf(allTDM == 0 ? "Infinity" : ((double) Math.round((float) winTDM / allTDM * 100)) / 100);
        sender.sendMessage("§e<TDM> 勝利数§f:" + winTDM + ", §e敗北数§f:" + loseTDM + ", §e勝率§f:" + rateTDM);
//        String rateSD = String.valueOf(loseSD == 0 ? "Infinity" : ((double) Math.round((float) winSD / allSD * 100)) / 100);
//        sender.sendMessage("§e<SD> 勝利数§f:" + winSD + ", §e敗北数§f:" + loseSD + ", §e勝率§f:" + rateSD);
//        String rateDOM = String.valueOf(loseDOM == 0 ? "Infinity" : ((double) Math.round((float) winDOM / allDOM * 100)) / 100);
//        sender.sendMessage("§e<DOM> 勝利数§f:" + winDOM + ", §e敗北数§f:" + loseDOM + ", §e勝率§f:" + rateDOM);
//        String rateCQ = String.valueOf(loseCQ == 0 ? "Infinity" : ((double) Math.round((float) winCQ / allCQ * 100)) / 100);
//        sender.sendMessage("§e<C Q> 勝利数&f:" + winCQ + ", §e敗北数§f:" + loseCQ + ", §e勝率§f:" + rateCQ);
//        String rateINF = String.valueOf(loseINF == 0 ? "Infinity" : ((double) Math.round((float) winINF / allINF * 100)) / 100);
//        sender.sendMessage("§e<INF> 勝利数&f:" + winINF + ", §e敗北数§f:" + loseINF + ", §e勝率§f:" + rateINF);
    }
}