package com.beacmc.beacmcauth.bungeecord.server.listener;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.server.Server;
import com.beacmc.beacmcauth.bungeecord.player.BungeeServerPlayer;
import com.beacmc.beacmcauth.bungeecord.server.BungeeServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

public class AuthListener implements Listener {

    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public AuthListener(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        authManager.onLogin(new BungeeServerPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onLogin(PlayerDisconnectEvent event) {
        authManager.onDisconnect(new BungeeServerPlayer(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        final Connection sender = event.getSender();
        final Config config = plugin.getConfig();
        final List<String> whitelistCommands = config.getWhitelistCommands();

        if (!(sender instanceof ProxiedPlayer proxiedPlayer)) {
            return;
        }

        ServerPlayer player = new BungeeServerPlayer(proxiedPlayer);
        if (!authManager.isAuthenticating(player)) {
            String cmd = event.getMessage().split(" ")[0];
            if(event.isCommand() && whitelistCommands.contains(cmd.toLowerCase())) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnect(ServerConnectEvent event) {
        final Server server = new BungeeServer(event.getTarget());
        final ServerPlayer player = new BungeeServerPlayer(event.getPlayer());
        final Config config = plugin.getConfig();

        if (!authManager.isAuthenticating(player))
            return;

        if (config.getDisabledServers().stream().anyMatch(execute -> server.getName().equals(execute))) {
            player.sendMessage(config.getMessage("blocked-server"));
            event.setCancelled(true);
        }
    }
}
