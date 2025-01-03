package com.beacmc.beacmcauth.api.config;

import java.util.Map;

public interface TelegramConfig {

    String getResetPasswordChars();

    Integer getCodeLength();

    Integer getMaxLink();

    public Integer getTimePerConfirm();

    String getToken();

    String getCodeChars();

    boolean isEnabled();

    String getAccountsCommand();

    boolean isDisableUnlink();

    Integer getPasswordResetLength();

    String getLinkCommand();

    String getMessage(String messagePath, Map<String, String> placeholders);

    default String getMessage(String messagePath) {
        return getMessage(messagePath, null);
    }
}
