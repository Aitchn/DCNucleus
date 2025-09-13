package io.aitchn.dcnucleus.api.discord.guild;

import io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceDispatchInterceptor;

import java.util.List;

public interface Discord {

    String getToken();

    List<Guild> getGuilds();

    Guild getGuild(long guildId);

    void setVoiceDispatchInterceptor(VoiceDispatchInterceptor voiceDispatchInterceptor);
}
