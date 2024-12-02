package com.mugen.bloodrose;

import com.mugen.bloodrose.filemanager.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mugen.bloodrose.BloodRose.getInstance;
import static com.mugen.bloodrose.VariableMaps.*;
import static com.mugen.bloodrose.VariableMaps.SDTeams.DESTROYER;
import static com.mugen.bloodrose.VariableMaps.SDTeams.SEARCHER;
import static com.mugen.bloodrose.VariableMaps.StatusBomb.DEAD_DESTROYER;
import static com.mugen.bloodrose.VariableMaps.StatusBomb.DEAD_SEARCHER;
import static com.mugen.bloodrose.filemanager.PlayerData.*;
import static com.mugen.bloodrose.filemanager.SessionData.*;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.getGlobalSpawn;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.getRespawnSpawn;
import static com.mugen.bloodrose.filemanager.loader.LangLoader.prefix;
import static com.mugen.bloodrose.filemanager.loader.PlayerLoader.loadPyml;
import static com.mugen.bloodrose.filemanager.loader.PlayerLoader.setPyml;
import static com.mugen.bloodrose.utils.AssistantGame.*;
import static com.mugen.bloodrose.utils.FastTeam.leaveTeamGeneral;
import static com.mugen.bloodrose.utils.Util.*;

public class ListenerEvent implements Listener {
    public static void playerJoin(Player p) {
        playerManager.put(p, new PlayerData());
        loadPyml(p);
        initPlayer(p);
        new Score(null, null, "setup", p, 0);

        if (getGlobalSpawn() != null) {
            p.teleport(getGlobalSpawn());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        leaveTeamGeneral(event.getPlayer());
        playerJoin(event.getPlayer());
    }

    @EventHandler
    public void leaveEvent(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String session = getSession(p);
        if (getPlayerStatus(p).equals(PlayerStatus.UNDEFINED)) return;

        checkLeaveArena(p);

        new Score(null, null, "delete", p, 0);
        boards.remove(p);
        leaveTeamGeneral(p);
        playerManager.remove(p);

        switch (getMode(p)) {
            case "sd":
                dropBomb(p, p.getLocation());
                if (getDeads(session, "red").contains(p)) {
                    var reds = getDeads(session, "red");
                    reds.remove(p);
                    setDeads(session, "red", reds);

                } else if (getDeads(session, "blue").contains(p)) {
                    var blues = getDeads(session, "blue");
                    blues.remove(p);
                    setDeads(session, "blue", blues);
                }
                break;

            case "dom":
//                if (whereIsPlayerIn.get(p) != null) {
//                    String point = whereIsPlayerIn.get(p);
//                    if (getBlues(session).contains(p)) {
//                        setAmountOfPersonInPoint(session, point, "blue", getAmountOfPersonInPoint(session, point, "blue") - 1);
//                    } else if (getReds(session).contains(p)) {
//                        setAmountOfPersonInPoint(session, point, "red", getAmountOfPersonInPoint(session, point, "red") - 1);
//                    }
//                }
                break;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player attacker = event.getEntity().getKiller();

        if (!getPlayerStatus(victim).equals(PlayerStatus.PLAYING)) {
            runTask(() -> {
                event.getEntity().spigot().respawn();
                Bukkit.getScheduler().runTask(getInstance(), () -> victim.teleport(getGlobalSpawn()));
            });
            return;
        }

        String session = getSession(victim);
        String mode = getMode(victim);

        Bukkit.getScheduler().scheduleSyncDelayedTask(getInstance(), () -> {
            event.getEntity().spigot().respawn();
            Bukkit.getScheduler().runTask(getInstance(), () -> victim.teleport(getRespawnSpawn()));
            deathGame(victim, attacker);

            if (!mode.equalsIgnoreCase("sd")) respawnMethod(victim, session);

            switch (mode.toLowerCase()) {
                case "tdm":
                    if (getReds(session).contains(attacker)) {
                        setRedP(session, getRedP(session) + 1);
                    } else if (getBlues(session).contains(attacker)) {
                        setBlueP(session, getBlueP(session) + 1);
                    }
                    break;

//                case "sd":
//                    dropBomb(victim, victim.getLocation());
//                    runTask(() -> {
//                        victim.setGameMode(GameMode.SPECTATOR);
//                        if (getSDPlayers(session, DESTROYER).contains(victim)) {
//                            victim.teleport(getLocSD(session, DESTROYER));
//                        } else {
//                            victim.teleport(getLocSD(session, SEARCHER));
//                        }
//                    });
//
//                    if (getReds(session).contains(victim)) {
//                        if (!getDeads(session, "red").contains(victim)) {
//                            List<Player> pList = new ArrayList<>(getDeads(session, "red"));
//                            pList.add(victim);
//                            setDeads(session, "red", pList);
//
//                            if (getReds(session).size() == getDeads(session, "red").size()) {
//                                if (getRole(session, "red").equals(DESTROYER)) {
//                                    setStatusBomb(session, DEAD_DESTROYER);
//                                } else {
//                                    setStatusBomb(session, DEAD_SEARCHER);
//                                }
//                            }
//                        }
//
//                    } else if (getBlues(session).contains(victim)) {
//                        if (!getDeads(session, "blue").contains(victim)) {
//                            List<Player> pList = new ArrayList<>(getDeads(session, "blue"));
//                            pList.add(victim);
//                            setDeads(session, "blue", pList);
//
//                            if (getBlues(session).size() == getDeads(session, "blue").size()) {
//                                if (getRole(session, "blue").equals(DESTROYER)) {
//                                    setStatusBomb(session, DEAD_DESTROYER);
//                                } else {
//                                    setStatusBomb(session, DEAD_SEARCHER);
//                                }
//                            }
//                        }
//                    }
//                    break;
//
//                case "dom":
//                    if (whereIsPlayerIn.get(victim) != null) {
//                        String point = whereIsPlayerIn.get(victim);
//                        if (getBlues(session).contains(victim)) {
//                            setAmountOfPersonInPoint(session, point, "blue", getAmountOfPersonInPoint(session, point, "blue") - 1);
//                        } else if (getReds(session).contains(victim)) {
//                            setAmountOfPersonInPoint(session, point, "red", getAmountOfPersonInPoint(session, point, "red") - 1);
//                        }
//                    }
//                    break;
            }
            new Score(mode, getArena(victim), "update", attacker, 0);
        }, 2L);
    }

    private void respawnMethod(Player victim, String session) {
        var players = getPlayers(session);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!getPlayerStatus(victim).equals(PlayerStatus.PLAYING)) {
                    Bukkit.getScheduler().runTask(getInstance(), () -> victim.teleport(getGlobalSpawn()));
                    return;
                }
                List<Location> newSpawnList = new ArrayList<>(getSpawns(session));
                choiceSpawn(newSpawnList, 1, victim, players);
                openGUI("loadloadout", victim);
            }
        }.runTaskLater(getInstance(), 100);
    }

    private static void deathGame(Player victim, Player attacker) {
        Map<String, Integer> victimData = new HashMap<>(pyml.get(victim.getUniqueId()));
        String mode = getMode(victim);
        String session = getSession(victim);
        //加害者と被害者が一致した場合
        if (attacker != null && attacker.equals(victim)) {
            victimData.put("death", victimData.get("death") + 1);
            setPyml(victim, victimData);
            setKillstreak(session, victim, 0);
            return;
        }

        //加害者がいる場合
        if (attacker != null) {
            Map<String, Integer> attackerData = new HashMap<>(pyml.get(attacker.getUniqueId()));
            attackerData.put("kill", attackerData.get("kill") + 1);
            setPyml(attacker, attackerData);
            setTmpkills(session, attacker, getTmpkills(session).get(attacker) + 1);
            setKillstreak(session, attacker, getKillstreak(session).get(attacker) + 1);

            if (getKillstreak(session).get(attacker) >= 3) {
                int k = getKillstreak(session).get(attacker);
                if (k == 3 || k == 5 || k == 10) {
                    bcToList(getPlayers(getSession(attacker)), prefix + "§f<§7" + session + "§f> §a%player%が%kill_streak%キルストリークを達成！"
                            .replace("%player%", attacker.getName())
                            .replace("%prefix%", prefix)
                            .replace("%kill_streak%", String.valueOf(getKillstreak(session).get(attacker))));
                }
            }
        }

        //被害者の処理
        victimData.put("death", victimData.get("death") + 1);
        setPyml(victim, victimData);
        setKillstreak(session, victim, 0);
    }
}
