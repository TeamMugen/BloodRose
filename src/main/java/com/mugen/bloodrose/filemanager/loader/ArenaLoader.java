package com.mugen.bloodrose.filemanager.loader;

import com.mugen.bloodrose.VariableMaps;
import com.mugen.bloodrose.filemanager.SessionData;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mugen.bloodrose.BloodRose.getInstance;
import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.sessions;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.modes;

public class ArenaLoader {
    public static void loadArenasFromFolder() {
        VariableMaps.arenas = new HashMap<>();

        // ファイルディレクトリの指定
        File dir = new File("plugins/BloodRose/arenas/");

        // 対応するモードのリストを初期化
        for (String mode : modes) {
            VariableMaps.arenas.put(mode, new ArrayList<>());
        }

        // ディレクトリが存在し、ディレクトリであることを確認
        if (dir.exists() && dir.isDirectory()) {
            // ディレクトリ内のすべてのファイルを取得
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    // ファイルであることを確認
                    if (file.isFile()) {
                        // ファイル名を取得（拡張子なし）
                        String fileName = file.getName().split("\\.")[0];

                        // 各モードに対して処理を行う
                        for (String mode : modes) {
                            // プレフィックスが一致する場合
                            if (fileName.startsWith(mode)) {
                                // プレフィックスを除いた部分を取得
                                String arena = fileName.substring(mode.length());

                                // リストに追加
                                VariableMaps.arenas.get(mode).add(arena);
                                Bukkit.getLogger().info("[BloodRose] load arena: " + mode + "." + arena);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void loadArenas(String mode, String arena) {
        String session = mode + arena;
        String yamlFilePath = "plugins/BloodRose/arenas/" + session + ".yml";
        Yaml yaml = new Yaml();

        if (!modes.contains(mode)) return;
        if (!VariableMaps.arenas.get(mode).contains(arena)) return;

        try (InputStream inputStream = Files.newInputStream(new File(yamlFilePath).toPath())) {
            Map<String, Object> data = yaml.load(inputStream);
            Bukkit.getLogger().info("[BloodRose] load arena \"" + mode + "." + arena + "\"");
            sessions.put(session, new SessionData(mode));

            if (data.get("need_player") != null) {
                int needP = (int) data.get("need_player");
                setNeedP(session, needP);
            }

            if (data.get("max_player") != null) {
                int maxP = (int) data.get("max_player");
                setMaxP(session, maxP);
            }

            if (data.get("time_game") != null) {
                int time = (int) data.get("time_game");
                setTime(session, time);
            }

            if (data.get("spawns") != null) {
                if (data.get("spawns") instanceof List<?>) {
                    Object strs = data.get("spawns");
                    List<Location> spawns = convertStringListToLocationList((List<String>) strs);
                    setSpawns(session, spawns);
                } else {
                    Bukkit.getLogger().info("error: spawns");
                }
            }

            if (data.get("points") != null) {
                Map<String, String> points = (Map<String, String>) data.get("points");
                Map<String, Location> inner = new HashMap<>();
                for (Map.Entry<String, String> point : points.entrySet()) {
                    inner.put(point.getKey(), convertStringListToLocationList(List.of(point.getValue())).get(0));
                }
                setPoints(session, inner);
            }


            //SD
            if (data.get("sd_need_winning_count") != null) {
                int count = (int) data.get("sd_need_winning_count");
                setCountOfWin(session, count);
            }

            if (data.get("time_bomb") != null) {
                int time = (int) data.get("time_bomb");
                setTimeBomb(session, time);
            }

            if (data.get("count_bomb_place") != null) {
                int count = (int) data.get("count_bomb_place");
                setCountPlace(session, count);
            }

            if (data.get("count_bomb_pick") != null) {
                int count = (int) data.get("count_bomb_pick");
                setCountPick(session, count);
            }

            if (data.get("point_bomb_drop") != null) {
                setLocBomb(session, (String) data.get("point_bomb_drop"));
            }

            if (data.get("point_bomb_points") != null) {
                if (data.get("spawns") instanceof List<?>) {
                    List<String> points = (List<String>) data.get("spawns");
                    setBombPoints(session, points);
                } else {
                    Bukkit.getLogger().info("error: point_bomb_points");
                }
            }

            if (data.get("point_spawn_destroyer") != null) {
                setLocSD(session, DESTROYER, (String) data.get("point_spawn_destroyer"));
            }

            if (data.get("point_spawn_searcher") != null) {
                setLocSD(session, SEARCHER, (String) data.get("point_spawn_searcher"));
            }
//
//            if (data.get("spawn_cp") != null) {
//                Map<String, List<String>> mapCQ = (Map<String, List<String>>) data.get("spawn_cp");
//                List<Location> spawnsEntire = new ArrayList<>();
//                for (String point : mapCQ.keySet()) {
//                    List<Location> spawnsCQ = convertStringListToLocationList(mapCQ.get(point));
//                    cqManager.get(session).setSpawnsCP(point, spawnsCQ);
//                    spawnsEntire.addAll(spawnsCQ);
//                }
//                arenaManager.get(session).setSpawns(spawnsEntire);
//            }
//
//            if (data.get("cp_red") != null) {
//                String cpRed = (String) data.get("cp_red");
//                cqManager.get(session).setCPRed(cpRed);
//            }
//
//            if (data.get("cp_blue") != null) {
//                String cpBlue = (String) data.get("cp_blue");
//                cqManager.get(session).setCPRed(cpBlue);
//            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Location> convertStringListToLocationList(List<String> stringList) {
        List<Location> locationList = new ArrayList<>();

        for (String str : stringList) {
            String[] parts = str.split(",");
            World world = Bukkit.getWorld(parts[0].split("=")[2].replace("}", ""));
            double x = Double.parseDouble(parts[1].split("=")[1]);
            double y = Double.parseDouble(parts[2].split("=")[1]);
            double z = Double.parseDouble(parts[3].split("=")[1]);
            float pitch = Float.parseFloat(parts[4].split("=")[1]);
            float yaw = Float.parseFloat(parts[5].split("=")[1].replace("}", ""));

            Location location = new Location(world, x, y, z, yaw, pitch);

            locationList.add(location);
        }

        return locationList;
    }


    public static Map<String, Object> getArenaYaml(String session) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        final File arenasDirectory = new File("plugins/BloodRose/arenas");
        if (!arenasDirectory.exists()) {
            arenasDirectory.mkdirs();
        }

        String yamlFilePath = "plugins/BloodRose/arenas/" + session + ".yml";

        Map<String, Object> data = new HashMap<>();

        if (new File(yamlFilePath).exists()) {
            try (InputStream inputStream = new FileInputStream(new File(yamlFilePath))) {
                data = yaml.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void setArenaYaml(String session, String index, Object object) {
        File configFile = new File(getInstance().getDataFolder(), "arenas/" + session + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        config.set(index, object);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        DumperOptions options = new DumperOptions();
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//        Yaml yaml = new Yaml(options);
//        String yamlFilePath = "plugins/BloodRose/arenas/" + session + ".yml";
//        try (FileWriter writer = new FileWriter(yamlFilePath)) {
//            yaml.dump(data, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void createArenaYaml(String mode, String session) {
        String folderPath = "plugins/BloodRose/arenas";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        String filePath = "plugins/BloodRose/arenas/" + session + ".yml";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        StringBuilder content = new StringBuilder();
        content.append("need_player: 2\n");
        content.append("max_player: 12\n");

        switch (mode.toLowerCase()) {
            case "ffa":
            case "tdm":
                content.append("time_game: 180\n");
                content.append("\n");
                content.append("spawns: \"\"\n");
                break;
            case "sd":
                content.append("time_game: 180\n");
                content.append("\n");
                content.append("#SDの必要な勝利数\n");
                content.append("sd_need_winning_count: 5\n");
                content.append("#SDの爆弾爆発時間(秒)\n");
                content.append("time_bomb: 45\n");
                content.append("#爆弾を設置するのに必要なクリック数\n");
                content.append("count_bomb_place: 20\n");
                content.append("#爆弾を解除するのに必要なクリック数\n");
                content.append("count_bomb_pick: 30\n");
                content.append("#ラウンド開始時に爆弾をドロップする地点\n");
                content.append("point_bomb_drop: \"\"\n");
                content.append("#爆弾を設置、解除する地点\n");
                content.append("point_bomb_areas:\n");
                content.append("  - \"\"\n");
                content.append("  - \"\"\n");
                content.append("#デストロイヤーのスポーン地点\n");
                content.append("point_spawn_destroyer: \"\"\n");
                content.append("#サーチャーのスポーン地点\n");
                content.append("point_spawn_searcher: \"\"\n");
                content.append("\n");
                content.append("points: \"\"\n");
                break;
//            case "dom":
//                content.append("time_game: 800\n");
//                content.append("\n");
//                content.append("points: \"\"\n");
//                content.append("spawns: \"\"\n");
//                break;
//            case "cq":
//                content.append("time_game: 800\n");
//                content.append("\n");
//                content.append("#CQチケットの数\n");
//                content.append("cq_amount_ticket: 400\n");
//                content.append("\n");
//                content.append("cp_red: \"\"\n");
//                content.append("cp_blue: \"\"\n");
//                content.append("spot: \"\"\n");
//                content.append("spawn_cp: \"\"\n");
//                break;
        }

        // ファイルに保存
        try {
            FileUtils.writeStringToFile(file, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
