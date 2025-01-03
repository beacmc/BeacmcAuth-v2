package com.beacmc.beacmcauth.core.command.executor;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.command.executor.CommandExecutor;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class ChangepasswordCommandExecutor implements CommandExecutor {

    private final ProtectedPlayerDao dao;
    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public ChangepasswordCommandExecutor(BeacmcAuth plugin) {
        dao = plugin.getDatabase().getProtectedPlayerDao();
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ServerPlayer player)) {
            sender.sendMessage("Only player");
            return;
        }

        final Config config = plugin.getConfig();
        final Integer minLength = config.getPasswordMinLength();
        final Integer maxLength = config.getPasswordMaxLength();

        if (args.length < 2) {
            player.sendMessage(config.getMessage("change-password-command-usage"));
            return;
        }

        CompletableFuture<ProtectedPlayer> future = authManager.getProtectedPlayer(player.getLowercaseName());

        future.thenAccept(protectedPlayer -> {
            if (!protectedPlayer.checkPassword(args[0])) {
                player.sendMessage(config.getMessage("old-password-wrong"));
                return;
            }

            if (args[1].length() < minLength) {
                player.sendMessage(config.getMessage("low-character-password"));
                return;
            }

            if (args[1].length() > maxLength) {
                player.sendMessage(config.getMessage("high-character-password"));
                return;
            }

            if (args[1].equals(args[0])) {
                player.sendMessage(config.getMessage("passwords-match"));
                return;
            }

            CompletableFuture.supplyAsync(() -> {
                try {
                    dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(args[1], BCrypt.gensalt(config.getBCryptRounds()))));
                    player.sendMessage(config.getMessage("change-password-success"));
                } catch (SQLException e) {
                }
                return null;
            });
        });
    }
}
