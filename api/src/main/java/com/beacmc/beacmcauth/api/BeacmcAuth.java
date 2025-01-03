package com.beacmc.beacmcauth.api;

import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.DiscordConfig;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.social.SocialProvider;

import java.io.File;
import java.io.InputStream;

public interface BeacmcAuth {

    BeacmcAuth onEnable();

    BeacmcAuth onDisable();

    Config getConfig();

    Database getDatabase();

    AuthManager getAuthManager();

    void reloadAllConfigurations();

    MessageProvider getMessageProvider();

    BeacmcAuth setMessageProvider(MessageProvider messageProvider);

    Proxy getProxy();

    TelegramConfig getTelegramConfig();

    File getDataFolder();

    DiscordConfig getDiscordConfig();

    void saveResource(String file);

    SocialProvider getDiscordProvider();

    SocialProvider getTelegramProvider();

    InputStream getResource(String file);

    BeacmcAuth setDataFolder(File file);

    ServerLogger getServerLogger();

    CommandManager getCommandManager();

    BeacmcAuth setProxy(Proxy proxy);

    BeacmcAuth setLibraryProvider(LibraryProvider libraryProvider);

    BeacmcAuth setServerLogger(ServerLogger serverLogger);

    LibraryProvider getLibraryProvider();
}
