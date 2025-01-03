package com.beacmc.beacmcauth.core.social.telegram.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.core.social.telegram.TelegramProvider;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.Map;

public class LinkCommand {

    public static void process(BeacmcAuth plugin, TelegramProvider telegramProvider, Update update) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final LinkProvider linkProvider = telegramProvider.getLinkProvider();

        if (update.message() == null) return;

        String message = update.message().text();
        Long chatId = update.message().chat().id();
        if (message == null) return;

        String[] args = message.split("\\s+");

        if (!message.startsWith(telegramConfig.getLinkCommand()))
            return;

        if (args.length < 2) {
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-usage"));
            bot.execute(sendMessage);
            return;
        }

        String name = args[1].toLowerCase();

        plugin.getAuthManager().getProtectedPlayer(name).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-player-not-found"));
                bot.execute(sendMessage);
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (player == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-player-offline"));
                bot.execute(sendMessage);
                return;
            }

            if (protectedPlayer.getTelegram() != 0) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-command-already-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (linkProvider.getProtectedPlayersById(update.message().from().id()).size() < telegramConfig.getMaxLink()) {
                String code = CodeGenerator.generate(telegramConfig.getCodeChars(), telegramConfig.getCodeLength());
                linkProvider.getLinkCodes().put(protectedPlayer.setLinkCode(code), update.message().from().id());
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-message", Map.of("%name%", player.getName(), "%code%", code)));
                bot.execute(sendMessage);
                return;
            }
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("link-limit"));
            bot.execute(sendMessage);
        });
    }
}
