package com.beacmc.beacmcauth.core.social.telegram.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.core.social.telegram.TelegramProvider;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public class ConfirmationListener {

    public static void process(BeacmcAuth plugin, TelegramProvider telegramProvider, Update update) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final Config config = plugin.getConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final AuthManager authManager = plugin.getAuthManager();

        if (update.callbackQuery() == null) return;

        String callbackData = update.callbackQuery().data();
        Long chatId = update.callbackQuery().from().id();

        if (callbackData != null && callbackData.startsWith("confirm-accept-")) {
            String[] split = callbackData.split("confirm-accept-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!telegramProvider.getConfirmationUsers().containsKey(name)) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("no-confirmation"));
                bot.execute(sendMessage);
                return;
            }
            ProtectedPlayer protectedPlayer = telegramProvider.getConfirmationUsers().get(name);

            telegramProvider.getConfirmationUsers().remove(name);
            authManager.performLogin(protectedPlayer);
            ServerPlayer proxiedPlayer = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("confirmation-success"));
            bot.execute(sendMessage);

            if (proxiedPlayer != null) {
                proxiedPlayer.sendMessage(config.getMessage("discord-confirmation-success"));
                authManager.connectPlayer(proxiedPlayer, config.findServer(config.getGameServers()));
            }
        }

        if (callbackData != null && callbackData.startsWith("confirm-decline-")) {
            String[] split = callbackData.split("confirm-decline-");
            String name = split.length >= 1 ? split[1] : null;
            if (name == null) return;

            if (!telegramProvider.getConfirmationUsers().containsKey(name)) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("no-confirmation"));
                bot.execute(sendMessage);
                return;
            }

            ProtectedPlayer protectedPlayer = telegramProvider.getConfirmationUsers().get(name);
            telegramProvider.getConfirmationUsers().remove(name);
            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("confirmation-denied"));
            bot.execute(sendMessage);
            if (player != null) {
                player.disconnect(config.getMessage("discord-confirmation-denied"));
            }
        }
    }
}
