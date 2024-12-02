package com.mugen.bloodrose.commands.triggers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.loadArenas;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.modes;
import static com.mugen.bloodrose.utils.Util.*;
import static org.bukkit.Material.*;

public class Arena implements CommandExecutor, Listener {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String a, String[] args) {
        if (args.length < 2) {
            return sendError((Player) sender, "§c引数が足りません。/rose arena (check|remove|info <mode> <arena>|list|gui)");
        }

        Player p = (Player) sender;
        String subCmd = args[1].toLowerCase();
        switch (subCmd) {
            case "list":
            case "status":
                list(p);
                return true;

            case "gui":
                gui(p);
                return true;
        }

        if (args.length < 4) {
            return sendError(p, "§c引数が足りません。");
        }

        String mode = args[2].toLowerCase();
        String arena = args[3].toLowerCase();
        String session = mode + arena;

        switch (subCmd) {
            // /rose arena remove <mode> <arena>
            case "del":
            case "delete":
            case "remove":
                arenaDelete(mode, arena, p);
                return true;

            // /rose arena check <mode> <arena>
            case "check":
                List<String> list = new ArrayList<>(arenaChecker(mode, arena));

                if (!list.isEmpty()) {
                    p.sendMessage("§6アリーナ" + session + "判定結果: ");
                    for (String msg : list) {
                        p.sendMessage(msg);
                    }

                } else {
                    p.sendMessage("§6アリーナ" + session + "判定結果: §7問題なし");
                }
                return true;

            case "info":
                info(p, args);

            default:
                return sendError(p, "§c引数が違います。/rose arena (list|remove <mode> <arena>)");
        }
    }

    private void list(Player p) {
        p.sendMessage("§6アリーナ一覧");

        for (String mode : modes) {
            if (arenas.get(mode) == null || arenas.get(mode).isEmpty()) {
                p.sendMessage("§e" + mode.toUpperCase() + ": §7未設定");

            } else {
                StringBuilder msg = new StringBuilder("§e" + mode.toUpperCase() + ": ");
                for (String arena : arenas.get(mode)) {
                    String session = mode + arena;
                    if (sessions.containsKey(session)) {
                        msg.append("§a" + arena + " ");
                    } else {
                        msg.append("§7" + arena + " ");
                    }
                }
                p.sendMessage(msg.toString());
            }
        }
    }


    // /rose spawn list <mode> <arena>
    private void info(Player p, String[] args) {
        if (args.length < 4) {
            p.sendMessage("§c引数が足りねえぞ！拓也！");
            return;
        }

        String mode = args[2].toLowerCase();
        String arena = args[3].toLowerCase();
        String session = mode + arena;

        //アリーナがあるか
        if (!arenas.get(mode).contains(arena)) {
            p.sendMessage("§c" + session + "アリーナは存在しません。");
            return;
        }
        if (isBoot(mode, arena)) {
            p.sendMessage("§c" + session + "アリーナは使用中のためその操作はできません。");
            return;
        }
        loadArenas(mode, arena);

        switch (mode) {
            case "ffa":
            case "tdm":
            case "dom":
                List<Location> spawnList = getSpawns(session);

                p.sendMessage("§6アリーナ%name%スポーンの設定状況"
                        .replace("%name%", session));
                if (spawnList == null) {
                    p.sendMessage("§e%name%アリーナのスポーン: §7未設定"
                            .replace("%name%", session));
                } else {
                    int size = spawnList.size();
                    p.sendMessage("§e%name%アリーナのスポーン数: §7%size%(0-%max%)"
                            .replace("%name%", session)
                            .replace("%size%", String.valueOf(size))
                            .replace("%max%", String.valueOf(size - 1)));
                }
//                if (mode.equals("dom")) {
//                    if (getPoints(name) == null) {
//                        return true;
//                    }
//                    Map<String, Location> points = new HashMap<>(getPoints(name));
//                    StringBuilder pointSB = new StringBuilder("§f");
//                    int i = 0;
//                    for (String point : points.keySet()) {
//                        i++;
//                        if (i == points.keySet().size()) {
//                            pointSB.append(point);
//                        } else {
//                            pointSB.append(point).append(", ");
//                        }
//                    }
//                    String pointStr = pointSB.toString();
//                    p.sendMessage("§a登録地点 : " + pointStr);
//                }
                break;

//            case "sd":
//                Location searcher = getLocSD(session, SEARCHER);
//                Location destroyer = getLocSD(session, DESTROYER);
//
//                p.sendMessage("§6アリーナ" + session + "スポーンの設定状況");
//
//                if (searcher == null) {
//                    p.sendMessage("§eSearcherスポーン: §7未設定");
//                } else {
//                    p.sendMessage("§eSearcherスポーン: §7設定済み");
//                }
//
//                if (destroyer == null) {
//                    p.sendMessage("§eDestroyerスポーン: §7未設定");
//                } else {
//                    p.sendMessage("§eDestroyerスポーン: §7設定済み");
//                }
//                break;
            default:
                p.sendMessage("§cモードが違います。");
                break;
        }
        sessions.remove(session);
    }

    public static Inventory[] listGUI;
    public static void loadArenaMenu() {
        int row = 0;
        List<String> arenaList = new ArrayList<>();
        for (String mode : modes) {
            for (String arena : arenas.get(mode)) {
                arenaList.add(mode + arena);
            }
            if (!arenas.get(mode).isEmpty()) {
                int n = arenas.get(mode).size() / 8 + 1;
                row += n;
            }
        }
        int page = row / 5 + 1;
        listGUI = new Inventory[page];

        for (int i = 0; i < page; i++) {
            int currentPage = i + 1;
            Inventory inv = Bukkit.createInventory(null, 54, "§e§lアリーナ一覧 §7§l[" + (currentPage) + "/" + page + "]");

            inv.setItem(49, createItemStack(BARRIER, "§7閉じる"));
            if (page > currentPage) {
                inv.setItem(50, createItemStack(ARROW, "§7次ページへ", Collections.singletonList(String.valueOf(currentPage + 1))));
            }
            if (currentPage > 1) {
                inv.setItem(48, createItemStack(ARROW, "§7前ページへ", Collections.singletonList(String.valueOf(currentPage - 1))));
            }
            listGUI[i] = inv;
        }

        Map<String, Boolean> map = new HashMap<>();
        int currentPage = 0;
        int beforeSlot = 0;
        int currentSlot = 0;
        Inventory inv = listGUI[currentPage];
        String arena;
        for (String m : modes) {
            map.put(m, false);
            for (String session : arenaList) {
                if (session.startsWith(m)) {
                    if (currentSlot - beforeSlot == 44) {
                        listGUI[currentPage] = inv;

                        currentPage++;
                        inv = listGUI[currentPage];
                        beforeSlot = currentPage * 45;
                        inv.setItem(0, createItemStack(BLACK_WOOL, "§9§l" + m.toUpperCase() + "のアリーナ一覧"));
                    }

                    if (currentSlot % 9 == 0 && !map.get(m)) {
                        inv.setItem(currentSlot - beforeSlot, createItemStack(BLACK_WOOL, "§9§l" + m.toUpperCase() + "のアリーナ一覧"));
                        map.put(m, true);

                    } else if ((currentSlot + 1) % 9 == 0 && map.get(m)) {
                        currentSlot++;
                    }

                    currentSlot++;
                    arena = session.substring(m.length());
                    inv.setItem(currentSlot - beforeSlot, createItemStack(RED_WOOL, "§c§lアリーナ" + arena.toUpperCase()));
                } else if (map.get(m)) {
                    break;
                }
            }
            currentSlot = (currentSlot / 9 + 1) * 9;
        }
        listGUI[currentPage] = inv;
    }

    public static void updateArenaMenu(String mode, String arena, boolean isStart) {
        String session = mode + arena;
        boolean bool = false;
        int page = 0;
        for (Inventory inv : listGUI) {
            int slot = 0;
            for (ItemStack item : inv.getContents()) {
                if (item == null) {
                    slot++;
                    continue;
                }
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    slot++;
                    continue;
                }
                String name = meta.getDisplayName();
                if (name.contains(mode.toUpperCase())) bool = true;
                if (name.contains(arena.toUpperCase()) && bool) {
                    if (isStart) {
                        if (reserveArena.get(mode) != null && reserveArena.get(mode).equalsIgnoreCase(arena)) {
                            inv.setItem(slot, createItemStack(LIME_WOOL, "§a§lアリーナ" + arena.toUpperCase(), Arrays.asList("§9参加人数: " + getPlayers(session).size() + "/" + getMaxP(session), "§9状態: " + getStatusArena(session), "§9参加可能")));
                        } else {
                            inv.setItem(slot, createItemStack(LIME_WOOL, "§a§lアリーナ" + arena.toUpperCase(), Arrays.asList("§9参加人数: " + getPlayers(session).size() + "/" + getMaxP(session), "§9状態: " + getStatusArena(session), "§c参加不可能")));
                        }
                        listGUI[page] = inv;
                        return;
                    } else {
                        inv.setItem(slot, createItemStack(RED_WOOL, "§c§lアリーナ" + arena.toUpperCase()));
                        listGUI[page] = inv;
                        return;
                    }
                }
                slot++;
            }
            page++;
        }
    }


    private void gui(Player p) {
        p.openInventory(listGUI[0]);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String inventoryTitle = e.getView().getTitle();
        if (!inventoryTitle.contains("§e§lアリーナ一覧")) return;

        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null) return;
        ItemMeta meta;

        switch (clickedItem.getType()) {
            case ARROW:
                p.closeInventory();

                meta = clickedItem.getItemMeta();
                assert meta != null;
                assert meta.getLore() != null;
                String intStr = meta.getLore().get(0);
                int i = Integer.parseInt(intStr) - 1;

                p.openInventory(listGUI[i]);
                break;
            case LIME_WOOL:
                meta = clickedItem.getItemMeta();
                assert meta != null;
                String name = meta.getDisplayName().toLowerCase();

                String mode = "";
                String arena = "";
                for (ItemStack item : e.getInventory().getContents()) {
                    if (item == null || item.getItemMeta() == null) continue;
                    String n = item.getItemMeta().getDisplayName().toLowerCase();
                    if (modes.contains(n.substring(4, 7))) {
                        mode = n.substring(4, 7);
                    }
                    if (item.equals(clickedItem)) {
                        arena = name.substring(8);
                        break;
                    }
                }

                p.closeInventory();
                new Join(mode, arena, (Player) e.getWhoClicked());
                break;
            case RED_WOOL:
                p.sendMessage("§c指定のアリーナは現在参加できません。緑色のアイコンをクリックしてください。");
                break;
            case BARRIER:
                p.closeInventory();
                break;
            default:
                break;
        }
    }
}