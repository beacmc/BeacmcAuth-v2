package com.beacmc.beacmcauth.velocity.server;

import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.server.ProxyType;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.beacmc.beacmcauth.velocity.player.VelocityServerPlayer;
import com.beacmc.beacmcauth.velocity.scheduler.VelocityScheduler;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VelocityProxy implements Proxy {

    private final ProxyServer proxyServer;

    public VelocityProxy(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public <T> void callEvent(T event) {
        proxyServer.getEventManager().fire(event);
    }

    @Override
    public Server getServer(String name) {
        RegisteredServer server = proxyServer.getServer(name).orElse(null);
        return server != null ? new VelocityServer(server) : null;
    }

    @Override
    public TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit) {
        return new VelocityScheduler(VelocityBeacmcAuth.getInstance().getBeacmcAuth()).runTaskDelay(
                runnable, delay, timeUnit);
    }

    @Override
    public ServerPlayer getPlayer(UUID uuid) {
        Player player = proxyServer.getPlayer(uuid).orElse(null);
        return player != null ? new VelocityServerPlayer(player) : null;
    }

    @Override
    public ServerPlayer getPlayer(String name) {
        Player player = proxyServer.getPlayer(name).orElse(null);
        return player != null ? new VelocityServerPlayer(player) : null;
    }

    @Override
    public List<ServerPlayer> getPlayers() {
        return proxyServer.getAllPlayers().stream()
                .map(VelocityServerPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public ProxyType getProxyType() {
        return ProxyType.VELOCITY;
    }

    @Override
    public void registerListener(Object listener) {
        proxyServer.getEventManager().register(VelocityBeacmcAuth.getInstance(), listener);
    }

    @Override
    public void shutdown() {
        proxyServer.shutdown();
    }

    @Override
    public <T> T getOriginalProxyServer() {
        return (T) proxyServer;
    }
}
