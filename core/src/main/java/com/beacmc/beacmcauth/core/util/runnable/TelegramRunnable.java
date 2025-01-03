package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.social.SocialProvider;

import java.util.concurrent.TimeUnit;

public class TelegramRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final Integer maxTimeAuth;
    private final SocialProvider telegramProvider;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private Integer timer;

    public TelegramRunnable(BeacmcAuth plugin, ServerPlayer player) {
        this.player = player;
        this.plugin = plugin;
        this.telegramProvider = plugin.getTelegramProvider();
        Config config = plugin.getConfig();
        TelegramConfig telegramConfig = plugin.getTelegramConfig();

        timer = 0;

        maxTimeAuth = telegramConfig.getTimePerConfirm();
        title = config.getMessage("telegram-confirmation-title");
        subtitle = config.getMessage("telegram-confirmation-subtitle");
        message = config.getMessage("telegram-confirmation-chat");

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

        if (!telegramProvider.getConfirmationUsers().containsKey(player.getLowercaseName()) || !player.isConnected()) {
            task.cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessage("time-is-up"));
            telegramProvider.getConfirmationUsers().remove(player.getName().toLowerCase());
            task.cancel();
            return;
        }

        player.sendMessage(message);
        player.sendTitle(title, subtitle, in, stay, out);
    }
}
