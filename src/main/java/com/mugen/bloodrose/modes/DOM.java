package com.mugen.bloodrose.modes;//package com.mugen.bloodrose.games;
//
//import com.mugen.bloodrose.Score;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Location;
//import org.bukkit.boss.BarColor;
//import org.bukkit.boss.BarStyle;
//import org.bukkit.entity.Player;
//import org.bukkit.event.Listener;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.scoreboard.Team;
//
//import java.util.*;
//
//import static com.mugen.bloodrose.utils.FastBossbar.addPlayerBossBar;
//import static com.mugen.bloodrose.utils.FastBossbar.createBossBar;
//import static com.mugen.bloodrose.utils.FastTeam.initializeTeam;
//import static com.mugen.bloodrose.utils.FastTeam.joinTeam;
//import static com.mugen.bloodrose.RosePvP.getInstance;
//import static com.mugen.bloodrose.utils.Util.*;
//import static com.mugen.bloodrose.commands.CheckTeam.checkTeam;
//import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.getTimeGame;
//import static com.mugen.bloodrose.filemanager.loader.LangLoader.prefix;
//import static com.mugen.bloodrose.filemanager.PlayerData.PlayerRank;
//import static com.mugen.bloodrose.filemanager.PlayerData.PlayerStatus.playing;
//import static com.mugen.bloodrose.VariableMaps.*;
//import static com.mugen.bloodrose.filemanager.arenamanager.ArenaData.Status.abort;
//import static com.mugen.bloodrose.filemanager.arenamanager.ArenaData.Status.goon;
//import static com.mugen.bloodrose.filemanager.arenamanager.ArenaData.*;
//import static com.mugen.bloodrose.filemanager.arenamanager.SessionDOMData.*;
//import static com.mugen.bloodrose.games.Game.*;
//import static com.mugen.bloodrose.Score.changeProgressScore;
//import static org.bukkit.Material.*;
//
//public class DOM implements Listener {
//    private static final String mode = "dom";
//    private final String arena;
//    private static String name;
//    private static List<Player> players;
//
//    private static final int RATE_TIME = 20;
//    static final int MAX_POINT_DOM = 200;
//    static final int POINT_OF_WIN = 800;
//    static final Map<Player, String> whereIsPlayerIn = new HashMap<>();
//    private static final Map<String, Integer> amountOfControl = new HashMap<>();
//    static final Map<String, Integer> pointOfDisplay = new HashMap<>();
//    public DOM(String arena) {
//        this.arena = arena;
//        name = mode + arena;
//        players = getPlayers(name);
//
//        divideTeamDOM();
//        initDOM();
//        progressDOM();
//        for (String point : getPoints(name).keySet()) {
//            progressDOMPoint(point);
//        }
//    }
//
//    private void divideTeamDOM() {
//        var tmpPlayers = new ArrayList<>(players);
//        int loop = players.size() / 2;
//        var tmpLList = new ArrayList<>(getSpawns(name));
//
//        initializeTeam(name + "red", ChatColor.RED, false, Team.OptionStatus.FOR_OTHER_TEAMS);
//        initializeTeam(name + "blue", ChatColor.BLUE, false, Team.OptionStatus.FOR_OTHER_TEAMS);
//        Random r = new Random();
//
//        // 赤チーム選出
//        var reds = new ArrayList<Player>();
//        int index = r.nextInt(tmpLList.size());
//        Location l = tmpLList.get(index);
//        tmpLList.remove(l);
//        for (int i = 0; i < loop; i++) {
//            int randomIndexP = new Random().nextInt(tmpPlayers.size());
//            Player p = tmpPlayers.get(randomIndexP);
//            reds.add(p);
//            tmpPlayers.remove(p);
//
//            joinTeam(name + "red", p);
//            Location finalL1 = l;
//            runTask(() -> p.teleport(finalL1));
//        }
//        arenaManager.get(name).setReds(reds);
//
//        // 残りから青チーム設定
//        index = r.nextInt(tmpLList.size());
//        l = tmpLList.get(index);
//        for (Player p : tmpPlayers) {
//            joinTeam(name + "blue", p);
//            Location finalL = l;
//            runTask(() -> p.teleport(finalL));
//        }
//        arenaManager.get(name).setBlues(tmpPlayers);
//
//        for (Player p : players) {
//            checkTeam(p);
//        }
//    }
//
//    private void initDOM() {
//        pointOfDisplay.put(name + "red", 0);
//        pointOfDisplay.put(name + "blue", 0);
//        amountOfControl.put(name + "red", 0);
//        amountOfControl.put(name + "blue", 0);
//
//        reserveArena.put(mode, null);
//        arenaManager.get(name).setStatusArena(goon);
//        new Score(mode, arena, "playing", "setup", null, 0);
//
//        createBossBar(name, "§e§l" + mode.toUpperCase() + "モード開催中！", BarColor.BLUE, BarStyle.SEGMENTED_6, 1.0);
//
//        for (Player p : players) {
//            addPlayerBossBar(name, p);
//            openGUI("loadloadout", p);
//
//            initializePlayer(p);
//            whereIsPlayerIn.put(p, null);
//            playerManager.get(p).setStatusPlayer(playing);
//        }
//        bcToList(players, prefix + "§bモードDOM、アリーナ" + arena + "開始！");
//    }
//
//    private void progressDOM() {
//
//        new BukkitRunnable() {
//            int ticks = 0;
//            final int totalSeconds = RATE_TIME * getTimeGame().get(mode);
//
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks % RATE_TIME == 0) {
//                    countdown(mode, arena, ticks / RATE_TIME, totalSeconds / RATE_TIME, players);
//                }
//
//                if (getStatusArena(name).equals(abort)) {
//                    cancel();
//                    gameEnd(mode, arena);
//                }
//
//                changeProgressScore(name);
//
//                if (ticks >= totalSeconds) {
//                    cancel();
//                    gameEnd(mode, arena);
//                }
//            }
//        }.runTaskTimer(getInstance(), 0L, (1L / RATE_TIME));
//    }
//
//    private void progressDOMPoint(String point) {
//        Location loc = getPoints(name).get(point);
//        Random random = new Random();
//
//        List<Location> woolList = new ArrayList<>();
//        Location tmpLoc = loc.clone().add(-2, 0, -2);
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 5; j++) {
//                woolList.add(tmpLoc.clone().add(i, 0, j));
//            }
//        }
//        woolList.remove(loc);
//
//        Bukkit.getScheduler().runTask(getInstance(), () -> loc.clone().add(0, 1, 0).getBlock().setType(GLASS));
//        for (Location l : woolList) {
//            Bukkit.getScheduler().runTask(getInstance(), () -> l.getBlock().setType(WHITE_WOOL));
//        }
//
//        new BukkitRunnable() {
//            int ticks = 0;
//            final int totalSeconds = RATE_TIME * getTimeGame().get(mode);
//            boolean bool = false;
//            double count = 0.0;
//            int tickBlue = 0;
//            int tickRed = 0;
//
//            @Override
//            public void run() {
//                ticks++;
//                if (getStatusArena(name).equals(abort)) {
//                    cancel();
//                }
//
//                for (Player p : players) {
//                    double distance = p.getLocation().distance(loc);
//                    if (distance <= 3) {
//                        if (whereIsPlayerIn.get(p) == null) {
//                            whereIsPlayerIn.put(p, point);
//                            if (getReds(name).contains(p)) {
//                                setAmountOfPersonInPoint(name, point, "red", getAmountOfPersonInPoint(name, point, "red") + 1);
//                            } else if (getBlues(name).contains(p)) {
//                                setAmountOfPersonInPoint(name, point, "blue", getAmountOfPersonInPoint(name, point, "blue") + 1);
//                            }
//                        }
//                    } else {
//                        if (whereIsPlayerIn.get(p) != null && whereIsPlayerIn.get(p).equalsIgnoreCase(point)) {
//                            whereIsPlayerIn.put(p, null);
//                            if (getReds(name).contains(p)) {
//                                setAmountOfPersonInPoint(name, point, "red", getAmountOfPersonInPoint(name, point, "red") - 1);
//                            } else if (getBlues(name).contains(p)) {
//                                setAmountOfPersonInPoint(name, point, "blue", getAmountOfPersonInPoint(name, point, "blue") - 1);
//                            }
//                        }
//                    }
//                }
//
//                int r = getAmountOfPersonInPoint(name, point, "red");
//                int b = getAmountOfPersonInPoint(name, point, "blue");
//
//                if (r > b) {
//                    if (ticks % 5 == 0) {
//                        if (bool) {
//                            Bukkit.getScheduler().runTask(getInstance(), () -> loc.clone().add(0, 1, 0).getBlock().setType(RED_STAINED_GLASS));
//                            for (Location woolL : woolList) {
//                                Bukkit.getScheduler().runTask(getInstance(), () -> woolL.getBlock().setType(RED_WOOL));
//                            }
//                        } else {
//                            Location l = woolList.get(random.nextInt(woolList.size()));
//                            Bukkit.getScheduler().runTask(getInstance(), () -> l.getBlock().setType(RED_WOOL));
//                        }
//                    }
//
//                    count = switch (b) {
//                        case 1 -> 2.0;
//                        case 2 -> 3.2;
//                        default -> 5.0;
//                    };
//
//                    if (getProgressDominate(name, point) < MAX_POINT_DOM) {
//                        if (bool && 0 > getProgressDominate(name, point)) {
//                            amountOfControl.put(name + "blue", amountOfControl.get(name + "blue") - 1);
//                            bcToList(players, "§c赤チームが" + point + "を制圧中です...(§c" + amountOfControl.get(name + "red") + " §f: §9" + amountOfControl.get(name + "blue") + "§a)");
//                            bool = false;
//                        }
//                        domManager.get(name).setProgressDominate(point, getProgressDominate(name, point) + count);
//                    } else if (getProgressDominate(name, point) >= MAX_POINT_DOM) {
//                        if (!bool) {
//                            amountOfControl.put(name + "red", amountOfControl.get(name + "red") + 1);
//                            bcToList(players, "§c赤チームが" + point + "を占拠しました！(§c" + amountOfControl.get(name + "red") + " §f: §9" + amountOfControl.get(name + "blue") + "§a)");
//                            for (Player p : players) {
//                                p.sendTitle("§c占拠完了: [" + point + "]", "", 1, 20, 1);
//                            }
//                            Bukkit.getScheduler().runTask(getInstance(), () -> {
//                                Objects.requireNonNull(loc.getWorld()).strikeLightning(loc);
//                            });
//                            bool = true;
//                        }
//                        domManager.get(name).setProgressDominate(point, MAX_POINT_DOM);
//                    }
//
//                } else if (b > r) {
//                    if (ticks % 5 == 0) {
//                        if (bool) {
//                            Bukkit.getScheduler().runTask(getInstance(), () -> loc.clone().add(0, 1, 0).getBlock().setType(BLUE_STAINED_GLASS));
//                            for (Location woolL : woolList) {
//                                Bukkit.getScheduler().runTask(getInstance(), () -> woolL.getBlock().setType(BLUE_WOOL));
//                            }
//                        } else {
//                            Location l = woolList.get(random.nextInt(woolList.size()));
//                            Bukkit.getScheduler().runTask(getInstance(), () -> l.getBlock().setType(BLUE_WOOL));
//                        }
//                    }
//
//                    count = switch (b) {
//                        case 1 -> 2.0;
//                        case 2 -> 3.2;
//                        default -> 5.0;
//                    };
//
//                    if (getProgressDominate(name, point) > -MAX_POINT_DOM) {
//                        if (bool && 0 < getProgressDominate(name, point)) {
//                            amountOfControl.put(name + "red", amountOfControl.get(name + "red") - 1);
//                            bcToList(players, "§9青チームが" + point + "を制圧中です...(§c" + amountOfControl.get(name + "red") + " §f: §9" + amountOfControl.get(name + "blue") + "§a)");
//                            bool = false;
//                        }
//                        domManager.get(name).setProgressDominate(point, getProgressDominate(name, point) - count);
//                    } else if (getProgressDominate(name, point) <= -MAX_POINT_DOM) {
//                        if (!bool) {
//                            amountOfControl.put(name + "blue", amountOfControl.get(name + "blue") + 1);
//                            bcToList(players, "§9青チームが" + point + "を占拠しました！(§c" + amountOfControl.get(name + "red") + " §f: §9" + amountOfControl.get(name + "blue") + "§a)");
//                            for (Player p : players) {
//                                p.sendTitle("§9占拠完了: [" + point + "]", "", 1, 20, 1);
//                            }
//                            Bukkit.getScheduler().runTask(getInstance(), () -> {
//                                Objects.requireNonNull(loc.getWorld()).strikeLightning(loc);
//                            });
//                            bool = true;
//                        }
//                        domManager.get(name).setProgressDominate(point, -MAX_POINT_DOM);
//                    }
//                }
//
//                if (bool) {
//                    tickBlue++;
//                    if (tickBlue >= 20) {
//                        tickBlue = 0;
//                        pointOfDisplay.put(name + "blue", pointOfDisplay.get(name + "blue") + 1);
//                        if (pointOfDisplay.get(name + "blue") >= POINT_OF_WIN) {
//                            gameEnd(mode, arena);
//                            cancel();
//                        }
//                    }
//                    tickRed++;
//                    if (tickRed >= 100) {
//                        tickRed = 0;
//                        pointOfDisplay.put(name + "red", pointOfDisplay.get(name + "red") + 1);
//                        if (pointOfDisplay.get(name + "red") >= POINT_OF_WIN) {
//                            gameEnd(mode, arena);
//                            cancel();
//                        }
//                    }
//                }
//
//                if (ticks >= totalSeconds) {
//                    cancel();
//                }
//            }
//        }.runTaskTimerAsynchronously(getInstance(), 0L, (1L / RATE_TIME));
//    }
//
//    public static void rankDOM(String name) {
//        final int TIMER_THREE = 20 * 3;
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                int red = pointOfDisplay.get(name + "red");
//                int blue = pointOfDisplay.get(name + "blue");
//                bcToList(players, "§c" + red + " §f: §9" + blue + "§fで...");
//
//                String msg;
//                if (blue == 0 && red == 0) {
//                    msg = "§e誰も占拠しませんでした";
//                } else if (blue > red) {
//                    msg = "§9青勝利！！";
//                    for (Player p : getBlues(name)) {
//                        playerManager.get(p).setRank(PlayerRank.domWin);
//                    }
//                } else if (red > blue) {
//                    msg = "§c赤勝利！！";
//                    for (Player p : getReds(name)) {
//                        playerManager.get(p).setRank(PlayerRank.domWin);
//                    }
//                } else {
//                    msg = "§e引き分けです";
//                }
//                bcToList(players, msg);
//
//                new BukkitRunnable() {
//                    @Override
//                    public void run() {
//                        finalizeGame(name);
//                    }
//                }.runTaskLater(getInstance(), TIMER_THREE);
//            }
//        }.runTaskLater(getInstance(), TIMER_THREE);
//
//    }
//}
