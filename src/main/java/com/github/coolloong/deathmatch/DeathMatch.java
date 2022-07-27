package com.github.coolloong.deathmatch;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Logger;
import cn.nukkit.utils.Utils;
import com.google.gson.GsonBuilder;
import io.leangen.geantyref.TypeToken;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.fusesource.jansi.Ansi.ansi;

public class DeathMatch extends PluginBase {
    Logger log;

    @Override
    public void onLoad() {
        log = new PluginLogger(this);
        this.saveDefaultConfig();
        var playerData = new File(this.getDataFolder().getPath() + "\\playerdata.json");
        if (playerData.exists() && playerData.length() != 0) {
            try {
                log.info(colorRGB(255, 255, 0, "载入玩家数据中..."));
                var data = Utils.readFile(playerData);
                var builder = new GsonBuilder();
                var gson = builder.create();
                EventListener.playerData = gson.fromJson(data, new TypeToken<HashMap<String, Integer>>() {
                }.getType());
                log.info(colorRGB(0, 255, 255, "载入完成!"));
            } catch (IOException e) {
                log.error("载入失败!");
                throw new RuntimeException(e);
            }
        }
        log.info(colorRGB(255, 255, 0, "DeathMatch插件启动成功!"));
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
        this.getServer().getScheduler().scheduleRepeatingTask(this, new RepeatSaveTask(this), this.getConfig().getInt("saveTime") * 1200);
    }

    @Override
    public void onDisable() {
        var builder = new GsonBuilder();
        var gson = builder.create();
        try {
            Utils.writeFile(this.getDataFolder().getPath() + "\\playerdata.json", gson.toJson(EventListener.playerData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String color(Ansi.Color color, Object o) {
        return ansi().fg(color).a(o.toString()).reset().toString();
    }

    public static String colorRGB(int r, int g, int b, Object o) {
        return ansi().fgRgb(r, g, b).a(o.toString()).reset().toString();
    }
}
