package com.beacmc.beacmcauth.core.social.discord.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.DiscordConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.SocialProvider;
import com.beacmc.beacmcauth.core.social.discord.DiscordProvider;
import com.beacmc.beacmcauth.core.util.runnable.TelegramRunnable;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ConfirmationListener extends ListenerAdapter {

    private final AuthManager authManager;
    private final DiscordProvider discordProvider;
    private final BeacmcAuth plugin;
    private final SocialProvider telegram;

    public ConfirmationListener(BeacmcAuth plugin, DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        this.plugin = plugin;
        authManager = plugin.getAuthManager();
        telegram = plugin.getTelegramProvider();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        final Button button = event.getButton();
        final DiscordConfig discordConfig = plugin.getDiscordConfig();
        final Config config = plugin.getConfig();

        if (button.getId() != null && button.getId().startsWith("confirm-accept-")) {
            String[] split = button.getId().split("confirm-accept-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!discordProvider.getConfirmationUsers().containsKey(name)) {
                event.reply(discordConfig.getMessage("no-confirmation"))
                        .setEphemeral(true)
                        .queue();
                return;
            }
            ProtectedPlayer protectedPlayer = discordProvider.getConfirmationUsers().get(name);


            discordProvider.getConfirmationUsers().remove(name);
            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
            event.reply(discordConfig.getMessage("confirmation-success"))
                    .setEphemeral(true)
                    .queue();

            player.sendMessage(config.getMessage("discord-confirmation-success"));
            if (protectedPlayer.getTelegram() != 0 && telegram != null && protectedPlayer.isTelegramTwoFaEnabled()) {
                telegram.sendConfirmationMessage(protectedPlayer);
                telegram.getConfirmationUsers().put(protectedPlayer.getLowercaseName(), protectedPlayer);
                new TelegramRunnable(plugin, player);
                return;
            }

            authManager.performLogin(protectedPlayer);
            authManager.connectPlayer(player, config.findServer(config.getGameServers()));
        }

        if (button.getId() != null && button.getId().startsWith("confirm-decline-")) {
            String[] split = button.getId().split("confirm-decline-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!discordProvider.getConfirmationUsers().containsKey(name)) {
                event.reply(discordConfig.getMessage("no-confirmation"))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            ProtectedPlayer protectedPlayer = discordProvider.getConfirmationUsers().get(name);
            discordProvider.getConfirmationUsers().remove(name);
            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
            event.reply(discordConfig.getMessage("confirmation-denied"))
                    .setEphemeral(true)
                    .queue();

            if (player != null) {
                player.disconnect(config.getMessage("discord-confirmation-denied"));
            }
        }
    }
}
