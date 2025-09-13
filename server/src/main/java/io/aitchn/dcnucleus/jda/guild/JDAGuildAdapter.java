package io.aitchn.dcnucleus.jda.guild;

import io.aitchn.dcnucleus.api.discord.guild.Guild;
import io.aitchn.dcnucleus.api.discord.guild.channel.voice.VoiceChannel;
import io.aitchn.dcnucleus.jda.guild.channel.JDAVoiceChannelAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class JDAGuildAdapter implements Guild {
    private final net.dv8tion.jda.api.entities.Guild guild;

    public JDAGuildAdapter(net.dv8tion.jda.api.entities.Guild guild) {
        this.guild = guild;
    }

    @Override
    public long getId() {
        return guild.getIdLong();
    }

    @Override
    public String getName() {
        return guild.getName();
    }

    @Override
    public List<VoiceChannel> getVoiceChannels() {
        return guild.getVoiceChannels().stream()
                .map(JDAVoiceChannelAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public VoiceChannel getVoiceChannelById(long channelId) {
        return null;
    }

    @Override
    public void joinVoiceChannel(long channelId) {
        net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel channel =
                guild.getVoiceChannelById(channelId);
        if (channel != null) {
            guild.getAudioManager().openAudioConnection(channel);
        }
    }

    @Override
    public void leaveVoiceChannel() {
        guild.getAudioManager().closeAudioConnection();
    }
}
