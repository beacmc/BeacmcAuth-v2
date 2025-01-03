package com.beacmc.beacmcauth.core.util.runnable;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.DiscordConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.social.SocialProvider;

import java.util.concurrent.TimeUnit;

public class DiscordRunnable implements Runnable {

    private final String title;
    private final String subtitle;
    private final Integer in, stay, out;
    private final String message;
    private final Integer maxTimeAuth;
    private final SocialProvider discordProvider;
    private final ServerPlayer player;
    private final BeacmcAuth plugin;
    private final TaskScheduler task;
    private Integer timer;

    public DiscordRunnable(BeacmcAuth plugin, ServerPlayer player) {
        this.player = player;
        this.plugin = plugin;
        this.discordProvider = plugin.getDiscordProvider();
        Config config = plugin.getConfig();
        DiscordConfig discordConfig = plugin.getDiscordConfig();

        timer = 0;

        maxTimeAuth = discordConfig.getTimePerConfirm();
        title = config.getMessage("discord-confirmation-title");
        subtitle = config.getMessage("discord-confirmation-subtitle");
        message = config.getMessage("discord-confirmation-chat");

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

        if (!discordProvider.getConfirmationUsers().containsKey(player.getLowercaseName()) || !player.isConnected()) {
            task.cancel();
            return;
        }

        timer += 1;

        if (timer >= maxTimeAuth) {
            player.disconnect(config.getMessage("time-is-up"));
            discordProvider.getConfirmationUsers().remove(player.getName().toLowerCase());
            task.cancel();
            return;
        }

        player.sendMessage(message);
        player.sendTitle(title, subtitle, in, stay, out);
    }
}
