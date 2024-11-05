package com.mugen.bloodrose.utils;

import com.mugen.bloodrose.BloodRose;
import com.mugen.bloodrose.Score;
import com.mugen.bloodrose.filemanager.PlayerData;
import fr.skytasul.glowingentities.GlowingEntities;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static com.mugen.bloodrose.BloodRose.getInstance;
import static com.mugen.bloodrose.VariableMaps.PlayerStatus.PLAYING;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.StatusArena.*;
import static com.mugen.bloodrose.VariableMaps.StatusBomb.HAVING;
import static com.mugen.bloodrose.commands.triggers.Arena.updateArenaMenu;
import static com.mugen.bloodrose.commands.triggers.CheckTeam.checkTeam;
import static com.mugen.bloodrose.filemanager.PlayerData.*;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.*;
import static com.mugen.bloodrose.filemanager.loader.LangLoader.prefix;
import static com.mugen.bloodrose.filemanager.loader.PlayerLoader.setPyml;
import static com.mugen.bloodrose.utils.FastBossbar.*;
import static com.mugen.bloodrose.utils.FastTeam.joinTeam;
import static com.mugen.bloodrose.utils.FastTeam.leaveTeamGeneral;
import static com.mugen.bloodrose.utils.Util.*;
import static org.bukkit.Bukkit.getLogger;

public class AssistantGame {

    public static void checkLeaveArena(Player player) {
        if (getSession(player) == null) return;
        String session = getSession(player);
        String mode = getMode(player);
        String arena = getArena(player);

        List<Player> pList = new ArrayList<>(getPlayers(session));
        pList.remove(player);
        setPlayers(session, pList);
        updateArenaMenu(mode, arena, true);

        if (mode.equalsIgnoreCase("ffa")) {
            bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §e" + player.getName() + "§eが退室しました。");
        } else {
            bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §" + getColor(session, player) + player.getName() + "§eが退室しました。");
        }

        int red = 999;
        int blue = 999;
        if (!mode.equalsIgnoreCase("ffa")) {
            List<Player> reds = new ArrayList<>(getReds(session));
            reds.remove(player);
            setReds(session, reds);
            red = reds.size();

            List<Player> blues = new ArrayList<>(getBlues(session));
            blues.remove(player);
            setBlues(session, blues);
            blue = blues.size();
        }

        setPlayerStatus(player, PlayerStatus.UNDEFINED);
        if (pList.size() <= 1 || (Math.min(red, blue) == 0)) {
            if (checkStatusArena(session, GAMING)) {
                if (session.equals(mode + reserveArena.get(mode))) {
                    reserveArena.remove(mode);
                }
                updateArenaMenu(mode, arena, false);
                setStatusArena(session, ABORT);

                bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §cゲームに必要なメンバーがいなくなったためゲームを終了します。");
            }

            if (pList.isEmpty()) {
                if (session.equals(mode + reserveArena.get(mode))) {
                    reserveArena.remove(mode);
                }
                if (checkStatusArena(session, ANNOUNCE)) {
                    setStatusArena(session, SKIP);
                }
                updateArenaMenu(mode, arena, false);
            }
        }

        leaveTeamGeneral(player);
        leavePlayerBossBar(session, player);
        playerManager.put(player, new PlayerData());
        new Score(mode, getArena(player), "setup", player, 0);
        player.sendMessage("§c" + session + "アリーナから退室しました。");
    }


    public static void countdown(String mode, String arena, int timeCurrent, int timeTotal) {
        int rest = timeTotal - timeCurrent;
        double div = (double) rest / timeTotal;
        setProgress(mode + arena, div);

        new Score(mode, arena, "count", null, rest);

        String message = switch (rest) {
            case 5 -> "残り5秒です";
            case 10 -> "残り10秒です";
            case 15 -> "残り15秒です";
            case 30 -> "残り30秒です";
            case 60 -> "残り1分です";
            case 180 -> "残り3分です";
            case 300 -> "残り5分です";
            case 600 -> "残り10分です";
            case 1800 -> "残り30分です";
            default -> null;
        };
        if (message != null) {
            String session = mode + arena;
            List<Player> players = getPlayers(session);
            bcToList(players, prefix + "§f<§7" + session + "§f> §b" + message.replace("%prefix%", prefix));
        }
    }


