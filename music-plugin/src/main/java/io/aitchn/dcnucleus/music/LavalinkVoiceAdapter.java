package io.aitchn.dcnucleus.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.protocol.v4.VoiceState;
import io.aitchn.dcnucleus.api.discord.event.guide.channel.voice.VoiceServerUpdateEvent;
import io.aitchn.dcnucleus.api.discord.event.guide.channel.voice.VoiceStateUpdateEvent;
import io.aitchn.dcnucleus.api.discord.guild.Guild;
import io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceDispatchInterceptor;

public class LavalinkVoiceAdapter implements VoiceDispatchInterceptor {
    private final LavalinkClient lavalink;

    public LavalinkVoiceAdapter(LavalinkClient lavalink) {
        this.lavalink = lavalink;
    }


    @Override
    public void onVoiceServerUpdate(VoiceServerUpdateEvent event) {
        Guild guild = event.guild();
        VoiceState voiceState = new VoiceState(event.token(), event.endpoint(), event.sessionId());

        lavalink.getOrCreateLink(guild.getId())
                .onVoiceServerUpdate(voiceState);
    }

    @Override
    public boolean onVoiceStateUpdate(VoiceStateUpdateEvent event) {
        return false;
    }
}
