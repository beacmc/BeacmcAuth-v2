package com.beacmc.beacmcauth.bungeecord.server;

import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.bungeecord.player.BungeeServerPlayer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;
import java.util.stream.Collectors;

public class BungeeServer implements Server {

    private final ServerInfo server;

    public BungeeServer(ServerInfo server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return server.getName();
    }

    @Override
    public int getOnlinePlayersSize() {
        return server.getPlayers().size();
    }

    @Override
    public Collection<ServerPlayer> getOnlinePlayers() {
        return server.getPlayers().stream()
                .map(BungeeServerPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public <T> T getOriginalServer() {
        return (T) server;
    }
}
