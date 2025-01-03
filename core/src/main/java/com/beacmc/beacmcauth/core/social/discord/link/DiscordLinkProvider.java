package com.beacmc.beacmcauth.core.social.discord.link;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.database.dao.ProtectedPlayerDao;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.core.social.discord.DiscordProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordLinkProvider implements LinkProvider {

    private final DiscordProvider discordProvider;
    private final Map<ProtectedPlayer, Long> linkCodes;
    private final ProtectedPlayerDao dao;

    public DiscordLinkProvider(BeacmcAuth plugin, DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        linkCodes = new HashMap<>();
        dao = plugin.getDatabase().getProtectedPlayerDao();
    }

    public List<ProtectedPlayer> getProtectedPlayersById(long discord) {
        try {
            return dao.queryForEq("discord", discord);
        } catch (SQLException ignored) {
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
