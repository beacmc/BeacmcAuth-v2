package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class RegisterRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String registerMessage;
    private final AuthManager authManager;
    private final Integer maxTimeAuth;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private final ServerLogger logger;
    private Integer timer;

    public RegisterRunnable(BeacmcAuth plugin, ServerPlayer player) {
        this.player = player;
        this.plugin = plugin;
        Config config = plugin.getConfig();

        timer = 0;
        maxTimeAuth = config.getTimePerRegister();
        authManager = plugin.getAuthManager();

        title = config.getMessage("register-title");
        subtitle = config.getMessage("register-subtitle");
        registerMessage = config.getMessage("register-chat");
        in = 0;
        stay = 3;
        out = 0;

        logger = plugin.getServerLogger();
        task = plugin.getProxy().runTaskDelay(this, 1, TimeUnit.SECONDS);
        logger.debug("RegisterRunnable has started for player(" + player + ")");
    }

    @Override
    public void run() {
        if (player == null) {
            task.cancel();
            return;
        }

        final Config config = plugin.getConfig();

        if (!authManager.getAuthPlayers().containsKey(player.getName().toLowerCase()) || !player.isConnected()) {
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

        player.sendMessage(registerMessage);
        player.sendTitle(title, subtitle, in, stay, out);
    }
}
