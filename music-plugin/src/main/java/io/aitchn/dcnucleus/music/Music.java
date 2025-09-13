package io.aitchn.dcnucleus.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.player.*;
import io.aitchn.dcnucleus.api.Plugin;
import io.aitchn.dcnucleus.music.console.MusicConsole;

import static dev.arbjerg.lavalink.client.Helpers.getUserIdFromToken;

public class Music extends Plugin {
    public LavalinkClient lavalink;

    @Override
    public void onEnable() {
        logger.info("Music plugin enabled!");
        logger.info(discord.getToken());
        long userId = getUserIdFromToken(discord.getToken().trim());

        this.lavalink = new LavalinkClient(userId);
        try {
            this.lavalink.addNode(
                    new NodeOptions.Builder()
                            .setName("Music")
                            .setServerUri("ws://ip") // TODO: 留空白 以免洩漏 "D
                            .setPassword("") // TODO: 留空白 以免洩漏 "D
                            .build()
            );
            logger.info("Lavalink node added successfully");
        } catch (Exception e) {
            logger.error("Failed to add Lavalink node", e);
            return;
        }

        LavalinkVoiceAdapter lavalinkVoiceAdapter = new LavalinkVoiceAdapter(this.lavalink);
        discord.setVoiceDispatchInterceptor(lavalinkVoiceAdapter);

        serverManager.registerConsole(new MusicConsole(this), this);
    }}
