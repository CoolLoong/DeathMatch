package com.github.coolloong.deathmatch;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.Task;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class EventListener implements Listener {
    private final DeathMatch plugin;
    public static HashMap<String, Integer> playerData = new HashMap<>();

    private final ConcurrentHashMap<String, Boolean> invincible = new ConcurrentHashMap<>();

    public EventListener(DeathMatch plugin) {
        this.plugin = plugin;
    }

    private boolean isWork(String worldName) {
        return plugin.getConfig().getStringList("world").contains(worldName);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        var player = e.getEntity();
        if (e.getDeathMessage().getText().equals("death.attack.player")) {
            var uuid = player.getUniqueId().toString();
            if (player.getMaxHealth() == 2) {
                e.setKeepInventory(false);
                e.setKeepExperience(false);
            } else {
                player.setMaxHealth(player.getMaxHealth() - 2);
                playerData.put(uuid, player.getMaxHealth());
            }
            invincible.put(uuid, true);
            plugin.getServer().getScheduler().scheduleDelayedTask(new Task() {
                @Override
                public void onRun(int currentTick) {
                    invincible.put(uuid, false);
                }
            }, plugin.getConfig().getInt("invincibleTime") * 20);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            if (isWork(player.getLevel().getName())) {
                var uuid1 = e.getEntity().getUniqueId().toString();
                var uuid2 = e.getDamager().getUniqueId().toString();
                if (invincible.containsKey(uuid1)) {
                    if (invincible.get(uuid1)) {
                        e.setCancelled(true);
                        return;
                    }
                }
                if (invincible.containsKey(uuid2)) {
                    if (invincible.get(uuid2)) {
                        e.setCancelled(true);
                        return;
                    }
                }
                var health = new BigDecimal(String.valueOf(e.getEntity().getHealth()));
                var damage = new BigDecimal(String.valueOf(e.getDamage()));
                var cmp = damage.compareTo(health);
                if (cmp >= 0) {
                    if (e.getEntity().getMaxHealth() == 2) {
                        return;
                    }
                    player.setMaxHealth(player.getMaxHealth() + 2);
                    playerData.put(player.getUniqueId().toString(), player.getMaxHealth());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        if (isWork(player.getLevel().getName())) {
            var uuid = player.getUniqueId().toString();
            if (playerData.containsKey(uuid)) {
                player.setMaxHealth(playerData.get(uuid));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityLevelChange(EntityLevelChangeEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (!isWork(e.getTarget().getName())) {
                player.setMaxHealth(20);
            } else {
                var uuid = player.getUniqueId().toString();
                if (playerData.containsKey(uuid)) {
                    player.setMaxHealth(playerData.get(uuid));
                }
            }
        }
    }

}
