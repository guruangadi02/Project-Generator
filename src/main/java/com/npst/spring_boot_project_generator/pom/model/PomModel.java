package com.npst.spring_boot_project_generator.pom.model;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "modelVersion", "parent", "groupId", "artifactId",
                        "version", "packaging", "dependencies", "build" })
public class PomModel {

    @XmlAttribute
    private final String xmlns = "http://maven.apache.org/POM/4.0.0";

    @XmlAttribute(name = "xmlns:xsi")
    private final String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";

    @XmlAttribute(name = "xsi:schemaLocation")
    private final String schemaLocation = "http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd";

    @XmlElement(name = "modelVersion")
    private String modelVersion;

    @XmlElement(name = "groupId")
    private String groupId;

    @XmlElement(name = "artifactId")
    private String artifactId;

    @XmlElement(name = "version")
    private String version;

    @XmlElement(name = "packaging")
    private String packaging;

    @XmlElement(name = "parent")
    private Parent parent;

    @XmlElement(name = "dependencies")
    private Dependencies dependencies = new Dependencies();

    @XmlElement(name = "build")
    private Build build;

    public PomModel() {}

    public PomModel(String modelVersion, String groupId, String artifactId, String version, String packaging) {
        this.modelVersion = modelVersion;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    public void addDependency(Dependency dependency) {
        dependencies.getDependencies().add(dependency);
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public Build getBuild() {
        return build;
    }
}

// === Model for Parent ===

// === Model for Dependencies ===
class Dependencies {
    @XmlElement(name = "dependency")
    private List<Dependency> dependencies = new ArrayList<>();

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}

