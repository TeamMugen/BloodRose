package com.mugen.bloodrose.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class FastTeam {

    private static final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private static final Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

    public static void initializeTeam(String name, ChatColor color, boolean friendFire, Team.OptionStatus visibility) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }

        team.setColor(color);
        team.setAllowFriendlyFire(friendFire);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, visibility);
    }

    public static void removeTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.unregister();
        }
    }

    public static void joinTeam(String name, Player player) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.addEntry(player.getName());
        }
    }

    public static List<Player> getTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            List<Player> players = new ArrayList<>();
            for (String entry : team.getEntries()) {
                Player player = Bukkit.getPlayer(entry);
                if (player != null) {
                    players.add(player);
                }
            }
            return players;
        }
        return new ArrayList<>(); // ここでnullを返すのではなく、空のリストを返す
    }

    public static void leaveTeamGeneral(Player p) {
        Team team = scoreboard.getPlayerTeam(p);
        if (team != null) {
            team.removeEntry(p.getName());
        }
    }

    public static void leaveTeam(String name, Player player) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.removeEntry(player.getName());
        }
        team = scoreboard.getTeam(name + "red");
        if (team != null) {
            team.removeEntry(player.getName());
        }
        team = scoreboard.getTeam(name + "blue");
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    public static void setCollision(String name, Team.OptionStatus collision) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setOption(Team.Option.COLLISION_RULE, collision);
        }
    }

    public static void setColor(String name, ChatColor color) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setColor(color);
        }
    }

    public static void setDeathMessageVisibility(String name, Team.OptionStatus visibility) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, visibility);
        }
    }

    public static void setDisplayName(String name, String newName) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setDisplayName(newName);
        }
    }

    public static void setFriendlyFire(String name, boolean bool) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setAllowFriendlyFire(bool);
        }
    }

    public static void setNametagVisibility(String name, Team.OptionStatus visibility) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, visibility);
        }
    }

    public static void setSeeFriendlyInvisibles(String name, boolean bool) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.setCanSeeFriendlyInvisibles(bool);
        }
    }
}
