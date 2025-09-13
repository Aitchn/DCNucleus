package io.aitchn.dcnucleus.jda;

import io.aitchn.dcnucleus.api.discord.event.guide.channel.voice.VoiceServerUpdateEvent;
import io.aitchn.dcnucleus.api.discord.event.guide.channel.voice.VoiceStateUpdateEvent;
import io.aitchn.dcnucleus.jda.guild.JDAGuildAdapter;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;

public class JDAVoiceDispatchInterceptor implements VoiceDispatchInterceptor {
    public static volatile io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceDispatchInterceptor apiVoiceDispatchInterceptor;

    @Override
    public void onVoiceServerUpdate(VoiceServerUpdate voiceServerUpdate) {
        JDAGuildAdapter guild = new JDAGuildAdapter(voiceServerUpdate.getGuild());
        String token = voiceServerUpdate.getToken();
        String endpoint = voiceServerUpdate.getEndpoint();
        String sessionId = voiceServerUpdate.getSessionId();

        VoiceServerUpdateEvent voiceServerUpdateEvent = new VoiceServerUpdateEvent(guild, token, endpoint, sessionId);
        apiVoiceDispatchInterceptor.onVoiceServerUpdate(voiceServerUpdateEvent);
    }

    @Override
    public boolean onVoiceStateUpdate(VoiceStateUpdate voiceStateUpdate) {
        JDAGuildAdapter guild = new JDAGuildAdapter(voiceStateUpdate.getGuild());

        VoiceStateUpdateEvent apiEvent = new VoiceStateUpdateEvent(
                guild,
                voiceStateUpdate.getVoiceState().getSessionId(),
                voiceStateUpdate.getVoiceState().isSelfMuted(),
                voiceStateUpdate.getVoiceState().isSelfDeafened()
        );
        return apiVoiceDispatchInterceptor.onVoiceStateUpdate(apiEvent);
    }
}
