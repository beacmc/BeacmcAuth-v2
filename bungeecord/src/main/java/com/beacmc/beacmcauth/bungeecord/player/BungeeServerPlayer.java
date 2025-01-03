package com.beacmc.beacmcauth.bungeecord.player;

import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.bungeecord.BungeeBeacmcAuth;
import com.beacmc.beacmcauth.bungeecord.server.BungeeServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeServerPlayer implements ServerPlayer {

    private final ProxiedPlayer player;
    private final ServerLogger logger;
    private final MessageProvider messageProvider;

    public BungeeServerPlayer(ProxiedPlayer player) {
        this.player = player;
        this.logger = BungeeBeacmcAuth.getInstance().getBeacmcAuth().getServerLogger();
        this.messageProvider = BungeeBeacmcAuth.getInstance().getBeacmcAuth().getMessageProvider();
    }

    @Override
    public String getName() {
        String result = player.getName();
        logger.debug("Get player username: " + result);
        return result;
    }

    @Override
    public UUID getUUID() {
        logger.debug("Get player(" + player.getDisplayName() + ") UUID: " + player.getUniqueId());
        return player.getUniqueId();
    }

    @Override
    public void sendMessage(String message) {
        logger.debug("Send message to player(" + player.getDisplayName() + "): " + message);
        player.sendMessage(messageProvider.createMessage(message).toBaseComponent());
    }

    @Override
    public boolean isConnected() {
        boolean result = player.isConnected();
        logger.debug("player(" + player.getDisplayName() + ") connect check: " + result);
        return result;
    }


    @Override
    public void connect(Server server) {
        logger.debug("Create connection request. Player(" + player.getName() + "), Server(" + server.getName() + ")");
        player.connect((ServerInfo) server.getOriginalServer());
    }

    @Override
    public void disconnect(String message) {
        logger.debug("Player( "+ player.getName() + ") disconnect for message: " + message);
        player.disconnect(messageProvider.createMessage(message).toBaseComponent());
    }

    @Override
    public void sendTitle(String title, String subtitle, long in, long stay, long out) {
        logger.debug("send title to player(" + player.getName() + "); title: " + title + "; subtitle: " + subtitle + "; fadeIn:" + in + "; stay:" + stay + "; fadeOut:" + out);
        Title execute = BungeeBeacmcAuth.getInstance().getProxy().createTitle()
                .title(messageProvider.createMessage(title).toBaseComponent())
                .subTitle(messageProvider.createMessage(subtitle).toBaseComponent())
                .fadeIn((int) in)
                .stay((int) stay)
                .fadeOut((int) out);
        player.sendTitle(execute);
    }

    @Override
    public String getIpAddress() {
        String result = player.getAddress().getHostName();
        logger.debug("Get player(" + player.getName() + ") ip address: " + result);
        return result;
    }

    @Override
    public <T> T getOriginalPlayer() {
        return (T) player;
    }

    @Override
    public boolean hasPermission(String perm) {
        boolean result = player.hasPermission(perm);
        logger.debug("Check permission(" + perm + ") for player(" + player.getName() + "). Result: " + result);
        return result;
    }

    @Override
    public Server getCurrentServer() {
        net.md_5.bungee.api.connection.Server connection = player.getServer();
        return connection != null ? new BungeeServer(connection.getInfo()) : null;
    }

    @Override
    public String toString() {
        return player.getDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals(getName());
        }
        return super.equals(obj);
    }
}
