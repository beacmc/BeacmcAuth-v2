package com.beacmc.beacmcauth.core.social.telegram.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.core.social.telegram.TelegramProvider;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AccountListener {

    public static void process(BeacmcAuth plugin, TelegramProvider telegramProvider, Update update) {
        if (update.callbackQuery() == null) return;

        String callbackData = update.callbackQuery().data();
        Long chatId = update.callbackQuery().from().id();
        String[] args = callbackData.split(":");

        if (args.length != 2) return;

        String action = args[0];
        String id = args[1];

        switch (action.toLowerCase()) {
            case "toggle-2fa": handleTwoFA(plugin, telegramProvider, chatId, id); break;
            case "kick": handleKick(plugin, telegramProvider, chatId, id); break;
            case "toggle-ban": handleBan(plugin, telegramProvider, chatId, id); break;
            case "unlink": handleUnlink(plugin, telegramProvider, chatId, id); break;
            case "reset-password": handleResetPassword(plugin, telegramProvider, chatId, id); break;
        }
    }

    private static void handleKick(BeacmcAuth plugin, TelegramProvider telegramProvider, Long chatId, String id) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final Config config = plugin.getConfig();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (protectedPlayer.getTelegram() != chatId) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (player == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("player-offline"));
                bot.execute(sendMessage);
                return;
            }

            SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-kick-success"));
            bot.execute(sendMessage);
            player.disconnect(config.getMessage("telegram-kick"));
        });
    }

    private static void handleTwoFA(BeacmcAuth plugin, TelegramProvider telegramProvider, Long chatId, String id) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final TelegramBot bot = telegramProvider.getBot();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (protectedPlayer.getTelegram() != chatId) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setTelegramTwoFaEnabled(!protectedPlayer.isTelegramTwoFaEnabled());
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (protectedPlayer.isTelegramTwoFaEnabled()) {
                    SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-2fa-enabled"));
                    bot.execute(sendMessage);
                } else {
                    SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-2fa-disabled"));
                    bot.execute(sendMessage);
                }

                return protectedPlayer;
            });
        });
    }

    private static void handleBan(BeacmcAuth plugin, TelegramProvider telegramProvider, Long chatId, String id) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final TelegramBot bot = telegramProvider.getBot();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final Config config = plugin.getConfig();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (protectedPlayer.getTelegram() != chatId) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setBanned(!protectedPlayer.isBanned());
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (protectedPlayer.isBanned()) {
                    SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-banned"));
                    bot.execute(sendMessage);
                    if (player != null) {
                        player.disconnect(config.getMessage("account-banned"));
                    }
                } else {
                    SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-unbanned"));
                    bot.execute(sendMessage);
                }

                return protectedPlayer;
            });
        });
    }

    private static void handleResetPassword(BeacmcAuth plugin, TelegramProvider telegramProvider, Long chatId, String id) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final TelegramBot bot = telegramProvider.getBot();
        final Config config = plugin.getConfig();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (protectedPlayer.getTelegram() != chatId) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                String password = CodeGenerator.generate(telegramConfig.getResetPasswordChars(), telegramConfig.getPasswordResetLength());
                try {
                    protectedPlayer.setSession(0).setPassword(BCrypt.hashpw(password, BCrypt.gensalt(config.getBCryptRounds())));
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-reset-password", Map.of("%name%", protectedPlayer.getRealName(), "%password%", password)));
                bot.execute(sendMessage);
                return protectedPlayer;
            });
        });
    }

    private static void handleUnlink(BeacmcAuth plugin, TelegramProvider telegramProvider, Long chatId, String id) {
        final TelegramConfig telegramConfig = plugin.getTelegramConfig();
        final ProtectedPlayerDao dao = plugin.getDatabase().getProtectedPlayerDao();
        final TelegramBot bot = telegramProvider.getBot();

        plugin.getAuthManager().getProtectedPlayer(id).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (protectedPlayer.getTelegram() != chatId) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-not-linked"));
                bot.execute(sendMessage);
                return;
            }

            if (telegramConfig.isDisableUnlink()) {
                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("unlink-disabled"));
                bot.execute(sendMessage);
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    protectedPlayer.setTelegram(0).setBanned(false);
                    dao.createOrUpdate(protectedPlayer);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                SendMessage sendMessage = new SendMessage(chatId, telegramConfig.getMessage("account-unlink-success"));
                bot.execute(sendMessage);
                return protectedPlayer;
            });
        });
    }
}
