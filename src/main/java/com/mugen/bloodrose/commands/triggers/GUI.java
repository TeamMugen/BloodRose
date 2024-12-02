package com.mugen.bloodrose.commands.triggers;

import com.mugen.bloodrose.BloodRose;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

import static com.mugen.bloodrose.BloodRose.getInstance;
import static com.mugen.bloodrose.VariableMaps.pyml;
import static com.mugen.bloodrose.commands.CommandBloodRose.gameToggle;
import static com.mugen.bloodrose.commands.CommandBloodRose.skipend;
import static com.mugen.bloodrose.commands.triggers.CheckStatus.checkStatus;
import static com.mugen.bloodrose.filemanager.PlayerData.getArena;
import static com.mugen.bloodrose.filemanager.PlayerData.getMode;
import static com.mugen.bloodrose.utils.Util.createItemStack;
import static com.mugen.bloodrose.utils.Util.runTask;
import static org.bukkit.Material.*;

public class GUI implements CommandExecutor, Listener {
    public static Inventory menu;
    public static Inventory menuOp;
    public static void initializeMenu() {
        menu = Bukkit.createInventory(null, 27, "§e§lメインメニュー");

        ItemStack frame = new ItemStack(WHITE_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            menu.setItem(i, frame);
        }
        menu.setItem(2, createItemStack(LANTERN, "§7チームメンバー表示", Collections.singletonList("§9試合中のみ使用可")));
        menu.setItem(3, createItemStack(OAK_SIGN, "§7チャット切替", Collections.singletonList("§9SDモードでのみ使用可")));
        menu.setItem(10, createItemStack(RED_WOOL, "§cFFA (ソロ)"));
        menu.setItem(11, createItemStack(ORANGE_WOOL, "§6TDM (チーム)"));
        menu.setItem(12, createItemStack(YELLOW_WOOL, "§eSD (爆弾)", Collections.singletonList("§9開発中")));
        menu.setItem(13, createItemStack(LIME_WOOL, "§aDOM (陣地)", Collections.singletonList("§9開発中")));
        menu.setItem(14, createItemStack(CYAN_WOOL, "§3CQ (陣地･改)", Collections.singletonList("§9開発中")));
        menu.setItem(15, createItemStack(BLUE_WOOL, "§bINF (感染)", Collections.singletonList("§9企画中")));
        menu.setItem(16, createItemStack(PURPLE_WOOL, "§5??? (未定)", Collections.singletonList("§9企画中")));
        menu.setItem(21, createItemStack(TARGET, "§7射撃訓練場へ"));
        menu.setItem(23, createItemStack(SEA_LANTERN, "§7ハブへ", List.of("§c試合中に実行すると退室してしまいます。")));
        ItemStack link = createItemStack(FLOWER_BANNER_PATTERN, "§7リンク表示");
        ItemMeta linkMeta = link.getItemMeta();
        linkMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        link.setItemMeta(linkMeta);
        menu.setItem(6, link);

        menuOp = Bukkit.createInventory(null, menu.getSize(), "§e§lメインメニュー");
        for (int i = 0; i < menu.getSize(); i++) {
            menuOp.setItem(i, menu.getItem(i));
        }
        menuOp.setItem(9, createItemStack(ARROW, "§7試合スキップ", Arrays.asList("§9待機時間をスキップ", "§cOP限定機能")));
        menuOp.setItem(17, createItemStack(END_ROD, "§7ゲームトグル", Arrays.asList("§9試合参加受け入れを中断", "§cOP限定機能")));
    }

    public static void openGUI(Player p) {
        Map<String, Integer> data = pyml.get(p.getUniqueId());
        List <String> lore = new ArrayList<>();
        if (data != null) {
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
            String rateKill = String.valueOf(death == 0 ? "Infinity" : ((double) Math.round((float) kill / death * 100)) / 100);
            String rateFFA = String.valueOf(allFFA == 0 ? "Infinity" : ((double) Math.round((float) winFFA / allFFA * 100)) / 100);
            String rateTDM = String.valueOf(allTDM == 0 ? "Infinity" : ((double) Math.round((float) winTDM / allTDM * 100)) / 100);
            String rateSD = String.valueOf(allSD == 0 ? "Infinity" : ((double) Math.round((float) winSD / allSD * 100)) / 100);
            String rateDOM = String.valueOf(allDOM == 0 ? "Infinity" : ((double) Math.round((float) winDOM / allDOM * 100)) / 100);
            String rateCQ = String.valueOf(allCQ == 0 ? "Infinity" : ((double) Math.round((float) winCQ / allCQ * 100)) / 100);
            String rateINF = String.valueOf(allINF == 0 ? "Infinity" : ((double) Math.round((float) winINF / allINF * 100)) / 100);


            Economy econ = ((BloodRose) getInstance()).getEconomy();
            if (econ == null) {
                return;
            }
            double balance = econ.getBalance(p);

            lore = Arrays.asList(
                    "§9所持金 §f: " + balance,
                    "§e<All> §f: " + rateKill + "(" + kill + "/" + death + ")",
                    "§e<FFA> §f: " + rateFFA + "(" + winFFA + "/" + allFFA + ")",
                    "§e<TDM> §f: " + rateTDM + "(" + winTDM + "/" + allTDM + ")"
                    );
        }
        ItemStack head = createItemStack(PLAYER_HEAD, "§7" + p.getName() + "のステータス", lore);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(p);
        head.setItemMeta(headMeta);

        Inventory mn;
        if (p.isOp()) {
            mn = menuOp;
        } else {
            mn = menu;
        }
        mn.setItem(5, head);
        p.openInventory(mn);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String inventoryTitle = e.getView().getTitle();
        if (inventoryTitle.equals("§e§lメインメニュー")) {
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null) return;

            switch (clickedItem.getType()) {
                case WHITE_STAINED_GLASS_PANE:
                    break;
                case LANTERN:
//                    checkTeam(p);
                    break;
                case PLAYER_HEAD:
                    checkStatus(p, p);
                    break;
                case FLOWER_BANNER_PATTERN:
                    runTask(() -> Bukkit.dispatchCommand(p, "sendwiki"));
                    runTask(() -> Bukkit.dispatchCommand(p, "rpver check"));
                    break;
                case ARROW:
                    if (p.isOp()) {
                        skipend("skip", getMode(p), getArena(p), p);
                    }
                    break;
                case RED_WOOL:
                    new Join("ffa", null, p);
                    break;
                case ORANGE_WOOL:
                    new Join("tdm", null, p);
                    break;
                case YELLOW_WOOL:
                    new Join("sd", null, p);
                    break;
                case LIME_WOOL:
                    new Join("dom", null, p);
                    break;
                case CYAN_WOOL:
                    new Join("cq", null, p);
                    break;
                case BLUE_WOOL:
                    new Join("inf", null, p);
                    break;
                case PURPLE_WOOL:
                    new Join("???", null, p);
                    break;
                case END_ROD:
                    if (p.isOp()) {
                        gameToggle(p);
                    }
                    break;
                case TARGET:
                    p.performCommand("tppoint shoot");
                    break;
                case SEA_LANTERN:
                    runTask(() -> Bukkit.dispatchCommand(p, "hub"));
                    break;
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String a, String[] args) {
        String command = cmd.getName().toLowerCase();
        if (!command.equals("menu")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cこのコマンドはプレイヤーのみが使用できます。");
            return false;
        }
        Player p = (Player) sender;
        openGUI(p);
        return false;
    }
}
