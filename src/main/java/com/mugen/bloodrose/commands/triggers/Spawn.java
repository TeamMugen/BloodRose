package com.mugen.bloodrose.commands.triggers;

import com.mugen.bloodrose.Score;
import com.mugen.bloodrose.VariableMaps;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mugen.bloodrose.VariableMaps.PlayerStatus.RESERVING;
import static com.mugen.bloodrose.VariableMaps.PlayerStatus.SPAWN_SET;
import static com.mugen.bloodrose.VariableMaps.sessions;
import static com.mugen.bloodrose.filemanager.PlayerData.getPlayerStatus;
import static com.mugen.bloodrose.filemanager.PlayerData.setPlayerStatus;
import static com.mugen.bloodrose.filemanager.SessionData.getSpawns;
import static com.mugen.bloodrose.filemanager.SessionData.setSpawns;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.loadArenas;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.setArenaYaml;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.getSpawnRadius;
import static com.mugen.bloodrose.utils.Util.*;
import static org.bukkit.Material.STICK;

public class Spawn implements CommandExecutor, Listener {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String a, String[] args) {
        Player p = (Player) sender;
        if (args.length < 4) {
            return sendError(p, "§c引数が足りません。/rose spawn (set..|remove..|tp..) <mode> <arena>");
        }
        String mode = args[2].toLowerCase();
        String arena = args[3].toLowerCase();

        if (isBoot(mode, arena)) {
            return sendError(p, "§c現在そのアリーナは試合進行中なので変更ができません。");
        }

        if (getPlayerStatus(p).equals(VariableMaps.PlayerStatus.PLAYING) || getPlayerStatus(p).equals(RESERVING)) {
            return sendError(p, "§c試合に参加中なので起動できません。");
        }

        if (mode.equals("sd")) {
            return sendError(p, "§cSDモードではこのサブコマンドは使いません。pointを使ってください。");
        }

        String commandName = args[1].toLowerCase();
        switch (commandName) {
            case "add", "set" -> {
                set(p, mode, arena);
                return true;
            }
        }

        if (args.length < 5) {
            return sendError(p, "§c引数が足りません。/rose spawn (remove..|tp..) <mode> <arena> <index>");
        }
        int index = Integer.parseInt(args[4]);

        return switch (commandName) {
            case "remove", "delete", "del" -> {
                delete(p, mode, arena, index);
                yield true;
            }
            case "teleport", "tp" -> {
                teleport(p, mode, arena, index);
                yield true;
            }
            default -> sendError(p, "§c引数が違います。/rose spawn (set..|remove..|tp..|list)");
        };
    }


    private static final Map<Player, String> spawnSession = new HashMap<>();
    private static final Map<Player, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 20 * 50L; // 20 ticks in milliseconds

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!getPlayerStatus(player).equals(SPAWN_SET)) return;
        String session = spawnSession.get(player);

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.STICK) return;
        event.setCancelled(true);

        Location loc = player.getLocation();
        loc.setPitch(0);

        if (cooldowns.containsKey(player) && (System.currentTimeMillis() - cooldowns.get(player)) < COOLDOWN_TIME) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cクールダウン中です。"));
            return;
        }
        cooldowns.put(player, System.currentTimeMillis());
//
//        if (name.startsWith("cq")) {
//            if (event.getAction().name().contains("LEFT")) {
//                boolean bool = false;
//                for (String point : getPoints(name).keySet()) {
//                    if (bool) {
//                        whatSpot.put(player, point);
//                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aポイント" + point + "のスポーン登録に切り替えました"));
//                    }
//                    if (whatSpot.get(player).equals(point)) {
//                        bool = true;
//                    }
//                }
//            } else if (event.getAction().name().contains("RIGHT")) {
//                int size = getSpawnsCP(name, whatSpot.get(player)).size() + 1;
//                int i = 0;
//                for (Location l : getSpawns(whatSession.get(player))) {
//                    if (l.distance(loc) <= getSpawnRadius()) {
//                        player.sendMessage("§c近くに登録済みのスポーン地点がありますが、登録しました。(発見地点:" + i + ", 現在地点:" + size + ")");
//                        break;
//                    }
//                    i++;
//                }
//                registerSpawn(player, loc);
//                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a" + whatSession.get(player) + "の" + whatSpot.get(player) + "地点におけるスポーン地点を登録しました。 §7(index:" + size + ")"));
//            }
//
//        } else {
        int size = getSpawns(session).size() + 1;
        int i = 0;
        for (Location l : getSpawns(spawnSession.get(player))) {
            if (l.distance(loc) <= getSpawnRadius()) {
                player.sendMessage("§c近くに登録済みのスポーン地点がありますが、登録しました。(発見地点:" + i + ", 現在地点:" + size + ")");
                break;
            }
            i++;
        }
        registerSpawn(player, loc);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a登録しました！ §7(index:" + size + ")"));
