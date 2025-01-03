package com.beacmc.beacmcauth.core.database;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.DatabaseSettings;
import com.beacmc.beacmcauth.api.database.Database;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.library.LibraryProvider;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.core.database.dao.BaseProtectPlayerDao;
import com.beacmc.beacmcauth.core.library.Libraries;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class BaseDatabase implements Database {

    private final ServerLogger logger;
    private ConnectionSource connectionSource;
    private ProtectedPlayerDao protectedPlayerDao;
    private BeacmcAuth plugin;

    public BaseDatabase(BeacmcAuth plugin) {
        this.plugin = plugin;
        this.logger = plugin.getServerLogger();
    }

    @Override
    public void init() {
        final DatabaseSettings databaseSettings = plugin.getConfig().getDatabaseSettings();

        try {
            loadDatabaseLibrary(databaseSettings.getType().name().toLowerCase());
            connectionSource = new JdbcConnectionSource(databaseSettings.getUrl(), databaseSettings.getUsername(), databaseSettings.getPassword());
            protectedPlayerDao = new BaseProtectPlayerDao(connectionSource);
            TableUtils.createTableIfNotExists(connectionSource, ProtectedPlayer.class);
        } catch (Throwable e) {
            if (databaseSettings.isStopServerOnFailedConnection()) {
                logger.error("Database is not connected. Server stopping...");
                logger.error("error message: " + e.getMessage());
                plugin.getProxy().shutdown();
            }
        }

        if (connectionSource == null && databaseSettings.isStopServerOnFailedConnection()) {
            logger.error("Database is not connected. Server stopping...");
            plugin.getProxy().shutdown();
        }
    }

    private void loadDatabaseLibrary(String databaseType) {
        final LibraryProvider libraryLoader = plugin.getLibraryProvider();

        switch (databaseType) {
            case "sqlite": libraryLoader.loadLibrary(Libraries.SQLITE); break;
            case "mariadb": libraryLoader.loadLibrary(Libraries.MARIADB); break;
            case "postgresql": libraryLoader.loadLibrary(Libraries.POSTGRESQL); break;
        }
    }

    @Override
    public ProtectedPlayerDao getProtectedPlayerDao() {
        return protectedPlayerDao;
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }
}
