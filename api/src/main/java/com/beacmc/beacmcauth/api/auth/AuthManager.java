package com.beacmc.beacmcauth.api.auth;

import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AuthManager {

    void onLogin(ServerPlayer player);

    void onDisconnect(ServerPlayer player);

    void connectPlayer(ServerPlayer player, Server server);

    boolean isAuthenticating(ServerPlayer player);

    CompletableFuture<ProtectedPlayer> createProtectedPlayer(String lowercaseName, String realName, String password, long session, long lastJoin, boolean banned, boolean discordTwoFaEnabled, boolean telegramTwoFaEnabled, String registerIp, String lastIp, UUID uuid);

    CompletableFuture<ProtectedPlayer> getProtectedPlayer(String lowercaseName);

    CompletableFuture<ProtectedPlayer> performLogin(ProtectedPlayer protectedPlayer);

    CompletableFuture<ProtectedPlayer> register(ProtectedPlayer protectedPlayer, String password);

    Map<String, Integer> getAuthPlayers();
}
