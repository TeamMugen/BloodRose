package com.keisa1333.rosepvp.filemanager.loader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.keisa1333.rosepvp.VariableMaps.pyml;
import static com.keisa1333.rosepvp.VariableMaps.uuids;

public class PlayerLoader {
    public static void loadPlayers() {
        String path = "plugins/RosePvP/players";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        Path folder = Paths.get(path);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folder, "*.{yml}")) {
            for (Path filePath : directoryStream) {
                String fileName = filePath.getFileName().toString();
                String uuidString = fileName.replace(".yml", "");
                UUID uuid = UUID.fromString(uuidString);
                uuids.add(uuid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadPyml(Player p) {
        UUID uuid = p.getUniqueId();
        if (uuids.contains(uuid)) {
            String yamlFilePath = "plugins/RosePvP/players/" + uuid.toString() + ".yml";
            Yaml yaml = new Yaml();
            try (InputStream inputStream = new FileInputStream(yamlFilePath)) {
                Map<String, Object> data = yaml.load(inputStream);
                Map<String, Integer> inside = new HashMap<>();
                inside.put("kill", (int) data.get("kill"));
                inside.put("death", (int) data.get("death"));
                inside.put("win_ffa", (int) data.get("win_ffa"));
                inside.put("win_tdm", (int) data.get("win_tdm"));
                inside.put("win_sd", (int) data.get("win_sd"));
                inside.put("win_dom", (int) data.get("win_dom"));
                inside.put("win_cq", (int) data.get("win_cq"));
                inside.put("win_inf", (int) data.get("win_inf"));
                inside.put("lose_ffa", (int) data.get("lose_ffa"));
                inside.put("lose_tdm", (int) data.get("lose_tdm"));
                inside.put("lose_sd", (int) data.get("lose_sd"));
                inside.put("lose_dom", (int) data.get("lose_dom"));
                inside.put("lose_cq", (int) data.get("lose_cq"));
                inside.put("lose_inf", (int) data.get("lose_inf"));
                pyml.put(uuid, inside);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            initPyml(p);
        }
    }

    public static void setPyml(Player p, Map<String, Integer> data) {
        UUID uuid = p.getUniqueId();
        final File playerFile = new File("plugins/RosePvP/players", uuid + ".yml");
        final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        playerConfig.set("kill", data.get("kill"));
        playerConfig.set("death", data.get("death"));
        playerConfig.set("win_ffa", data.get("win_ffa"));
        playerConfig.set("win_tdm", data.get("win_tdm"));
        playerConfig.set("win_sd", data.get("win_sd"));
        playerConfig.set("win_dom", data.get("win_dom"));
        playerConfig.set("win_cq", data.get("win_cq"));
        playerConfig.set("win_inf", data.get("win_inf"));
        playerConfig.set("lose_ffa", data.get("lose_ffa"));
        playerConfig.set("lose_tdm", data.get("lose_tdm"));
        playerConfig.set("lose_sd", data.get("lose_sd"));
        playerConfig.set("lose_dom", data.get("lose_dom"));
        playerConfig.set("lose_cq", data.get("lose_cq"));
        playerConfig.set("lose_inf", data.get("lose_inf"));

        Map<String, Integer> insideData = new HashMap<>();
        insideData.put("kill", data.get("kill"));
        insideData.put("death", data.get("death"));
        insideData.put("win_ffa", data.get("win_ffa"));
        insideData.put("win_tdm", data.get("win_tdm"));
        insideData.put("win_sd", data.get("win_sd"));
        insideData.put("win_dom", data.get("win_dom"));
        insideData.put("win_cq", data.get("win_cq"));
        insideData.put("win_inf", data.get("win_inf"));
        insideData.put("lose_ffa", data.get("lose_ffa"));
        insideData.put("lose_tdm", data.get("lose_tdm"));
        insideData.put("lose_sd", data.get("lose_sd"));
        insideData.put("lose_dom", data.get("lose_dom"));
        insideData.put("lose_cq", data.get("lose_cq"));
        insideData.put("lose_inf", data.get("lose_inf"));

        pyml.put(uuid, insideData);

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initPyml(Player p) {
        UUID uuid = p.getUniqueId();
        uuids.add(uuid);
        Map<String, Integer> newPlayersList = pyml.get(uuid);

        if (newPlayersList == null) {
            Map<String, Integer> data = new HashMap<>();
            data.put("kill", 0);
            data.put("death", 0);
            data.put("win_ffa", 0);
            data.put("win_tdm", 0);
            data.put("win_sd", 0);
            data.put("win_dom", 0);
            data.put("win_cq", 0);
            data.put("win_inf", 0);
            data.put("lose_ffa", 0);
            data.put("lose_tdm", 0);
            data.put("lose_sd", 0);
            data.put("lose_dom", 0);
            data.put("lose_cq", 0);
            data.put("lose_inf", 0);
            setPyml(p, data);
        }
    }
}
