package io.aitchn.dcnucleus.music.console;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;
import io.aitchn.dcnucleus.api.discord.guild.Guild;
import io.aitchn.dcnucleus.music.Music;

public class MusicConsole implements ConsoleCommand {
    private final Music plugin;

    public MusicConsole(Music plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "music";
    }

    @Override
    public String getDescription() {
        return "音樂播放控制台測試 music play <guildId> <channelId> <url>";
    }

    @Override
    public String getUsage() {
        return "music play <guildId> <channelId> <url>";
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < 4) {
            System.out.println("用法: " + getUsage());
            return false;
        }

        String sub = args[0];
        if ("play".equalsIgnoreCase(sub)) {
            try {
                long guildId = Long.parseLong(args[1]);
                long channelId = Long.parseLong(args[2]);
                String url = args[3];

                play(guildId, channelId, url);
                plugin.logger.info("播放請求: guildId={}, channelId={}, url={}", guildId, channelId, url);
                return true;
            } catch (NumberFormatException e) {
                plugin.logger.warn("guildId 或 channelId 格式錯誤");
                return false;
            }
        }

        plugin.logger.warn("未知子命令");
        return false;
    }

    private void play(long guildId, long channelId, String url) {
        Guild guild = plugin.discord.getGuild(guildId);
        if (guild == null) {
            plugin.logger.warn("找不到 Guild: {}", guildId);
            return;
        }

        guild.joinVoiceChannel(channelId);

        Link link = plugin.lavalink.getOrCreateLink(guildId);
        link.loadItem(url).subscribe(result -> {
            if (result instanceof TrackLoaded tl) {
                link.updatePlayer(p -> p.setTrack(tl.getTrack()).setPaused(false)).subscribe();
            } else if (result instanceof PlaylistLoaded pl) {
                link.updatePlayer(p -> p.setTrack(pl.getTracks().get(0)).setPaused(false)).subscribe();
            } else {
                System.out.println("⚠️ 無法播放: " + url);
            }
        });
    }
}
