package com.beacmc.beacmcauth.core;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.command.CommandManager;
import com.beacmc.beacmcauth.api.config.Config;
import com.beacmc.beacmcauth.api.config.DiscordConfig;
import com.beacmc.beacmcauth.api.config.TelegramConfig;
import com.beacmc.beacmcauth.api.config.loader.ConfigLoader;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.message.MessageProvider;
import com.beacmc.beacmcauth.api.server.Proxy;
import com.beacmc.beacmcauth.api.social.SocialProvider;
import com.beacmc.beacmcauth.core.auth.BaseAuthManager;
import com.beacmc.beacmcauth.core.command.BaseCommandManager;
import com.beacmc.beacmcauth.core.command.executor.*;
import com.beacmc.beacmcauth.core.config.BaseConfig;
import com.beacmc.beacmcauth.core.config.BaseDiscordConfig;
import com.beacmc.beacmcauth.core.config.BaseTelegramConfig;
import com.beacmc.beacmcauth.core.config.loader.BaseConfigLoader;
import com.beacmc.beacmcauth.core.database.BaseDatabase;
import com.beacmc.beacmcauth.core.library.Libraries;
import com.beacmc.beacmcauth.core.social.discord.DiscordProvider;
import com.beacmc.beacmcauth.core.social.telegram.TelegramProvider;

import java.io.*;

public class BaseBeacmcAuth implements BeacmcAuth {

    private Proxy proxy;
    private ServerLogger serverLogger;
    private CommandManager commandManager;
    private File dataFolder;
    private SocialProvider discordProvider;
    private SocialProvider telegramProvider;
    private Database database;
    private ConfigLoader configLoader;
    private MessageProvider messageProvider;
    private LibraryProvider libraryProvider;
    private AuthManager authManager;
    private TelegramConfig telegramConfig;
    private Config config;
    private DiscordConfig discordConfig;

    @Override
    public BeacmcAuth onEnable() {
        configLoader = new BaseConfigLoader();
        config = new BaseConfig(this, configLoader);
        telegramConfig = new BaseTelegramConfig(this, configLoader);
        discordConfig = new BaseDiscordConfig(this, configLoader);
        database = new BaseDatabase(this);
        database.init();
        authManager = new BaseAuthManager(this);
        if (telegramConfig.isEnabled()) {
            getLibraryProvider().loadLibrary(Libraries.TELEGRAM);
            telegramProvider = new TelegramProvider(this);
        }
        if (discordConfig.isEnabled()) {
            getLibraryProvider().loadLibrary(Libraries.JDA);
            discordProvider = new DiscordProvider(this);
        }
        commandManager = new BaseCommandManager();
        commandManager.register("register", new RegisterCommandExecutor(this));
        commandManager.register("login", new LoginCommandExecutor(this));
        commandManager.register("link", new LinkCommandExecutor(this));
        commandManager.register("changepassword", new ChangepasswordCommandExecutor(this));
        commandManager.register("auth", new AuthCommandExecutor(this));

        return this;
    }

    @Override
    public BeacmcAuth onDisable() {
        commandManager = null;
        serverLogger = null;
        configLoader = null;
        return this;
    }

    @Override
    public void reloadAllConfigurations() {
        config = new BaseConfig(this, configLoader);
        telegramConfig = new BaseTelegramConfig(this, configLoader);
        discordConfig = new BaseDiscordConfig(this, configLoader);
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public SocialProvider getDiscordProvider() {
        return discordProvider;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public DiscordConfig getDiscordConfig() {
        return discordConfig;
    }

    @Override
    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public BeacmcAuth setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    @Override
    public BeacmcAuth setLibraryProvider(LibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
        return this;
    }

    @Override
    public LibraryProvider getLibraryProvider() {
        return libraryProvider;
    }

    @Override
    public TelegramConfig getTelegramConfig() {
        return telegramConfig;
    }

    @Override
    public SocialProvider getTelegramProvider() {
        return telegramProvider;
    }

    @Override
    public BeacmcAuth setMessageProvider(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
        return this;
    }

    @Override
    public BeacmcAuth setServerLogger(ServerLogger serverLogger) {
        this.serverLogger = serverLogger;
        return this;
    }

    @Override
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Override
    public ServerLogger getServerLogger() {
        return serverLogger;
    }

    @Override
    public AuthManager getAuthManager() {
        return authManager;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public BeacmcAuth setDataFolder(File dataFolder) {
        this.dataFolder = dataFolder;
        return this;
    }

    @Override
    public void saveResource(String resourcePath) {
        if (resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            System.out.println("Could not save " + outFile.getName() + " to " + outFile);
        }
    }

    @Override
    public InputStream getResource(String file) {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
}
