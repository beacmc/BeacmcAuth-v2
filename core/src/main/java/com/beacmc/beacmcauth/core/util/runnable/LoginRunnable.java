package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class LoginRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final AuthManager authManager;
    private final Integer maxTimeAuth;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private Integer timer;

    public LoginRunnable(BeacmcAuth plugin, ServerPlayer player) {
        this.player = player;
        this.plugin = plugin;
        Config config = plugin.getConfig();

        timer = 0;
        maxTimeAuth = config.getTimePerLogin();
        authManager = plugin.getAuthManager();

        title = config.getMessage("login-title");
        subtitle = config.getMessage("login-subtitle");
        message = config.getMessage("login-chat");
        in = 0;
        stay = 3;
        out = 0;

        task = plugin.getProxy().runTaskDelay(this, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (player == null) {
            task.cancel();
            return;
        }

        final Config config = plugin.getConfig();

        if (!authManager.getAuthPlayers().containsKey(player.getLowercaseName()) || !player.isConnected()) {
            task.cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessage("time-is-up"));
            authManager.getAuthPlayers().remove(player.getName().toLowerCase());
            task.cancel();
            return;
        }

        player.sendMessage(message);
        player.sendTitle(title, subtitle, in, stay, out);
    }
}
