package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.SocialProvider;
import com.beacmc.beacmcauth.core.util.runnable.DiscordRunnable;
import com.beacmc.beacmcauth.core.util.runnable.TelegramRunnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LoginCommandExecutor implements CommandExecutor {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;
    private final SocialProvider discord;
    private final SocialProvider telegram;

    public LoginCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.discord = plugin.getDiscordProvider();
        this.telegram = plugin.getTelegramProvider();
        this.authManager = plugin.getAuthManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) {
            sender.sendMessage("Only player");
            return;
        }

        final Config config = plugin.getConfig();

        if (!authManager.getAuthPlayers().containsKey(player.getName().toLowerCase())) {
            player.sendMessage(config.getMessage("already-authed"));
            return;
        }

        final CompletableFuture<ProtectedPlayer> future = authManager.getProtectedPlayer(player.getLowercaseName());

        future.thenAccept(protectedPlayer -> {
            if (!protectedPlayer.isRegister()) {
                player.sendMessage(config.getMessage("not-register"));
                return;
            }

            if (args.length < 1) {
                player.sendMessage(config.getMessage("enter-password"));
                return;
            }

            if (!protectedPlayer.checkPassword(args[0])) {
                int attempts = authManager.getAuthPlayers().get(protectedPlayer.getLowercaseName());
                authManager.getAuthPlayers().put(protectedPlayer.getLowercaseName(), attempts - 1);

                player.sendMessage(config.getMessage("wrong-password", Map.of("%attempts%", String.valueOf(attempts - 1))));
                if (authManager.getAuthPlayers().get(protectedPlayer.getLowercaseName()) <= 0) {
                    player.disconnect(config.getMessage("attempts-left"));
                }
                return;
            }

            player.sendMessage(config.getMessage("login-success"));
            authManager.getAuthPlayers().remove(protectedPlayer.getLowercaseName());

            if (protectedPlayer.getDiscord() != 0 && discord != null && protectedPlayer.isDiscordTwoFaEnabled()) {
                discord.sendConfirmationMessage(protectedPlayer);
                discord.getConfirmationUsers().put(protectedPlayer.getLowercaseName(), protectedPlayer);
                new DiscordRunnable(plugin, player);
                return;
            }

            if (protectedPlayer.getTelegram() != 0 && telegram != null && protectedPlayer.isTelegramTwoFaEnabled()) {
                telegram.sendConfirmationMessage(protectedPlayer);
                telegram.getConfirmationUsers().put(protectedPlayer.getLowercaseName(), protectedPlayer);
                new TelegramRunnable(plugin, player);
                return;
            }

            authManager.performLogin(protectedPlayer);
            authManager.connectPlayer(player, config.findServer(config.getGameServers()));
        });
    }
}
