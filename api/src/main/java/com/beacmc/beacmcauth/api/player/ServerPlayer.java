package com.beacmc.beacmcauth.api.player;

import com.beacmc.beacmcauth.api.command.CommandSender;
import com.beacmc.beacmcauth.api.server.Server;

import java.util.UUID;

public interface ServerPlayer extends CommandSender {

    String getName();

    default String getLowercaseName() {
        return getName().toLowerCase();
    }

    UUID getUUID();

    void connect(Server server);

    void disconnect(String message);

    void sendTitle(String title, String subtitle, long in, long stay, long out);

    String getIpAddress();

    boolean isConnected();

    Server getCurrentServer();

    <T> T getOriginalPlayer();
}
