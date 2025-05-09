package com.npst.spring_boot_project_generator.pom.model;

import jakarta.xml.bind.annotation.XmlElement;

public class Parent {
    @XmlElement(name = "groupId")
    private String groupId;

    @XmlElement(name = "artifactId")
    private String artifactId;

    @XmlElement(name = "version")
    private String version;

    @XmlElement(name = "relativePath")
    private String relativePath = "";

    public Parent() {}

    public Parent(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
}

