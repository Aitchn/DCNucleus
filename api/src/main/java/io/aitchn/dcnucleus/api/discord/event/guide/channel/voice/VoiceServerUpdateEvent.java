package io.aitchn.dcnucleus.api.discord.event.guide.channel.voice;

import io.aitchn.dcnucleus.api.discord.guild.Guild;

public record VoiceServerUpdateEvent(
        Guild guild,
        String token,
        String endpoint,
        String sessionId
) {}