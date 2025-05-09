package com.npst.spring_boot_project_generator.pom.model;

import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class Plugins {
    @XmlElement(name = "plugin")
    private List<Plugin> plugins = new ArrayList<>();

    public List<Plugin> getPlugins() {
        return plugins;
    }
}


