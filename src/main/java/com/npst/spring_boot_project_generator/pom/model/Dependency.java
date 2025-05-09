package com.npst.spring_boot_project_generator.pom.model;

import jakarta.xml.bind.annotation.XmlElement;

public class Dependency {
    @XmlElement(name = "groupId")
    private String groupId;

    @XmlElement(name = "artifactId")
    private String artifactId;

    @XmlElement(name = "scope")
    private String scope;

    public Dependency() {}

    public Dependency(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    public Dependency(String groupId, String artifactId, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.scope = scope;
    }
}