package com.npst.spring_boot_project_generator.pom.model;

import jakarta.xml.bind.annotation.XmlElement;

public class Build {
    @XmlElement(name = "plugins")
    private Plugins plugins = new Plugins();

    public void addPlugin(Plugin plugin) {
        plugins.getPlugins().add(plugin);
    }
}