//        }
    }

    @EventHandler
    public void leaveEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        String session = spawnSession.get(player);
        sessions.remove(session);
        spawnSession.remove(player);
    }

    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!getPlayerStatus(player).equals(SPAWN_SET)) return;
        event.setCancelled(true);

        String session = spawnSession.get(player);

        if (event.getMessage().contains("done")) {
            player.sendMessage("§a地点登録モードを終了しました。 §7(現在の登録数:" + getSpawns(session).size() + ")");
            sessions.remove(session);
            setPlayerStatus(player, VariableMaps.PlayerStatus.UNDEFINED);
            spawnSession.remove(player);
            new Score(null, null, "setup", player, 0);

        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c地点登録モード起動中です。終了するには/done"));
        }
    }

    private void registerSpawn(Player player, Location loc) {
        String session = spawnSession.get(player);

//            if (name.startsWith("cq")) {
//            List<Location> spawnList = new ArrayList<>();
//            if (getSpawns(name) != null) {
//                spawnList = getSpawns(name);
//            }
//            spawnList.add(loc);
//            arenaManager.get(name).setSpawns(spawnList);
//
//            List<Location> spawnCPList = new ArrayList<>();
//            if (getSpawnsCP(name, whatSpot.get(player)) != null) {
//                spawnCPList = getSpawnsCP(name, whatSpot.get(player));
//            }
//            spawnCPList.add(loc);
//            cqManager.get(name).setSpawnsCP(whatSpot.get(player), spawnCPList);
//
//            List<String> list = new ArrayList<>();
//            for (Location l : spawnCPList) {
//                list.add(l.toString());
//            }
//
//            Map<String, Object> data = getArenaYaml(name);
//            data.put("spawn_cp." + whatSpot.get(player), list);
//            setArenaYaml(name, data);
//        }

        List<Location> spawnList = new ArrayList<>();
        if (getSpawns(session) != null) {
            spawnList = getSpawns(session);
        }
        spawnList.add(loc);
        setSpawns(session, spawnList);

        List<String> list = new ArrayList<>();
        for (Location l : spawnList) {
            list.add(l.toString());
        }
        setArenaYaml(session, "spawns", list);
    }


    // rose spawn set <mode> <arena>
    private void set(Player p, String mode, String arena) {
        String session = mode + arena;

        if (getPlayerStatus(p).equals(SPAWN_SET)) {
            p.sendMessage("§cすでに地点登録モードを起動中です。");
            return;
        }

        initializeArena(mode, arena, p);

        spawnSession.put(p, session);
        loadArenas(mode, arena);

        setPlayerStatus(p, SPAWN_SET);
        new Score(mode, arena, "spawn", p, 0);

        ItemStack item = new ItemStack(STICK);
        p.getInventory().addItem(item);

        p.sendMessage("§aスポーン地点登録モードを起動しました！§6done§aとチャットすることで完了できます。");
    }


    // /rose spawn remove <mode> <arena> <index>
    private void delete(Player p, String mode, String arena, int index) {
        String session = mode + arena;

        loadArenas(mode, arena);

        List<Location> spawnList = new ArrayList<>(getSpawns(session));

        if (index < 0 || index >= spawnList.size()) {
            p.sendMessage("§cそのindexは存在しません。");
        }

        spawnList.remove(index);
        setSpawns(session, spawnList);

        List<String> list = new ArrayList<>();
        for (Location l : spawnList) {
            list.add(l.toString());
        }

//        Map<String, Object> data = getArenaYaml(session);
//        data.put("spawns", list);
        setArenaYaml(session, "spawns", list);

        p.sendMessage("§7" + session + "アリーナ" + index + "のスポーン設定を削除しました。");


        if (spawnList.isEmpty()) {
            arenaDelete(mode, arena, p);
        }

        sessions.remove(session);
    }


    // /rose spawn tp <mode> <arena> <index>
    private void teleport(Player p, String mode, String arena, int index) {
        String session = mode + arena;

        loadArenas(mode, arena);

        if (getSpawns(session) == null) {
            p.sendMessage("§cアリーナのスポーンが設定されていません。");
        }

        List<Location> spawnList = getSpawns(session);
        if (index >= 0 && index < spawnList.size()) {
            Location loc = spawnList.get(index);
            p.teleport(loc);
            p.sendMessage("§7" + session + "アリーナのスポーン" + index + "にTPしました。");

        } else {
            p.sendMessage("§cそのindexは存在しません。");
        }

        sessions.remove(session);
    }
}

