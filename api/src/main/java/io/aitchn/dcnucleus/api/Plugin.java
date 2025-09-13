package io.aitchn.dcnucleus.api;

import io.aitchn.dcnucleus.api.annotation.Inject;
import io.aitchn.dcnucleus.api.discord.guild.Discord;
import org.slf4j.Logger;

import java.io.File;

public abstract class Plugin {
    @Inject public String name;
    @Inject public Logger logger;
    @Inject public File dataFolder;
    @Inject public Discord discord;
    @Inject public ServerManager serverManager;

    public void onEnable() {}
    public void onDisable() {}

    public String getName() { return name; }
    public Logger getLogger() { return logger; }
    public File getDataFolder() { return dataFolder; }
}
