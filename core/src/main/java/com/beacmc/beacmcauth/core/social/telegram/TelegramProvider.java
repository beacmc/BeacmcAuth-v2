package com.beacmc.beacmcauth.core.social.telegram;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.api.social.SocialProvider;
import com.beacmc.beacmcauth.core.social.telegram.command.AccountsCommand;
import com.beacmc.beacmcauth.core.social.telegram.command.LinkCommand;
import com.beacmc.beacmcauth.core.social.telegram.link.TelegramLinkProvider;
import com.beacmc.beacmcauth.core.social.telegram.listener.AccountListener;
import com.beacmc.beacmcauth.core.social.telegram.listener.ConfirmationListener;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramProvider implements SocialProvider {

    private TelegramBot bot;
    private final ServerLogger logger;
    private final BeacmcAuth plugin;
    private final LinkProvider linkProvider;
    private final Map<String, ProtectedPlayer> confirmationUsers;

    public TelegramProvider(BeacmcAuth plugin) {
        this.plugin = plugin;
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();

        confirmationUsers = new HashMap<>();
        logger = plugin.getServerLogger();
        linkProvider = new TelegramLinkProvider(plugin, this);
        if (telegramConfig.isEnabled()) init();
    }

    public void init() {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();

        logger.log("[Telegram] The bot turning on...");
        bot = new TelegramBot(telegramConfig.getToken());

        bot.setUpdatesListener(new TelegramUpdatesListener(plugin, this));
        logger.log("[Telegram] The bot has been successfully switched on");
    }

    public void sendConfirmationMessage(ProtectedPlayer protectedPlayer) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());
        final long telegram = protectedPlayer.getTelegram();

        if (telegram == 0 || player == null)
            return;

        String ip = player.getIpAddress();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                new InlineKeyboardButton(telegramConfig.getMessage("confirmation-button-accept-text")).callbackData("confirm-accept-" + protectedPlayer.getLowercaseName()),
                new InlineKeyboardButton(telegramConfig.getMessage("confirmation-button-decline-text")).callbackData("confirm-decline-" + protectedPlayer.getLowercaseName())
        );
        SendMessage sendMessage = new SendMessage(telegram, telegramConfig.getMessage("confirmation-message", Map.of("%name%", player.getName(), "%ip%", ip)));
        sendMessage.replyMarkup(markup);

        SendResponse response = bot.execute(sendMessage);
        if (!response.isOk()) {
            logger.error("[Telegram] Error on send message: " + response.errorCode() + " - " + response.description());
        }
    }

    public Map<String, ProtectedPlayer> getConfirmationUsers() {
        return confirmationUsers;
    }

    public boolean isEnabled() {
        return bot != null;
    }

    public TelegramBot getBot() {
        return bot;
    }

    public LinkProvider getLinkProvider() {
        return linkProvider;
    }

    protected final class TelegramUpdatesListener implements UpdatesListener {

        protected final TelegramProvider telegramProvider;
        protected final BeacmcAuth plugin;

        protected TelegramUpdatesListener(BeacmcAuth plugin, TelegramProvider telegramProvider) {
            this.telegramProvider = telegramProvider;
            this.plugin = plugin;
        }

        @Override
        public int process(List<Update> list) {
            for (Update update : list) {
                ConfirmationListener.process(plugin, telegramProvider, update);
                LinkCommand.process(plugin, telegramProvider, update);
                AccountsCommand.process(plugin, telegramProvider, update);
                AccountsCommand.processButtons(plugin, telegramProvider, update);
                AccountListener.process(plugin, telegramProvider, update);
            }
            return CONFIRMED_UPDATES_ALL;
        }
    }
}
