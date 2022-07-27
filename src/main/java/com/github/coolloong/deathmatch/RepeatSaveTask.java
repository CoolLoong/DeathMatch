package com.github.coolloong.deathmatch;

import cn.nukkit.utils.Utils;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class RepeatSaveTask implements Runnable {
    private final DeathMatch plugin;

    RepeatSaveTask(DeathMatch plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        var builder = new GsonBuilder();
        var gson = builder.create();
        try {
            Utils.writeFile(plugin.getDataFolder().getPath() + "\\playerdata.json", gson.toJson(EventListener.playerData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
