package com.beacmc.beacmcauth.bungeecord;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.bungeecord.library.BungeeLibraryProvider;
import com.beacmc.beacmcauth.bungeecord.logger.BungeeServerLogger;
import com.beacmc.beacmcauth.bungeecord.message.BungeeMessageProvider;
import com.beacmc.beacmcauth.bungeecord.server.BungeeProxy;
import com.beacmc.beacmcauth.bungeecord.server.command.*;
import com.beacmc.beacmcauth.bungeecord.server.listener.AuthListener;
import com.beacmc.beacmcauth.core.BaseBeacmcAuth;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeBeacmcAuth extends Plugin {
    
    private static BungeeBeacmcAuth instance;
    private BeacmcAuth beacmcAuth;
    
    @Override
    public void onEnable() {
        instance = this;
        beacmcAuth = new BaseBeacmcAuth();
        beacmcAuth.setDataFolder(getDataFolder())
                .setProxy(new BungeeProxy(getProxy()))
                .setLibraryProvider(new BungeeLibraryProvider())
                .setMessageProvider(new BungeeMessageProvider())
                .setServerLogger(new BungeeServerLogger(beacmcAuth))
                .onEnable();

        this.getProxy().getPluginManager().registerListener(this, new AuthListener(beacmcAuth));
        initCommands();
    }

    private void initCommands() {
        this.getProxy().getPluginManager().registerCommand(this, new LoginCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new RegisterCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new AuthCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new ChangepasswordCommand(beacmcAuth));
        this.getProxy().getPluginManager().registerCommand(this, new LinkCommand(beacmcAuth));
    }

    @Override
    public void onDisable() {
        if (beacmcAuth != null) beacmcAuth.onDisable();
        instance = null;
    }

    public static BungeeBeacmcAuth getInstance() {
        return instance;
    }

    public BeacmcAuth getBeacmcAuth() {
        return beacmcAuth;
    }
}
