package com.beacmc.beacmcauth.core.auth;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.api.social.SocialProvider;
import com.beacmc.beacmcauth.core.util.runnable.LoginRunnable;
import com.beacmc.beacmcauth.core.util.runnable.RegisterRunnable;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BaseAuthManager implements AuthManager {

    private final BeacmcAuth plugin;
    private final Map<String, Integer> authorizationPlayers;
    private final ProtectedPlayerDao dao;
    private final ServerLogger logger;

    public BaseAuthManager(BeacmcAuth plugin) {
        this.plugin = plugin;
        authorizationPlayers = new HashMap<>();
        this.logger = plugin.getServerLogger();
        dao = plugin.getDatabase().getProtectedPlayerDao();
    }

    @Override
    public void onLogin(@Nullable ServerPlayer player) {
        if (player == null ) return;

        final Config config = plugin.getConfig();
        final String ip = player.getIpAddress();

        logger.debug("Check player(" + player.getName() + ") nickname matcher(" + config.getNicknameRegex() + "). Success: " + config.getNicknameRegex().matcher(player.getName()).matches());

        if (!config.getNicknameRegex().matcher(player.getName()).matches()) {
            player.disconnect(config.getMessage("invalid-character-in-name", Map.of()));
            return;
        }

        getProtectedPlayer(player.getLowercaseName()).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                try {
                    protectedPlayer = createProtectedPlayer(player.getLowercaseName(), player.getName(), null, 0, System.currentTimeMillis(), false, true, true, player.getIpAddress(), player.getIpAddress(), player.getUUID()).get();
                } catch (InterruptedException | ExecutionException ignored) {
                }

                if (protectedPlayer == null) {
                    player.disconnect(config.getMessage("internal-error"));
                    return;
                }
            }

            if (protectedPlayer.isBanned()) {
                player.disconnect(config.getMessage("account-banned"));
                return;
            }

            if (config.isNameCaseControl() && !protectedPlayer.getRealName().equals(player.getName())) {
                player.disconnect(config.getMessage("name-case-failed", Map.of("%current_name%", player.getName(), "%need_name%", protectedPlayer.getRealName())));
                return;
            }

            if (!protectedPlayer.isRegister()) {
                logger.debug("The player(" + player.getName() + ") has started registration");

                authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
                new RegisterRunnable(plugin, player);
                this.connectPlayer(player, config.findServer(config.getAuthServers()));
                return;
            }

            logger.debug("Checking the player(" + player.getName() + ") for an active session(" + config.getSessionTime() + ") and a match in the IP-address(" + ip + ")");

            if (protectedPlayer.isSessionActive(config.getSessionTime()) && protectedPlayer.isValidIp(ip)) {
                logger.debug("the player(" + player.getName() + ") has an active session and a valid IP-address(" + ip + ")");
                this.connectPlayer(player, config.findServer(config.getGameServers()));
                player.sendMessage(config.getMessage("session-active"));
                return;
            }

            logger.debug("The player(" + player.getName() + ") has started authorization");

            authorizationPlayers.put(player.getLowercaseName(), config.getPasswordAttempts());
            new LoginRunnable(plugin, player);
            this.connectPlayer(player, config.findServer(config.getAuthServers()));
        });
    }

    @Override
    public void onDisconnect(ServerPlayer player) {
        getAuthPlayers().remove(player.getLowercaseName());
    }

    @Override
    public void connectPlayer(ServerPlayer player, Server server) {
        if (server == null || player == null) return;
        logger.debug("Try connect player(" + player + ") to server(" + server.getName() + ")");
        if (server.equals(player.getCurrentServer())) return;

        player.connect(server);
    }

    @Override
    public boolean isAuthenticating(ServerPlayer player) {
        final SocialProvider telegram = plugin.getTelegramProvider();
        final SocialProvider discord = plugin.getDiscordProvider();
        return !(getAuthPlayers().containsKey(player.getLowercaseName())
                || (discord != null && discord.getConfirmationUsers().containsKey(player.getLowercaseName()))
                || (telegram != null && telegram.getConfirmationUsers().containsKey(player.getLowercaseName())));
    }

    @Override
    public CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, boolean banned, boolean discordTwoFaEnabled, boolean telegramTwoFaEnabled, String registerIp, String lastIp, UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProtectedPlayer execute = new ProtectedPlayer(lowercaseName, realName, password, session, lastJoin, banned, discordTwoFaEnabled, telegramTwoFaEnabled, registerIp, lastIp, uuid);
                dao.create(execute);
                return execute;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<ProtectedPlayer> getProtectedPlayer(String lowercaseName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return dao.queryForId(lowercaseName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<ProtectedPlayer> performLogin(ProtectedPlayer protectedPlayer) {
        return CompletableFuture.supplyAsync(() -> {
            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (player == null) {
                return null;
            }

            try {
                getAuthPlayers().remove(protectedPlayer.getLowercaseName());
                String ip = player.getIpAddress();
                long currentTime = System.currentTimeMillis();
                dao.createOrUpdate(protectedPlayer.setLastIp(ip).setSession(currentTime).setLastJoin(currentTime));
            } catch (SQLException ignored) {
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<ProtectedPlayer> register(ProtectedPlayer protectedPlayer, String password) {
        final Config config = plugin.getConfig();

        return CompletableFuture.supplyAsync(() -> {
            try {
                dao.createOrUpdate(protectedPlayer.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(config.getBCryptRounds()))));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public Map<String, Integer> getAuthPlayers() {
        return authorizationPlayers;
    }
}
