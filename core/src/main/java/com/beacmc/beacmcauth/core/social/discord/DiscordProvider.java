package com.beacmc.beacmcauth.core.social.discord;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.ProtectedPlayer;
import com.beacmc.beacmcauth.api.config.DiscordConfig;
import com.beacmc.beacmcauth.api.logger.ServerLogger;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.api.social.SocialProvider;
import com.beacmc.beacmcauth.core.social.discord.command.AccountsCommand;
import com.beacmc.beacmcauth.core.social.discord.command.LinkCommand;
import com.beacmc.beacmcauth.core.social.discord.link.DiscordLinkProvider;
import com.beacmc.beacmcauth.core.social.discord.listener.AccountListener;
import com.beacmc.beacmcauth.core.social.discord.listener.ConfirmationListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DiscordProvider implements SocialProvider {

    private JDA jda;
    private Guild guild;
    private final ServerLogger logger;
    private final BeacmcAuth plugin;
    private final DiscordLinkProvider linkProvider;
    private final Map<String, ProtectedPlayer> confirmationUsers;

    public DiscordProvider(BeacmcAuth plugin) {
        this.plugin = plugin;
        confirmationUsers = new HashMap<>();

        final DiscordConfig discordConfig = plugin.getDiscordConfig();

        logger = plugin.getServerLogger();
        linkProvider = new DiscordLinkProvider(plugin, this);
        if (discordConfig.isEnabled()) init();
    }

    public void init() {
        final DiscordConfig discordConfig = plugin.getDiscordConfig();

        logger.log("[Discord] The bot turning on...");

        jda = JDABuilder.createDefault(discordConfig.getToken(), Arrays.asList(GatewayIntent.values())).build();
        if (!jda.getStatus().isInit()) {
            logger.error("[Discord] Bot is not initializing.");
            return;
        }

        guild = jda.getGuildById(discordConfig.getGuildID());
        if (guild == null) {
            logger.warn("[Discord] Guild not found.");
        }

        jda.addEventListener(new ConfirmationListener(plugin, this), new AccountListener(plugin));
        jda.addEventListener(new LinkCommand(plugin, this), new AccountsCommand(plugin, this));

        if (discordConfig.isActivityEnabled()) {
            jda.getPresence().setActivity(Activity.of(getActivityType(discordConfig.getActivityType()), discordConfig.getActivityText(), discordConfig.getActivityUrl()));
        }

        logger.log("[Discord] The bot has been successfully switched on");
    }

    public void sendConfirmationMessage(ProtectedPlayer protectedPlayer) {
        final DiscordConfig discordConfig = plugin.getDiscordConfig();
        final ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getRealName());
        final long discord = protectedPlayer.getDiscord();

        if (discord == 0 || player == null)
            return;

        User user = jda.getUserById(discord);
        if (user == null) {
            logger.error("User " + discord + " not found!");
            return;
        }

        user.openPrivateChannel().flatMap(channel -> {
            String ip = player.getIpAddress();
            return channel.sendMessage(discordConfig.getMessage("confirmation-message", Map.of("%name%", player.getName(), "%ip%", ip)))
                    .setActionRow(
                            Button.success("confirm-accept-" + protectedPlayer.getLowercaseName(), discordConfig.getMessage("confirmation-button-accept-text")),
                            Button.danger("confirm-decline-" + protectedPlayer.getLowercaseName(), discordConfig.getMessage("confirmation-button-decline-text"))
                    );
        }).queue();
    }

    public Map<String, ProtectedPlayer> getConfirmationUsers() {
        return confirmationUsers;
    }

    public boolean isEnabled() {
        if (jda == null)
            return false;
        return jda.getStatus().isInit();
    }

    private Activity.ActivityType getActivityType(String activityType) {
        try {
            return Activity.ActivityType.valueOf(activityType.toUpperCase());
        } catch (IllegalArgumentException e) {
        }
        return Activity.ActivityType.PLAYING;
    }

    public LinkProvider getLinkProvider() {
        return linkProvider;
    }
}
