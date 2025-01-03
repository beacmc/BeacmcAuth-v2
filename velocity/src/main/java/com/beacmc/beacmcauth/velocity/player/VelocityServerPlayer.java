package com.beacmc.beacmcauth.velocity.player;

import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.velocity.VelocityBeacmcAuth;
import com.beacmc.beacmcauth.velocity.server.VelocityServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.UUID;

public class VelocityServerPlayer implements ServerPlayer {

    private final Player player;
    private final MessageProvider messageProvider;
    private final ServerLogger logger;

    public VelocityServerPlayer(Player player) {
        this.player = player;
        this.messageProvider = VelocityBeacmcAuth.getInstance().getBeacmcAuth().getMessageProvider();
        this.logger = VelocityBeacmcAuth.getInstance().getBeacmcAuth().getServerLogger();
    }

    @Override
    public String getName() {
        logger.debug("Get player username: " + player.getUsername());
        return player.getUsername();
    }

    @Override
    public UUID getUUID() {
        logger.debug("Get player(" + player.getUsername() + ") UUID: " + player.getUniqueId());
        return player.getUniqueId();
    }

    @Override
    public void sendMessage(String message) {
        logger.debug("Send message to player(" + player.getUsername() + "): " + message);
        player.sendMessage(messageProvider.createMessage(message).toComponent());
    }

    @Override
    public boolean isConnected() {
        boolean result = player.isActive();
        logger.debug("player(" + player.getUsername() + ") connect check: " + result);
        return result;
    }


    @Override
    public void connect(Server server) {
        logger.debug("Create connection request. Player(" + player.getUsername() + "), Server(" + server.getName() + ")");
        player.createConnectionRequest(server.getOriginalServer());
    }

    @Override
    public void disconnect(String message) {
        logger.debug("Player( "+ player.getUsername() + ") disconnect for message: " + message);
        player.disconnect(messageProvider.createMessage(message).toComponent());
    }

    @Override
    public void sendTitle(String title, String subtitle, long in, long stay, long out) {
        logger.debug("send title to player(" + player.getUsername() + "); title: " + title + "; subtitle: " + subtitle + "; fadeIn:" + in + "; stay:" + stay + "; fadeOut:" + out);
        player.showTitle(Title.title(
                messageProvider.createMessage(title).toComponent(),
                messageProvider.createMessage(subtitle).toComponent(),
                Title.Times.times(
                        Duration.ofSeconds(in),
                        Duration.ofSeconds(stay),
                        Duration.ofSeconds(out)
                )
        ));
    }

    @Override
    public String getIpAddress() {
        String result = player.getRemoteAddress().getHostName();
        logger.debug("Get player(" + player.getUsername() + ") ip address: " + result);
        return result;
    }

    @Override
    public <T> T getOriginalPlayer() {
        return (T) player;
    }

    @Override
    public boolean hasPermission(String perm) {
        boolean result = player.hasPermission(perm);
        logger.debug("Check permission(" + perm + ") for player(" + player.getUsername() + "). Result: " + result);
        return result;
    }

    @Override
    public Server getCurrentServer() {
        ServerConnection connection = player.getCurrentServer().orElse(null);
        return connection != null ? new VelocityServer(connection.getServer()) : null;
    }

    @Override
    public String toString() {
        return player.getUsername();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals(getName());
        }
        return super.equals(obj);
    }
}
