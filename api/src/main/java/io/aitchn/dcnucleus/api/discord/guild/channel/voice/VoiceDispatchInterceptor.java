package io.aitchn.dcnucleus.api.discord.guild.channel.voice;

import io.aitchn.dcnucleus.api.discord.event.guide.channel.voice.VoiceServerUpdateEvent;
import io.aitchn.dcnucleus.api.discord.event.guide.channel.voice.VoiceStateUpdateEvent;

public interface VoiceDispatchInterceptor {
    void onVoiceServerUpdate(VoiceServerUpdateEvent event);
    boolean onVoiceStateUpdate(VoiceStateUpdateEvent event);

}
