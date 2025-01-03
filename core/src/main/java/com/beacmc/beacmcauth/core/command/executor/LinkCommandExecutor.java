package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.api.social.SocialProvider;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class LinkCommandExecutor implements CommandExecutor {

    private final SocialProvider discordProvider;
    private final LinkProvider discordLinkProvider;
    private final LinkProvider telegramLinkProvider;
    private final SocialProvider telegramProvider;
    private final ProtectedPlayerDao dao;
    private final BeacmcAuth plugin;

    public LinkCommandExecutor(BeacmcAuth plugin) {
        this.plugin = plugin;

        discordProvider = plugin.getDiscordProvider();
        telegramProvider = plugin.getTelegramProvider();

        discordLinkProvider = discordProvider != null ? discordProvider.getLinkProvider() : null;
        telegramLinkProvider = telegramProvider != null ? telegramProvider.getLinkProvider() : null;

        dao = plugin.getDatabase().getProtectedPlayerDao();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) {
            sender.sendMessage("Only player");
            return;
        }

        final Config config = plugin.getConfig();

        ProtectedPlayer protectedPlayer = null;

        if (discordLinkProvider != null) {
            protectedPlayer = discordLinkProvider.getPlayerByName(player.getName().toLowerCase());
        }

        if (protectedPlayer == null && telegramLinkProvider != null) {
            protectedPlayer = telegramLinkProvider.getPlayerByName(player.getName().toLowerCase());
        }

        if (protectedPlayer == null) {
            player.sendMessage(config.getMessage("link-code-absent"));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(config.getMessage("link-command-usage"));
            return;
        }

        String code = protectedPlayer.getLinkCode();
        long discord = discordLinkProvider != null ? discordLinkProvider.getLinkCodes().getOrDefault(protectedPlayer, 0L) : 0L;
        long telegram = telegramLinkProvider != null ? telegramLinkProvider.getLinkCodes().getOrDefault(protectedPlayer, 0L) : 0L;

        if (!code.equals(args[0])) {
            player.sendMessage(config.getMessage("incorrect-code"));
            return;
        }

        final ProtectedPlayer finalProtectPlayer = protectedPlayer;
        CompletableFuture.supplyAsync(() -> {
            try {
                player.sendMessage(config.getMessage("link-success"));

                if (telegramLinkProvider != null) {
                    telegramLinkProvider.getLinkCodes().remove(finalProtectPlayer);
                }
                if (discordLinkProvider != null) {
                    discordLinkProvider.getLinkCodes().remove(finalProtectPlayer);
                }

                if (discord != 0L) {
                    dao.createOrUpdate(finalProtectPlayer.setDiscord(discord));
                }
                if (telegram != 0L) {
                    dao.createOrUpdate(finalProtectPlayer.setTelegram(telegram));
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
