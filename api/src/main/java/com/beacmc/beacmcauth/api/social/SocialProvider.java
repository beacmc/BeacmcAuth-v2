package com.beacmc.beacmcauth.api.social;

import com.beacmc.beacmcauth.api.ProtectedPlayer;

import java.util.Map;

public interface SocialProvider {

    void init();

    void sendConfirmationMessage(ProtectedPlayer protectedPlayer);

    Map<String, ProtectedPlayer> getConfirmationUsers();

    boolean isEnabled();

    LinkProvider getLinkProvider();
}
