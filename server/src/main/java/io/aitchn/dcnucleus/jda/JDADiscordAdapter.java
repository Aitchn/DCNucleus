package io.aitchn.dcnucleus.jda;

import io.aitchn.dcnucleus.api.discord.guild.Discord;
import io.aitchn.dcnucleus.api.discord.guild.Guild;
import io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceDispatchInterceptor;
import io.aitchn.dcnucleus.jda.guild.JDAGuildAdapter;
import net.dv8tion.jda.api.JDA;

import java.util.List;
import java.util.stream.Collectors;

public class JDADiscordAdapter implements Discord {
    private final JDA jda;

    public JDADiscordAdapter(JDA jda) {
        this.jda = jda;
    }

    @Override
    public String getToken() {
        // 這是一個奇怪的問題 JDA 回傳 是 `Bot <Token>` 而不是 `Token`
        return jda.getToken().replaceFirst("^Bot\\s+", "");
    }

    @Override
    public List<Guild> getGuilds() {
        return jda.getGuilds().stream()
                .map(JDAGuildAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public Guild getGuild(long guildId) {
        net.dv8tion.jda.api.entities.Guild jdaGuild = jda.getGuildById(guildId);
        return jdaGuild != null ? new JDAGuildAdapter(jdaGuild) : null;
    }

    @Override
    public void setVoiceDispatchInterceptor(VoiceDispatchInterceptor voiceDispatchInterceptor) {
        JDAVoiceDispatchInterceptor.apiVoiceDispatchInterceptor = voiceDispatchInterceptor;
    }
}
