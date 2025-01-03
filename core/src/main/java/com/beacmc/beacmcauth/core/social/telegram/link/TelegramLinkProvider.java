package com.beacmc.beacmcauth.core.social.telegram.link;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.core.social.telegram.TelegramProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramLinkProvider implements LinkProvider {

    private final TelegramProvider telegramProvider;
    private final Map<ProtectedPlayer, Long> linkCodes;
    private final ProtectedPlayerDao dao;

    public TelegramLinkProvider(BeacmcAuth plugin, TelegramProvider telegramProvider) {
        this.telegramProvider = telegramProvider;
        linkCodes = new HashMap<>();
        dao = plugin.getDatabase().getProtectedPlayerDao();
    }

    public List<ProtectedPlayer> getProtectedPlayersById(long telegram) {
        try {
            return dao.queryForEq("telegram", telegram);
        } catch (SQLException e) {
        }
        return Collections.emptyList();
    }

    public ProtectedPlayer getPlayerByName(String name) {
        return linkCodes.keySet().stream()
                .filter(p -> p.getLowercaseName().equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public Map<ProtectedPlayer, Long> getLinkCodes() {
        return linkCodes;
    }
}
