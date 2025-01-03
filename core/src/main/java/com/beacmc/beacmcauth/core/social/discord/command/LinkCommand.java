package com.beacmc.beacmcauth.core.social.discord.command;

import com.beacmc.beacmcauth.api.BeacmcAuth;
import com.beacmc.beacmcauth.api.auth.AuthManager;
import com.beacmc.beacmcauth.api.config.DiscordConfig;
import com.beacmc.beacmcauth.api.player.ServerPlayer;
import com.beacmc.beacmcauth.api.social.LinkProvider;
import com.beacmc.beacmcauth.core.social.discord.DiscordProvider;
import com.beacmc.beacmcauth.core.util.CodeGenerator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

public class LinkCommand extends ListenerAdapter {

    private final DiscordProvider discordProvider;
    private final LinkProvider linkProvider;
    private final BeacmcAuth plugin;
    private final AuthManager authManager;

    public LinkCommand(BeacmcAuth plugin, DiscordProvider discordProvider) {
        this.discordProvider = discordProvider;
        this.plugin = plugin;
        this.authManager = plugin.getAuthManager();
        this.linkProvider = discordProvider.getLinkProvider();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        final User user = event.getAuthor();
        final DiscordConfig discordConfig = plugin.getDiscordConfig();
        final Message message = event.getMessage();
        final MessageChannelUnion channel = event.getChannel();
        final String[] args = message.getContentRaw().split("\\s+");

        if (!message.getContentRaw().startsWith(discordConfig.getLinkCommand()))
            return;

        if (!(channel instanceof PrivateChannel)) {
            channel.sendMessage(discordConfig.getMessage("private-channel-only")).queue();
            return;
        }

        if (args.length < 2) {
            channel.sendMessage(discordConfig.getMessage("link-command-usage")).queue();
            return;
        }

        String name = args[1].toLowerCase();
        authManager.getProtectedPlayer(name).thenAccept(protectedPlayer -> {
            if (protectedPlayer == null) {
                channel.sendMessage(discordConfig.getMessage("link-command-player-not-found")).queue();
                return;
            }

            ServerPlayer player = plugin.getProxy().getPlayer(protectedPlayer.getLowercaseName());

            if (player == null) {
                channel.sendMessage(discordConfig.getMessage("link-command-player-offline")).queue();
                return;
            }

            if (protectedPlayer.getDiscord() != 0) {
                channel.sendMessage(discordConfig.getMessage("link-command-already-linked")).queue();
                return;
            }

            if (linkProvider.getProtectedPlayersById(user.getIdLong()).size() < discordConfig.getMaxLink()) {
                String code;
                do {
                    code = CodeGenerator.generate(discordConfig.getCodeChars(), discordConfig.getCodeLength());
                } while (linkProvider.getLinkCodes().containsValue(code));
                linkProvider.getLinkCodes().put(protectedPlayer.setLinkCode(code), user.getIdLong());
                channel.sendMessage(discordConfig.getMessage("link-message", Map.of("%name%", player.getName(), "%code%", code))).queue();
                return;
            }
            channel.sendMessage(discordConfig.getMessage("link-limit")).queue();
        });
    }
}
