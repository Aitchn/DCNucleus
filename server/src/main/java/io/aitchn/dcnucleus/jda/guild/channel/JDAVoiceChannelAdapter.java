package io.aitchn.dcnucleus.jda.guild.channel;

import io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceChannel;

public class JDAVoiceChannelAdapter implements VoiceChannel {
    private final net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel channel;

    public JDAVoiceChannelAdapter(net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel voiceChannel) {
        this.channel = voiceChannel;
    }

    @Override
    public long getId() {
        return channel.getIdLong();
    }

    @Override
    public String getName() {
        return channel.getName();
    }

    @Override
    public long getGuildId() {
        return channel.getGuild().getIdLong();
    }
}
