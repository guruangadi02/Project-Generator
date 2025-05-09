package com.npst.spring_boot_project_generator.pom.model;

import jakarta.xml.bind.annotation.XmlElement;

public class Plugin {
    @XmlElement(name = "groupId")
    private String groupId;

    @XmlElement(name = "artifactId")
    private String artifactId;

    public Plugin() {}

    public Plugin(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }
}