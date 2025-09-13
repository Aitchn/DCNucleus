package io.aitchn.dcnucleus.jda.listener.guild;

import io.aitchn.dcnucleus.jda.DiscordManager;
import io.aitchn.dcnucleus.server.database.impl.GuildRepositoryImpl;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildLeaveListener extends ListenerAdapter {

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        long guildId = event.getGuild().getIdLong();

        GuildRepositoryImpl guildRepository = new GuildRepositoryImpl();
        guildRepository.delete(guildId);

        DiscordManager.logger.info("Guild {} left", event.getGuild().getName());
    }
}
