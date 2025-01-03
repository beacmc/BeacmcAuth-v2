package com.beacmc.beacmcauth.bungeecord.server;

import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.server.ProxyType;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;
import com.beacmc.beacmcauth.bungeecord.player.BungeeServerPlayer;
import com.beacmc.beacmcauth.bungeecord.scheduler.BungeeScheduler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BungeeProxy implements Proxy {

    private final ProxyServer proxyServer;

    public BungeeProxy(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public <T> void callEvent(T event) {
        proxyServer.getPluginManager().callEvent((Event) event);
    }

    @Override
    public Server getServer(String name) {
        ServerInfo server = proxyServer.getServerInfo(name);
        return server != null ? new BungeeServer(server) : null;
    }

    @Override
    public ServerPlayer getPlayer(UUID uuid) {
        ProxiedPlayer player = proxyServer.getPlayer(uuid);
        return player != null ? new BungeeServerPlayer(player) : null;
    }

    @Override
    public ServerPlayer getPlayer(String name) {
        ProxiedPlayer player = proxyServer.getPlayer(name);
        return player != null ? new BungeeServerPlayer(player) : null;
    }

    @Override
    public List<ServerPlayer> getPlayers() {
        return proxyServer.getPlayers().stream()
                .map(BungeeServerPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public ProxyType getProxyType() {
        return ProxyType.BUNGEECORD;
    }

    @Override
    public void registerListener(Object listener) {
        proxyServer.getPluginManager().registerListener(BungeeBeacmcAuth.getInstance(), (Listener) listener);
    }

    @Override
    public void shutdown() {
        proxyServer.stop();
    }

    @Override
    public TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit) {
        return new BungeeScheduler(proxyServer.getScheduler()).runTaskDelay(runnable, delay, timeUnit);
    }

    @Override
    public <T> T getOriginalProxyServer() {
        return (T) proxyServer;
    }
}
