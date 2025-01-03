package com.beacmc.beacmcauth.core.social.telegram.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.core.social.telegram.TelegramProvider;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AccountsCommand {

    public static void process(BeacmcAuth plugin, TelegramProvider telegramProvider, Update update) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final LinkProvider linkProvider = telegramProvider.getLinkProvider();

        if (update.message() == null) return;

        String message = update.message().text();
        Long chatId = update.message().chat().id();
        if (message == null) return;

        if (!message.startsWith(telegramConfig.getAccountsCommand()))
            return;

        List<ProtectedPlayer> players = linkProvider.getProtectedPlayersById(update.message().from().id());
        if (players.size() < 1) {
            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("accounts-empty"));
            bot.execute(sendMessage);
            return;
        }

        sendPlayerButtons(plugin, bot, chatId, players, 0);
    }

    private static void sendPlayerButtons(BeacmcAuth plugin, TelegramBot bot, Long chatId, List<ProtectedPlayer> players, int page) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        int start = page * 4;
        int end = Math.min(start + 4, players.size());
        List<ProtectedPlayer> pageContent = players.subList(start, end);

        if (pageContent.size() < 1) return;
        SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("choose-account"));
        List<InlineKeyboardButton> pageButtons = new LinkedList<>();

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        for (ProtectedPlayer player : pageContent) {
            inlineKeyboard.addRow(new InlineKeyboardButton(player.getRealName()).callbackData("account:" + player.getLowercaseName()));
        }

        InlineKeyboardButton previousButton = new InlineKeyboardButton("⏪").callbackData("previous:" + page);
        InlineKeyboardButton nextButton = new InlineKeyboardButton("⏩").callbackData("next:" + page);

        if (page > 0) pageButtons.add(previousButton);
        if (end < players.size()) pageButtons.add(nextButton);

        inlineKeyboard.addRow(pageButtons.toArray(new InlineKeyboardButton[0]));

        sendMessage.replyMarkup(inlineKeyboard);
        bot.execute(sendMessage);
    }

    public static void processButtons(BeacmcAuth plugin, TelegramProvider telegramProvider, Update update) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final LinkProvider linkProvider = telegramProvider.getLinkProvider();

        if (update.callbackQuery() == null) return;

        String callbackData = update.callbackQuery().data();
        Long chatId = update.callbackQuery().from().id();
        String[] args = callbackData.split(":");

        if (args.length != 2) return;

        String action = args[0];
        String id = args[1];

        if (action.equals("account")) {
            CompletableFuture<ProtectedPlayer> completableFuture = plugin.getAuthManager().getProtectedPlayer(id.toLowerCase());
            completableFuture.thenAccept(protectedPlayer -> {
                if (protectedPlayer == null) {
                    System.out.println("Account is null");
                    return;
                }

                if (protectedPlayer.getTelegram() != chatId) {
                    SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                    bot.execute(sendMessage);
                    return;
                }


                String playerOnline = telegramConfig.getMessage("player-info-online");
                String playerOffline = telegramConfig.getMessage("player-info-offline");
                String message = telegramConfig.getMessage("account-info", Map.of(
                                "%name%", protectedPlayer.getRealName(),
                                "%lowercase_name%", protectedPlayer.getLowercaseName(),
                                "%last_ip%", protectedPlayer.getLastIp(),
                                "%reg_ip%", protectedPlayer.getRegisterIp(),
                                "%is_online%", plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName()) == null ? playerOffline : playerOnline
                        )
                );

                SendMessage sendMessage = new SendMessage(chatId, message);
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

                markup.addRow(
                        new InlineKeyboardButton(telegramConfig.getMessage("account-2fa-toggle-button")).callbackData("toggle-2fa:" + id),
                        new InlineKeyboardButton(telegramConfig.getMessage("account-reset-password-button")).callbackData("reset-password:" + id)
                );
                markup.addRow(
                        new InlineKeyboardButton(telegramConfig.getMessage("account-ban-toggle-button")).callbackData("toggle-ban:" + id),
                        new InlineKeyboardButton(telegramConfig.getMessage("account-kick-button")).callbackData("kick:" + id)
                );

                if (!telegramConfig.isDisableUnlink()) {
                    markup.addRow(new InlineKeyboardButton(telegramConfig.getMessage("account-unlink-button")).callbackData("unlink:" + id));
                }
                sendMessage.replyMarkup(markup);
                bot.execute(sendMessage);
            });
        } else if (action.equals("previous") || action.equals("next")) {
            int currentPage = Integer.parseInt(id);
            int newPage = action.equals("next") ? currentPage + 1 : currentPage - 1;

            List<ProtectedPlayer> players = linkProvider.getProtectedPlayersById(chatId);
            sendPlayerButtons(plugin, bot, chatId, players, newPage);
        }
    }
}
