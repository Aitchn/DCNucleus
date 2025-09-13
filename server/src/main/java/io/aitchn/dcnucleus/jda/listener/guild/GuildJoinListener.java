package io.aitchn.dcnucleus.jda.listener.guild;

import io.aitchn.dcnucleus.jda.DiscordManager;
import io.aitchn.dcnucleus.server.database.impl.GuildRepositoryImpl;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildJoinListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        long guildId = event.getGuild().getIdLong();
        long joinTime = System.currentTimeMillis();

        GuildRepositoryImpl guildRepository = new GuildRepositoryImpl();
        guildRepository.save(guildId, joinTime);

        DiscordManager.logger.info("Guild {} joined at {}", event.getGuild().getName(), joinTime);
    }
}
