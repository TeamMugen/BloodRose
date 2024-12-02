package com.mugen.bloodrose.utils;

import com.mugen.bloodrose.VariableMaps;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mugen.bloodrose.BloodRose.getInstance;
import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.VariableMaps.StatusArena.ABORT;
import static com.mugen.bloodrose.VariableMaps.StatusArena.GAMING;
import static com.mugen.bloodrose.commands.triggers.Arena.loadArenaMenu;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.createArenaYaml;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.loadArenas;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.modes;

public class Util {
    public static void devMsg(String msg) {
        Bukkit.broadcastMessage(msg);
    }

    public static boolean isBoot(String mode, String arena) {
        String session = mode + arena;
        if (!sessions.containsKey(session)) {
            loadArenas(mode, arena);
            return false;
        }
        return true;
    }
    public static void bcToList(List<Player> list, String msg) {
        for (Player p : list) {
            p.sendMessage(msg);
        }
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(msg);
    }

    public static void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(getInstance(), task);
    }

    public static void openGUI(String gui, Player p) {
        runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "am open " + gui + " " + p.getName()));
    }

    public static boolean checkStatusArena(String session, StatusArena status) {
        if (modes.contains(session) || getStatusArena(session) == null) {
            return false;
        } else {
            return Objects.equals(getStatusArena(session), status);
        }
    }

    public static boolean sendError(Player p, String msg) {
        p.sendMessage(msg);
        return false;
    }

    public static ItemStack createItemStack(Material material, String name) {
        return createItemStack(material, name, null);
    }
    public static ItemStack createItemStack(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void initPlayer(Player p) {
        runTask(() -> p.setGameMode(GameMode.ADVENTURE));
        p.setFoodLevel(20);
        p.setHealth(20.0);
        p.getInventory().clear();
        for (PotionEffect e : p.getActivePotionEffects()) {
            runTask(() -> p.removePotionEffect(e.getType()));
        }
    }

    public static void initializeArena(String mode, String arena, Player p) {
        String session = mode + arena;
        if (!arenas.containsKey(mode)) {
            arenas.put(mode, new ArrayList<>());
        }
        if (arenas.get(mode).contains(arena)) return;

        createArenaYaml(mode, session);
        arenas.get(mode).add(arena);
        loadArenaMenu();

        p.sendMessage("§7アリーナ§e%name%§7を登録しました。"
                .replace("%name%", session));
    }

    public static void arenaDelete(String mode, String arena, Player p) {
        String session = mode + arena;

        if (!modes.contains(mode)) return;
        if (isBoot(mode, arena)) return;

        arenas.get(mode).remove(arena);
        sessions.put(session, null);

        File fileToDelete = new File("plugins/BloodRose/arenas/" + session + ".yml");
        if (fileToDelete.delete()) {
            Bukkit.getLogger().info("[BloodRose] " + session + ".ymlを削除しました。");
            p.sendMessage("§7アリーナ" + session + "の設定を削除しました。");
        } else {
            Bukkit.getLogger().info("[BloodRose] " + session + ".ymlの削除に失敗しました。");
            p.sendMessage("§cアリーナ" + session + "の設定を削除できませんでした。");
        }
    }

    public static List<String> arenaChecker(String mode, String arena) {
        String session = mode + arena;
        List<String> list = new ArrayList<>();

        if (!modes.contains(mode)) {
            list.add("§c-モードが違います。");
        } else if (!VariableMaps.arenas.get(mode).contains(arena)) {
            list.add("§c-アリーナ" + session + "がありません。");
        }

        if (!list.isEmpty()) {
            return list;
        }

        boolean bool = isBoot(mode, arena);

        if (sessions.containsKey(session)) {
            list.add("§c-管理者がアリーナを編集しているため使用できません。");
        }

        if (checkStatusArena(session, GAMING)
                || checkStatusArena(session, ABORT)) {
            list.add("§c-アリーナ%session%は使用されています。"
                    .replace("%session%", session));
        }

        for (String m : modes) {
            String s = m + arena;
            if (m.equalsIgnoreCase(mode)) {
                continue;
            }
            if (reserveArena.get(m) != null && reserveArena.get(m).equalsIgnoreCase(arena)) {
                list.add("§c-アリーナ%session%と同名のアリーナ%dupsession%が予約されています。"
                        .replace("%session%", session)
                        .replace("%dupsession%", s));
            } else if (checkStatusArena(session, GAMING)
                    || checkStatusArena(session, ABORT)) {
                list.add("§c-アリーナ%session%と同名のアリーナ%dupsession%が使用されています。"
                        .replace("%session%", session)
                        .replace("%dupsession%", s));
            }
        }

        if (mode.equalsIgnoreCase("sd")) {
            if (getLocSD(session, DESTROYER) == null) {
                list.add("§cdefenderスポーンがありません。");
            }
            if (getLocSD(session, SEARCHER) == null) {
                list.add("§csearcherスポーンがありません。");
            }
            if (getBombPoints(session) == null) {
                list.add("§cbombPointsが設定されていません。");
            } else if (getBombPoints(session).size() < 2) {
                list.add("§cbombPointsが足りません。(必要数:2)");
            } else if (getBombPoints(session).size() > 2) {
                list.add("§cbombPointsが多すぎます。(必要数:2)");
            }
            if (getLocBomb(session) == null) {
                list.add("§c爆弾のドロップ地が設定されていません。");
            }

            List<String> points = new ArrayList<>(getBombPoints(session));
            if (points.contains(getLocBomb(session))) {
                list.add("§c爆弾のドロップ地に関する重複があります。");
            }
            points.add(getLocBomb(session));
            if (points.contains(getLocSD(session, DESTROYER))) {
                list.add("§cデストロイヤーのスポーン地に関する重複があります。");
            }
            points.add(getLocSD(session, DESTROYER));
            if (points.contains(getLocSD(session, SEARCHER))) {
                list.add("§cサーチャーのスポーン地に関する重複があります。");
            }

            points = (List<String>) getPoints(session).keySet();
            if (!points.contains(getLocBomb(session))) {
                list.add("§c爆弾のドロップ地のポイント名が無効です。");
            } else if (!points.contains(getLocSD(session, DESTROYER))) {
                list.add("§cデストロイヤーのスポーン地のポイント名が無効です。");
            } else if (!points.contains(getLocSD(session, SEARCHER))) {
                list.add("§cサーチャーのスポーン地のポイント名が無効です。");
            }
            for (String str : getBombPoints(session)) {
                if (!points.contains(str)) {
                    list.add("§cbombPointsのポイント名が無効です。");
                }
            }

        } else {
            if (getSpawns(session).isEmpty()) {
                list.add("§c-アリーナ%session%にスポーン設定がありません。"
                        .replace("%session%", session));
            }

//            if (mode.equalsIgnoreCase("dom")) {
//                if (SessionDOM.getPoints(session).entrySet().isEmpty()) {
//                    list.add("§c占領地点が登録されていません。");
//                } else if (SessionDOM.getPoints(session).entrySet().size() < 3) {
//                    list.add("§c占領地点が3つありません。");
//                } else if (SessionDOM.getPoints(session).entrySet().size() > 3) {
//                    list.add("§c占領地点が3つより多くあります");
//                }
//            } else if (mode.equalsIgnoreCase("cq")) {
//                if (SessionCQ.getPoints(session).entrySet().isEmpty()) {
//                    list.add("§c占領地点が登録されていません。");
//                } else if (SessionCQ.getPoints(session).entrySet().size() < 5) {
//                    list.add("§c占領地点が5つありません。");
//                } else if (SessionCQ.getPoints(session).entrySet().size() > 5) {
//                    list.add("§c占領地点が5つより多くあります");
//                }
//
//                if (getCPRed(session) == null) {
//                    list.add("§c赤チームのデフォルトポイントがありません。");
//                }
//                if (SessionCQ.getPoints(session).containsKey(getCPRed(session))) {
//                    list.add("§c赤チームのデフォルトポイントが地点登録されていません。");
//                }
//
//                if (getCPBlue(session) == null) {
//                    list.add("§c青チームのデフォルトポイントがありません。");
//                }
//                if (SessionCQ.getPoints(session).containsKey(getCPBlue(session))) {
//                    list.add("§c青チームのデフォルトポイントが地点登録されていません。");
//                }
//
//                for (String point : SessionCQ.getPoints(session).keySet()) {
//                    if (getSpawnsCP(session, point) == null) {
//                        list.add("§cポイント" + point + "にスポーン地点が登録されていません。");
//                    }
//                }
//            }
        }

        if (!bool) {
            sessions.remove(session);
        }

        return list;
    }
}
