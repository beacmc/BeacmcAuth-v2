package com.beacmc.beacmcauth.api.server;

import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.scheduler.TaskScheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface Proxy {

    <T> void callEvent(T event);

    Server getServer(String name);

    ServerPlayer getPlayer(UUID uuid);

    ServerPlayer getPlayer(String name);

    List<ServerPlayer> getPlayers();

    ProxyType getProxyType();

    void registerListener(Object listener);

    void shutdown();

    TaskScheduler runTaskDelay(Runnable runnable, long delay, TimeUnit timeUnit);

    <T> T getOriginalProxyServer();
}
