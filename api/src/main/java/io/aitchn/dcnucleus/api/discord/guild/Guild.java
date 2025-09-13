package io.aitchn.dcnucleus.api.discord.guild;

import io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceChannel;

import java.util.List;

public interface Guild {

    long getId();

    String getName();

    List<VoiceChannel> getVoiceChannels();

    VoiceChannel getVoiceChannelById(long channelId);

    void joinVoiceChannel(long channelId);

    void leaveVoiceChannel();
}
