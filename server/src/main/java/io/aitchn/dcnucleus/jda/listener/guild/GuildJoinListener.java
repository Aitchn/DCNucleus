package io.aitchn.dcnucleus.jda.listener.guild;

import io.aitchn.dcnucleus.jda.DiscordManager;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        long joinTime = System.currentTimeMillis();

        DiscordManager.logger.info("Guild {} joined at {}", event.getGuild().getName(), joinTime);
    }
}
