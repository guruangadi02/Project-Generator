package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.pom.model.*;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class PomTask implements Task {

    @Autowired
    ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating POM file");

        final String POM_PATH = Utility.getPomPath(resourceModel.getApiRequestModel().getProjectName());

        //Create Metadata.
        PomModel pomModel = new PomModel("4.0.0",
                resourceModel.getApiRequestModel().getGroupId(),
                resourceModel.getApiRequestModel().getArtifactId(),
                resourceModel.getApiRequestModel().getVersion(),
                "jar");

        // Add Parent
        pomModel.setParent(new Parent("org.springframework.boot",
                "spring-boot-starter-parent",
                "3.4.4"));

        // Add Dependencies
        pomModel.addDependency(new Dependency("org.springframework.boot", "spring-boot-starter"));
        pomModel.addDependency(new Dependency("org.springframework.boot", "spring-boot-starter-web"));
        pomModel.addDependency(new Dependency("org.projectlombok", "lombok", "provided"));
        pomModel.addDependency(new Dependency("org.springframework.boot", "spring-boot-starter-data-jpa"));
        pomModel.addDependency(new Dependency("org.springframework.boot", "spring-boot-starter-test"));

        pomModel.addDependency(new Dependency("com.h2database", "h2", "runtime"));


        pomModel.addDependency(new Dependency("org.junit.jupiter", "junit-jupiter-api", "test"));

        // Add Build Plugins (Spring Boot Maven Plugin)
        pomModel.setBuild(new Build());
        pomModel.getBuild().addPlugin(new Plugin("org.springframework.boot", "spring-boot-maven-plugin"));


        try {
            // Configure JAXB
            JAXBContext context = JAXBContext.newInstance(PomModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Generate pom.xml
            marshaller.marshal(pomModel, new File(POM_PATH));

            resourceModel.setAdditionalProperty(AppConstants.POM_FILE_PATH, POM_PATH);

            modelFactory.updateApiResponseModel(ApiResponseModel.POM, POM_PATH);

            log.info("✅ pom.xml generated successfully : {}", POM_PATH);

        } catch (JAXBException e) {
            log.error("❌ POM File creation failed:", e);
        }
    }
}
