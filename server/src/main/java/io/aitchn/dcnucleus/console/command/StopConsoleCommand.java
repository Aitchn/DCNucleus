package io.aitchn.dcnucleus.console.command;

import io.aitchn.dcnucleus.DCNucleus;
import io.aitchn.dcnucleus.api.console.ConsoleCommand;
import io.aitchn.dcnucleus.jda.DiscordManager;
import io.aitchn.dcnucleus.plugin.PluginManager;

public class StopConsoleCommand implements ConsoleCommand {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stop the DCNucleus server and disable all plugins.";
    }

    @Override
    public String getUsage() {
        return "stop";
    }

    @Override
    public boolean execute(String[] args) {
        DCNucleus.logger.info("Shutting down...");

        // 停用所有插件
        PluginManager.getInstance().disableAll();
        DiscordManager.getInstance().shutdown();

        // 退出應用程式
        System.exit(0);
        return true; // 不會到這裡，但為了編譯通過
    }
}
