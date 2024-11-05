package com.mugen.bloodrose.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mugen.bloodrose.VariableMaps.arenas;
import static com.mugen.bloodrose.VariableMaps.sessions;
import static com.mugen.bloodrose.filemanager.SessionData.getSpawns;
import static com.mugen.bloodrose.filemanager.loader.ArenaLoader.loadArenas;
import static com.mugen.bloodrose.filemanager.loader.ConfigLoader.modes;
import static com.mugen.bloodrose.utils.Util.isBoot;

public class TabComplete implements TabCompleter {


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String a, String[] args) {
        String cmdName = cmd.getName().toLowerCase();
        if (!cmdName.equals("rosepvp")
                && !modes.contains(cmdName)
                && !cmdName.equals("leave")
                && !cmdName.equals("menu")) return null;

        if (!(sender instanceof Player p)) return null;

        if ("leave".startsWith(cmdName)) {
            return Collections.singletonList("leave");
        } else if ("menu".startsWith(cmdName)) {
            return Collections.singletonList("menu");
        }

        for (String mode : modes) {
            if (args.length == 0) {
                if (mode.startsWith(cmdName)) {
                    return Collections.singletonList(mode);
                }
            } else if (args.length == 1) {
                if (p.isOp()) {
                    if (cmdName.equals(mode)) {
                        List<String> list = arenas.get(mode);
                        return list != null ? list : Collections.emptyList();
                    }
                }
            }
        }

        if (!cmdName.equals("rosepvp")) return null;

        if (!sender.isOp()) return null;


        if (args.length > 0) {
            if (args[0].isEmpty()) {
                return Arrays.asList("help", "team", "status", "join", "reload", "arena", "spawn", "chat", "end", "cancel", "skip", "point", "gametoggle");

            } else if ("arena".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("arena");

                } else if (args.length == 2) {
                    if (args[1].isEmpty()) {
                        return Arrays.asList("list", "del", "check", "gui", "info");
                    } else if ("list".startsWith(args[1])) {
                        return Collections.singletonList("list");
                    } else if ("del".startsWith(args[1])) {
                        return Collections.singletonList("del");
                    } else if ("check".startsWith(args[1])) {
                        return Collections.singletonList("check");
                    } else if ("gui".startsWith(args[1])) {
                        return Collections.singletonList("gui");
                    } else if ("info".startsWith(args[1])) {
                        return Collections.singletonList("info");
                    }

                } else if (args.length == 3) {
                    if (args[2].isEmpty()) {
                        return modes;
                    } else {
                        for (String mode : modes) {
                            if (mode.startsWith(args[2])) {
                                return Collections.singletonList(mode);
                            }
                        }
                    }

                } else if (args.length == 4) {
                    List<String> list = arenas.get(args[2]);
                    if (list == null) {
                        return null;
                    }

                    if (args[3].isEmpty()) {
                        return list;
                    } else {
                        for (String arena : list) {
                            if (arena.startsWith(args[3])) {
                                return Collections.singletonList(arena);
                            }
                        }
                    }
                }

            } else if ("team".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("team");
                }

            } else if ("help".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("help");
                }

            } else if ("join".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("join");

                } else if (args.length == 2) {
                    if (args[1].isEmpty()) {
                        return modes;
                    } else {
                        for (String mode : modes) {
                            if (mode.startsWith(args[1])) {
                                return Collections.singletonList(mode);
                            }
                        }
                    }

                } else if (args.length == 3) {
                    List<String> list = arenas.get(args[1]);
                    if (list == null) {
                        return null;
                    }

                    if (args[2].isEmpty()) {
                        return list;
                    } else {
                        for (String arena : list) {
                            if (arena.startsWith(args[2])) {
                                return Collections.singletonList(arena);
                            }
                        }
                    }
                }

            } else if ("end".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("end");

                } else if (args.length == 2) {
                    if (args[1].isEmpty()) {
                        return modes;
                    } else {
                        for (String mode : modes) {
                            if (mode.startsWith(args[1])) {
                                return Collections.singletonList(mode);
                            }
                        }
                    }

                } else if (args.length == 3) {
                    List<String> list = arenas.get(args[1]);
                    if (list == null) {
                        return null;
                    }

                    if (args[2].isEmpty()) {
                        return list;
                    } else {
                        for (String arena : list) {
                            if (arena.startsWith(args[2])) {
                                return Collections.singletonList(arena);
                            }
                        }
                    }
                }

            } else if ("gametoggle".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("gametoggle");
                }

            } else if ("reload".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("reload");
                }

            } else if ("c".startsWith(args[0])) {
                if (args.length == 1) {
                    return Arrays.asList("chat", "cancel");
                }

            } else if ("chat".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("chat");
                }

            } else if ("cancel".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("cancel");
                } else if (args.length == 2) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                }

            } else if ("s".startsWith(args[0])) {
                if (args.length == 1) {
                    return Arrays.asList("spawn", "status", "skip");
                }

            } else if ("status".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("status");

                } else if (args.length == 2) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                }

            } else if ("skip".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("skip");

                } else if (args.length == 2) {
                    if (args[1].isEmpty()) {
                        return modes;
                    } else {
                        for (String mode : modes) {
                            if (mode.startsWith(args[1])) {
                                return Collections.singletonList(mode);
                            }
                        }
                    }

                } else if (args.length == 3) {
                    List<String> list = arenas.get(args[1]);
                    if (list == null) {
                        return null;
                    }

                    if (args[2].isEmpty()) {
                        return list;
                    } else {
                        for (String arena : list) {
                            if (arena.startsWith(args[2])) {
                                return Collections.singletonList(arena);
                            }
                        }
                    }
                }

            } else if ("spawn".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("spawn");

                } else if (args.length == 2) {
                    if (args[1].isEmpty()) {
                        return Arrays.asList("add", "del", "tp");
                    } else if ("add".startsWith(args[1])) {
                        return Collections.singletonList("add");
                    } else if ("del".startsWith(args[1])) {
                        return Collections.singletonList("del");
                    } else if ("tp".startsWith(args[1])) {
                        return Collections.singletonList("tp");
                    }

                } else if (args.length == 3) {
                    if (args[2].isEmpty()) {
                        return modes;
                    } else {
                        for (String mode : modes) {
                            if (mode.startsWith(args[2])) {
                                return Collections.singletonList(mode);
                            }
                        }
                    }

                } else if (args.length == 4) {
                    List<String> list = arenas.get(args[2]);
                    if (list == null) {
                        return null;
                    }

                    if (args[3].isEmpty()) {
                        return list;
                    } else {
                        for (String arena : list) {
                            if (arena.startsWith(args[3])) {
                                return Collections.singletonList(arena);
                            }
                        }
                    }

                } else if (args.length == 5) {
                    if ("del".startsWith(args[1]) || "tp".startsWith(args[1])) {
                        loadArenas(args[2].toLowerCase(), args[3].toLowerCase());

                        String mode = args[2].toLowerCase();
                        String arena = args[3].toLowerCase();
                        String session = mode + arena;
                        boolean bool = isBoot(mode, arena);

                        List<Location> spawns = getSpawns(session);
                        if (spawns == null) {
                            return null;
                        }

                        int index = spawns.size();
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < index; i++) {
                            list.add(String.valueOf(i));
                        }
                        if (!bool) {
                            sessions.remove(session);
                        }
                        return list;
                    }
                }

            } else if ("point".startsWith(args[0])) {
                if (args.length == 1) {
                    return Collections.singletonList("point");

                } else if (args.length == 2) {
                    if (args[1].isEmpty()) {
                        return Arrays.asList("confirm", "cancel", "add", "del", "tp", "set", "global", "respawn");
                    } else if ("c".startsWith(args[1])) {
                        return Arrays.asList("confirm", "cancel");
                    } else if ("confirm".startsWith(args[1])) {
                        return Collections.singletonList("confirm");
                    } else if ("cancel".startsWith(args[1])) {
                        return Collections.singletonList("cancel");
                    } else if ("add".startsWith(args[1])) {
                        return Collections.singletonList("add");
                    } else if ("del".startsWith(args[1])) {
                        return Collections.singletonList("del");
                    } else if ("tp".startsWith(args[1])) {
                        return Collections.singletonList("tp");
                    } else if ("global".startsWith(args[1])) {
                        return Collections.singletonList("global");
                    } else if ("respawn".startsWith(args[1])) {
                        return Collections.singletonList("respawn");
                    }

                } else if (args.length == 3) {
                    String[] MODE_SPOT = {"sd", "dom", "cq"};
                    if (!"cancel".startsWith(args[1]) && !"confirm".startsWith(args[1])) {
                        if (args[2].isEmpty()) {
                            return List.of(MODE_SPOT);
                        } else {
                            for (String mode : MODE_SPOT) {
                                if (mode.startsWith(args[2])) {
                                    return Collections.singletonList(mode);
                                }
                            }
                        }
                    }

                } else if (args.length == 4) {
                    if (!"cancel".startsWith(args[1]) && !"confirm".startsWith(args[1])) {
                        List<String> list = arenas.get(args[2]);
                        if (list == null) {
                            return null;
                        }

                        if (args[3].isEmpty()) {
                            return list;
                        } else {
                            for (String arena : list) {
                                if (arena.startsWith(args[3])) {
                                    return Collections.singletonList(arena);
                                }
                            }
                        }
                    }

                } else if (args.length == 5) {
                    if ("del".startsWith(args[1]) || "tp".startsWith(args[1])) {
                        return Collections.singletonList("#spot");
                    }

                } else if (args.length == 6) {
                    if (args[1].isEmpty()) {
                        return Arrays.asList("bombDrop", "bombPoints", "destroyer", "searcher");
                    } else if ("bomb".startsWith(args[1])) {
                        return Arrays.asList("bombDrop", "bombPoints");
                    } else if ("bombDrop".startsWith(args[1])) {
                        return Collections.singletonList("bombDrop");
                    } else if ("bombPoints".startsWith(args[1])) {
                        return Collections.singletonList("bombPoints");
                    } else if ("destroyer".startsWith(args[1])) {
                        return Collections.singletonList("destroyer");
                    } else if ("searcher".startsWith(args[1])) {
                        return Collections.singletonList("searcher");
                    }
                }

            }
        }
        return Collections.emptyList();
    }
}
