package io.aitchn.dcnucleus.jda;

import io.aitchn.dcnucleus.api.discord.guild.Discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordManager {
    public static final Logger logger = LoggerFactory.getLogger("[Discord]");
    private static final DiscordManager instance = new DiscordManager();
    private static JDA jda;
    public static final JDAVoiceDispatchInterceptor voiceDispatchInterceptor = new JDAVoiceDispatchInterceptor();

    private DiscordManager() {}

    public static DiscordManager getInstance() {
        return instance;
    }

    public void start(String token) throws Exception{
        if (jda != null) {
            logger.warn("Discord is already started");
            return;
        }

        JDABuilder jdaBuilder = JDABuilder.createDefault(token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EXPRESSIONS,
                        GatewayIntent.SCHEDULED_EVENTS
                )
                .setActivity(Activity.playing("with DCNucleus"))
                .setVoiceDispatchInterceptor(voiceDispatchInterceptor);

        jda = jdaBuilder.build().awaitReady();

        logger.info("JDA is ready. Logged in as {}", jda.getSelfUser().getAsTag());
    }

    public static @NotNull Discord getDiscord() {
        if (jda == null) {
            throw new IllegalStateException("Discord is not ready");
        }
        return new JDADiscordAdapter(jda);
    }

    public void addEventListener(ListenerAdapter listener) {
        if (jda != null) {
            jda.addEventListener(listener);
        } else {
            logger.warn("JDA is not ready. Cannot add event listener.");
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
            logger.info("JDA shut down.");
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public boolean isRunning() {
        return jda != null;
    }
}
