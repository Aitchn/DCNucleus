package io.aitchn.dcnucleus.api;

import io.aitchn.dcnucleus.api.annotation.Inject;
import org.slf4j.Logger;

import java.io.File;

public abstract class Plugin {
    @Inject private String name;
    @Inject private Logger logger;
    @Inject private File dataFolder;

    public void onEnable() {}
    public void onDisable() {}

    public String getName() { return name; }
    public Logger getLogger() { return logger; }
    public File getDataFolder() { return dataFolder; }
}
