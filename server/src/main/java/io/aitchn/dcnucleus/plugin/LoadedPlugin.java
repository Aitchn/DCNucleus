package io.aitchn.dcnucleus.plugin;

import io.aitchn.dcnucleus.api.Plugin;

/**
 * 已載入的插件封裝：
 * - descriptor: 插件描述檔 (plugin.yml)
 * - instance: 插件的主類實例
 * - classLoader: 插件專屬的類別載入器
 */
public class LoadedPlugin {
    private final PluginDescriptor descriptor;
    private final Plugin instance;
    private final PluginClassLoader classLoader;

    public LoadedPlugin(PluginDescriptor descriptor, Plugin instance, PluginClassLoader classLoader) {
        this.descriptor = descriptor;
        this.instance = instance;
        this.classLoader = classLoader;
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public Plugin getInstance() {
        return instance;
    }

    public PluginClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        return "LoadedPlugin{" +
                "descriptor=" + descriptor +
                ", instance=" + instance +
                ", classLoader=" + classLoader +
                '}';
    }
}