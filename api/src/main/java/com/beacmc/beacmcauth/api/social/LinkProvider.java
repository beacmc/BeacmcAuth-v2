package com.beacmc.beacmcauth.api.social;

import com.beacmc.beacmcauth.api.ProtectedPlayer;

import java.util.List;
import java.util.Map;

public interface LinkProvider {

    List<ProtectedPlayer> getProtectedPlayersById(long id);

    ProtectedPlayer getPlayerByName(String name);

    Map<ProtectedPlayer, Long> getLinkCodes();

    default boolean checkCode(String code) {
        return getLinkCodes().keySet().stream()
                .anyMatch(protectedPlayer -> protectedPlayer.getLinkCode().equals(code));
    }
}
