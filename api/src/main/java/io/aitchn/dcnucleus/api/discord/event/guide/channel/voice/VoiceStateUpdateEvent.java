package io.aitchn.dcnucleus.api.discord.event.guide.channel.voice;

import io.aitchn.dcnucleus.api.discord.guild.Guild;

public record VoiceStateUpdateEvent(
        Guild guild,
        String sessionId,
        boolean selfMuted,
        boolean selfDeafened
) {}
