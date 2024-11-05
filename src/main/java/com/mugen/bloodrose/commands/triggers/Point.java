package com.mugen.bloodrose.commands.triggers;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

import static com.mugen.bloodrose.VariableMaps.arenas;
import static com.mugen.bloodrose.VariableMaps.sessions;
import static com.mugen.bloodrose.filemanager.SessionData.getBombPoints;
import static com.mugen.bloodrose.filemanager.SessionData.getPoints;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.loadArenas;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.setArenaYaml;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.setGlobalSpawn;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.setRespawnSpawn;
import static com.mugen.bloodrose.utils.Util.*;

public class Point implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String a, String[] args) {
        Player p = (Player) sender;

        if (args.length < 2) {
            return sendError(p, "§c引数が足りません。/rose point (add..|set..|remove..|tp..||global|respawn)");
        }

        String commandName = args[1].toLowerCase();
        switch (commandName) {
            case "global":
                setGlobalSpawn(p.getLocation());
                sender.sendMessage("§7グローバルスポーン地点を設定しました。");
                return true;

            case "respawn":
                setRespawnSpawn(p.getLocation());
                sender.sendMessage("§7リスポーン地点を設定しました。");
                return true;
        }

        if (args.length < 5) {
            return sendError(p, "§c引数が足りねえぞ！拓也！");
        }

        String mode = args[2].toLowerCase();
        String arena = args[3].toLowerCase();
        String session = mode + arena;
        String point = args[4].toLowerCase();

        //アリーナがあるか
        if (!arenas.get(mode).contains(arena)) {
            return sendError(p, "§c" + session + "アリーナは存在しません。");
        }

        if (List.of("ffa", "tdm").contains(mode)) {
            return sendError(p, "§cFFA、TDMモードではこのサブコマンドは使いません。spawnを使ってください。");
        }

        if (isBoot(mode, arena)) {
            return sendError(p, "§c" + session + "アリーナは使用中のためその操作はできません。");
        }

        switch (commandName) {
            case "add" -> {
                add(p, mode, arena, point);
                return true;
            }
            case "remove", "delete", "del" -> {
                delete(mode, arena, point);
                return true;
            }
            case "teleport", "tp" -> {
                teleport(p, mode, arena, point);
                return true;
            }
        }

        if (args.length < 6) {
            return sendError(p, "§c引数が足りねえぞ！拓也！");
        }
        String role = args[5];

        if (commandName.equals("set")) {
            set(p, mode, arena, point, role);
            return true;
        }
        return false;
    }


    // rose spawn add <mode> <arena>
    private void add(Player p, String mode, String arena, String point) {
        String session = mode + arena;

        initializeArena(mode, arena, p);
        loadArenas(mode, arena);

        Location loc = p.getLocation();
        loc.setPitch(0);
        Map<String, Location> points = getPoints(session);
        points.put(point, loc);
        setArenaYaml(session, "points", points);

        p.sendMessage("§a" + session + "アリーナに" + point + "ポイントを登録しました。");
    }


    // /rose spawn remove <mode> <arena> <index>
    private void delete(String mode, String arena, String point) {
        String session = mode + arena;

        loadArenas(mode, arena);

        Map<String, Location> points = getPoints(session);
        points.remove(point);
        setArenaYaml(session, "points", points);

        sessions.remove(session);
    }


    // /rose spawn tp <mode> <arena> <index>
    private void teleport(Player p, String mode, String arena, String point) {
        String session = mode + arena;

        loadArenas(mode, arena);

        Map<String, Location> points = getPoints(session);
        Location loc = points.get(point);
        p.teleport(loc);

        sessions.remove(session);
    }


    // /rose point set <mode> <arena> <point> <role>
    private void set(Player p, String mode, String arena, String point, String role) {
        String session = mode + arena;

        switch (mode) {
            case "sd":
                switch (role) {
                    case "bombDrop":
                        setArenaYaml(session, "point_bomb_drop", point);
                        break;
                    case "bombPoints":
                        loadArenas(mode, arena);
                        List<String> points = getBombPoints(session);

                        if (points.contains(point)) {
                            p.sendMessage("§cその地点は既にbombPointsに登録されています。");
                            break;
                        }

                        points.add(point);
                        setArenaYaml(session, "point_bomb_points", points);
                        sessions.remove(session);
                        break;
                    case "destroyer":
                        setArenaYaml(session, "point_spawn_destroyer", point);
                        break;
                    case "searcher":
                        setArenaYaml(session, "point_spawn_searcher", point);
                        break;
                    default:
                        p.sendMessage("§c第6引数が違います。");
                        break;
                }

            case "dom":
            case "cq":
        }
    }
}

