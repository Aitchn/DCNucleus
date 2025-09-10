package io.aitchn.dcnucleus.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 對應 plugin.yml
 */
public class PluginDescriptor {
    private String name;
    private String version;
    private String entrypoint;

    public PluginDescriptor() {
    }

    public PluginDescriptor(String name, String version, String entrypoint) {
        this.name = name;
        this.version = version;
        this.entrypoint = entrypoint;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("entrypoint")
    public String getEntrypoint() {
        return entrypoint;
    }

    @JsonProperty("entrypoint")
    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    @Override
    public String toString() {
        return "PluginDescriptor{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", entrypoint='" + entrypoint + '\'' +
                '}';
    }
}