    public static void choiceSpawn(List<Location> spawnList, int n, Player p, List<Player> pList) {
        int index = new Random().nextInt(spawnList.size());
        Location spawn = spawnList.get(index);

        runTask(() -> p.teleport(spawn));

        Integer r = getSpawnRadius();

        Bukkit.getScheduler().runTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("RosePvP")), () -> {
            boolean hasNearbyPlayers = false;
            for (Entity entity : p.getNearbyEntities(r, r, r)) {
                if (!(entity instanceof Player)) continue;
                if (entity == p) continue;
                if (!pList.contains(entity)) continue;

                hasNearbyPlayers = true;
                Bukkit.getScheduler().runTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("RosePvP")), () -> {
                    if (n < getSpawnTry()) {
                        choiceSpawn(spawnList, n + 1, p, pList);

                    } else if (n == getSpawnTry()) {
                        spawnList.remove(spawn);
                        p.sendMessage("§c最適なスポーン位置が見つかりませんでした。最後に試行された地点に設定します。");
                    }
                });
                break;
            }
            if (!hasNearbyPlayers) {
                spawnList.remove(spawn);
            }
        });
    }


    public static void giveReward(Player p) {
        Economy econ = ((BloodRose) getInstance()).getEconomy();
        if (econ == null) {
            return;
        }

        String session = getSession(p);
        if (getTmpkills(session).get(p) == 0
                && getRank(session) == null) {
            return;
        }

        //キル報酬
        int i = getTmpkills(session).get(p) * getRewardKill();
        if (i > 0) {
            EconomyResponse response = econ.depositPlayer(p, i);
            if (response.transactionSuccess()) {
                p.sendMessage("§aキル報酬の $%money%"
                        .replace("%money%", String.valueOf(i)));
            } else {
                getLogger().severe(String.format("エラー: %sの口座にお金を追加できませんでした: %s", p.getName(), response.errorMessage));
            }
        }

        //ランク報酬
        if (getRank(session).get(p) != null) {
            Map<String, Integer> data = new HashMap<>(pyml.get(p.getUniqueId()));
            data.put("lose_" + getMode(p), data.get("lose_" + getMode(p)) + 1);
            int j = switch (getRank(session).get(p)) {
                case FFA1 -> {
                    data.put("win_ffa", data.get("win_ffa") + 1);
                    data.put("lose_" + getMode(p), data.get("lose_" + getMode(p)) - 1);
                    yield getRewards().get("ffa1");
                }
                case FFA2 -> getRewards().get("ffa2");
                case FFA3 -> getRewards().get("ffa3");
                case TDM -> {
                    data.put("win_tdm", data.get("win_tdm") + 1);
                    data.put("lose_" + getMode(p), data.get("lose_" + getMode(p)) - 1);
                    yield getRewards().get("tdm");
                }
                case SD -> {
                    data.put("win_sd", data.get("win_sd") + 1);
                    data.put("lose_" + getMode(p), data.get("lose_" + getMode(p)) - 1);
                    yield getRewards().get("sd");
                }
                case DOM -> {
                    data.put("win_dom", data.get("win_dom") + 1);
                    data.put("lose_" + getMode(p), data.get("lose_" + getMode(p)) - 1);
                    yield getRewards().get("dom");
                }
                case CQ -> {
                    data.put("win_cq", data.get("win_cq") + 1);
                    data.put("lose_" + getMode(p), data.get("lose_" + getMode(p)) - 1);
                    yield getRewards().get("cq");
                }
                case UNDEFINED -> 0;
            };
            setPyml(p, data);
            EconomyResponse response = econ.depositPlayer(p, j);
            if (response.transactionSuccess()) {
                p.sendMessage("§a試合報酬の $%money%"
                        .replace("%money%", String.valueOf(j)));
            } else {
                getLogger().severe(String.format("エラー: %sの口座にお金を追加できませんでした: %s", p.getName(), response.errorMessage));
            }
        }

        //最終的な評価
        double balance = econ.getBalance(p);
        p.sendMessage("§aが%player%の口座に追加されました。§7(新しい残高: %bal%円)"
                .replace("%player%", p.getName())
                .replace("%bal%", String.valueOf(balance)));
    }

    public static void initPlayerMode(String mode, String arena, Player p) {
        String session = mode + arena;

        initPlayer(p);
        addPlayerBossBar(session, p);
        setPlayerStatus(p, PLAYING);

        setTmpkills(session, p, 0);
        setKillstreak(session, p, 0);
        setRank(session, p, PlayerRank.UNDEFINED);

        switch(mode) {
            case "ffa":
                runTask(() -> choiceSpawn(getSpawns(session), 1, p, getPlayers(session)));
                break;
            case "tdm":
                runTask(() -> choiceSpawn(getSpawns(session), 1, p, getPlayers(session)));
                checkTeam(p);
                break;
            case "sd":
                setTeamChat(session, p, true);
                checkTeam(p);
                if (checkStatusArena(session, GAMING)) {

                } else {

                }
                break;
        }

        openGUI("loadloadout", p);
    }

    public static void midJoin(String session, Player p) {
        int reds = getReds(session).size();
        int blues = getBlues(session).size();

        int index;
        if (reds > blues) {
            index = 0;
        } else if (blues > reds) {
            index = 1;
        } else {
            index = new Random().nextInt(2);
        }

        if (index == 0) {
            joinTeam(session + "blue", p);
            var list = getBlues(session);
            list.add(p);
            setBlues(session, list);
        } else {
            joinTeam(session + "red", p);
            var list = getReds(session);
            list.add(p);
            setReds(session, list);
        }
        bcToList(getPlayers(session), prefix + "§f<§7" + session + "§f> §" + getColor(session, p) + p.getName() + "§eが途中参加しました。");
    }

    public static char getColor(String session, Player p) {
        if (getReds(session).contains(p)) {
            return 'c';
        }
        return 'b';
    }

    public static String getSDTeam(String session, SDTeams team) {
        SDTeams ROR = getRole(session, "red");

        if (team.equals(DESTROYER)) {
            if (ROR == DESTROYER) {
                return "red";
            }
            return "blue";
        }
        if (ROR == SEARCHER) {
            return "red";
        }
        return "blue";
    }

    public static List<Player> getSDPlayers(String session, SDTeams team) {
        SDTeams ROR = getRole(session, "red");

        if (team.equals(DESTROYER)) {
            if (ROR == DESTROYER) {
                return getReds(session);
            } else {
                return getBlues(session);
            }
        } else {
            if (ROR == SEARCHER) {
                return getReds(session);
            } else {
                return getBlues(session);
            }
        }
    }


    public static String getPoint(Player player, String session) {
        Map<String, Location> points = getPoints(session);
        for (Map.Entry<String, Location> p : points.entrySet()) {
            double d = p.getValue().distance(player.getLocation());
            if (d < 5) {
                return p.getKey();
            }
        }
        return null;
    }

    public static ItemStack getBomb() {
        ItemStack tntItem = new ItemStack(Material.TNT, 1);
        ItemMeta meta = tntItem.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName("§c§l爆弾");
        tntItem.setItemMeta(meta);
        return tntItem;
    }

    public static void giveTNT(Player p, String session) {
        p.getInventory().addItem(getBomb());

        setBomber(session, p);
        p.sendMessage("§cあなたがボンバーです。ビーコンを右クリックし続けて爆弾を設置しよう！");
        bcToList(getSDPlayers(session, DESTROYER), "§a" + p.getName() + "がボンバーです！護衛して爆弾設置を補助しよう！");
        bcToList(getSDPlayers(session, SEARCHER), "§aボンバーが決まりました！");
    }

    public static void dropBomb(Player p, Location loc) {
        if (!getPlayerStatus(p).equals(PLAYING)) return;
        String session = getSession(p);
        tntMap.remove(session);

        Player bomber;
        if (getBomber(session) != null) {
            bomber = getBomber(session);
            if (!bomber.equals(p)) return;
        }

        if (!getStatusBomb(session).equals(HAVING)) return;

        Entity droppedTNT = loc.getWorld().dropItem(loc, getBomb());
        GlowingEntities ge = new GlowingEntities(getInstance());
        for (Player d : getSDPlayers(session, DESTROYER)) {
            try {
                ge.setGlowing(droppedTNT, d, ChatColor.YELLOW);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        tntMap.put(session, droppedTNT);
        geMap.put(droppedTNT, ge);

        setBomber(session, null);
        bcToList(getSDPlayers(session, DESTROYER), "§aボンバーが死亡したので、爆弾がドロップしました！");
    }

    public static void removeDroppedBomb(String session) {
        for (Player d : getSDPlayers(session, DESTROYER)) {
            try {
                geMap.get(tntMap.get(session)).unsetGlowing(tntMap.get(session), d);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
        geMap.remove(tntMap.get(session));
        tntMap.remove(session);
    }

    public static boolean isBomb(ItemStack item) {
        return item.getType() ==
                Material.TNT &&
                item.hasItemMeta() &&
                Objects.requireNonNull(item.getItemMeta()).hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("§c§l爆弾");
    }
}
