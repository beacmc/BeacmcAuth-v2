package com.beacmc.beacmcauth.api.config;

import java.util.Map;

public interface DiscordConfig {

    public String getResetPasswordChars();

    public Integer getCodeLength();

    public Integer getMaxLink();

    public Integer getTimePerConfirm();

    public Long getGuildID();
    public String getToken();

    public String getCodeChars();

    public boolean isEnabled();

    public String getActivityType();

    public boolean isActivityEnabled();

    public String getActivityText();

    public String getActivityUrl();

    public String getAccountsCommand();

    public boolean isDisableUnlink();

    public Integer getPasswordResetLength();

    public String getLinkCommand();

    String getMessage(String messagePath, Map<String, String> placeholders);

    default String getMessage(String messagePath) {
        return getMessage(messagePath, null);
    }
}
