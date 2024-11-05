package com.keisa1333.rosepvp.modes;

import com.keisa1333.rosepvp.Score;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.keisa1333.rosepvp.VariableMaps.PlayerStatus.PLAYING;
import static com.keisa1333.rosepvp.VariableMaps.SDTeams.DESTROYER;
import static com.keisa1333.rosepvp.VariableMaps.SDTeams.SEARCHER;
import static com.keisa1333.rosepvp.VariableMaps.StatusArena.GAMING;
import static com.keisa1333.rosepvp.VariableMaps.StatusBomb.*;
import static com.keisa1333.rosepvp.filemanager.PlayerData.*;
import static com.keisa1333.rosepvp.filemanager.SessionData.*;
import static com.keisa1333.rosepvp.utils.AssistantGame.*;
import static com.keisa1333.rosepvp.utils.Util.bcToList;
import static com.keisa1333.rosepvp.utils.Util.checkStatusArena;

public class SDListener implements Listener {

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Item) {
            ItemStack item = ((Item) e.getEntity()).getItemStack();
            if (isBomb(item)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (getMode(p) == null) return;
        if (!getMode(p).equalsIgnoreCase("sd")) return;
        Item item = e.getItemDrop();
        ItemStack itemStack = item.getItemStack();
        if (isBomb(itemStack)) {
            e.setCancelled(true);
            p.sendMessage("§c爆弾をドロップすることはできません。");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (getPlayerStatus(p).equals(PLAYING) && getMode(p).equalsIgnoreCase("sd")) {
            String session = getSession(p);
            String arena = getArena(p);
            String msg = event.getMessage();

            if (msg.equalsIgnoreCase("chat")) {
                event.setCancelled(true);
                setTeamChat(session, p, !getTeamChat(session, p));
                if (getTeamChat(session, p)) {
                    p.sendMessage("§aチームチャットを有効にしました。");
                } else {
                    p.sendMessage("§aチームチャットを無効にしました。");
                }
                return;
            }

            if (getTeamChat(session, p)) {
                event.setCancelled(true);

                if (getSDPlayers(session, SEARCHER).contains(p)) {
                    bcToList(getSDPlayers(session, SEARCHER), "§" + getColor(session, p) + "[" + arena.toUpperCase() + ".SEARCH]§f " + p.getName() + ": " + msg);
                } else if (getSDPlayers(session, DESTROYER).contains(p)) {
                    bcToList(getSDPlayers(session, SEARCHER), "§" + getColor(session, p) + "[" + arena.toUpperCase() + ".DESTROY]§f " + p.getName() + ": " + msg);
                }
            }
        }
    }


    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof Player p)) return;

        if (getPlayerStatus(p).equals(PLAYING)) {
            String session = getSession(p);
            if (getBomber(session) != null) return;

            // 拾ったアイテムを取得
            ItemStack pickedItem = event.getItem().getItemStack();

            // アイテムがカスタムTNTであるかどうかをチェック
            if (isBomb(pickedItem)) {

                if (getSDPlayers(session, SEARCHER).contains(p)) {
                    event.setCancelled(true);
                    return;
                }

                if (getSDPlayers(session, DESTROYER).contains(p)) {
                    setBomber(session, p);
                    removeDroppedBomb(session);

                    p.sendMessage("§aあなたがボンバーです。爆弾を設置しよう！");
                    bcToList(getSDPlayers(session, DESTROYER), "§a" + p.getName() + "がボンバーです！護衛して爆弾設置を補助しよう！");
                }
            }
        }
    }

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 4 * 50L; // 4 ticks in milliseconds

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        event.setCancelled(true);

        if (cooldowns.containsKey(playerId) && (System.currentTimeMillis() - cooldowns.get(playerId)) < COOLDOWN_TIME) return;
        cooldowns.put(playerId, System.currentTimeMillis());

        String session = getSession(player);
        if (session == null && !getMode(player).equalsIgnoreCase("sd")) return;

        if (!checkStatusArena(session, GAMING) || getStatusBomb(session).equals(UNDEFINED)) return;

        //設置
        ItemStack item = player.getInventory().getItemInMainHand();
        Location loc = event.getRightClicked().getLocation();

        if (isBomb(item)) {

            if (!getSDPlayers(session, DESTROYER).contains(player)) return;
            if (!getStatusBomb(session).equals(HAVING)) return;

//            getInformBomb(session).point = null;
//            getInformBomb(session).location = null;

            if (getBombLoc(session) == null || !loc.equals(getBombLoc(name))) {
                setCountPlace(session, 0);
                setCountPick(session, 0);
                setBombLoc(loc);
            }

            setCountPlace(session, getCountPlace(session) + 1);

            int count = getCountPlace(session);
            int newBombPlace = getBombPlace();
            double n = (double) count / newBombPlace * 10;
            n = Math.floor(n);

            StringBuilder msg = new StringBuilder("§a爆弾設置まで.. ");
            for (int i = 0; i < n; i++) {
                msg.append("§a§l■");
            }
            for (int i = 0; i < 10 - n; i++) {
                msg.append("§7§l□");
            }

            for (Player p : getPlayers(session)) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg.toString()));
            }

            //爆弾設置完了で設置判定
            if (count >= newBombPlace) {
                getBombLoc(name).getBlock().setType(Material.RED_STAINED_GLASS);
                setStatusBomb(session, PLACE);
                player.getInventory().setItemInMainHand(null);

                setCountPlace(session, 0);
                setCountPick(session, 0);

                new Score("sd", getArena(player), "playing", null, 0);
            }
        }

        //解除
        if (getSDPlayers(session, SEARCHER).contains(player)) {
            if (!getStatusBomb(session).equals(PLACE)) return;
            if (!loc.equals(getBombLoc(name))) return;

            setCountPick(session, getCountPick(session) + 1);

            int count = getCountPick(session);
            int newBombPick = getCountPick(session);
            double n = (double) count / newBombPick * 10;
            n = Math.floor(n);

            StringBuilder msg = new StringBuilder("§a爆弾解除まで.. ");
            for (int i = 0; i < n; i++) {
                msg.append("§a§l■");
            }
            for (int i = 0; i < 10 - n; i++) {
                msg.append("§7§l□");
            }

            for (Player p : getPlayers(session)) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg.toString()));
            }

            //爆弾解除完了で解除判定
            if (count >= newBombPick) {
                getBombLoc(name).getBlock().setType(Material.GLASS);
                setStatusBomb(session, PICK);

                setCountPlace(session, 0);
                setCountPick(session, 0);
                setBombLoc(null);
                new Score("sd", getArena(player), "playing", null, 0);
            }
        }
    }
}
