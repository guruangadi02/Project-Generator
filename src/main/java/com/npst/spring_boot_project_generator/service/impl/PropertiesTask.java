package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class PropertiesTask implements Task {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Properties file.");

        final String PROJECT_PATH_PROPERTIES = Utility.getResourcePath(resourceModel.getApiRequestModel().getProjectName(),
                AppConstants.BASE_RESOURCE_PATH);

        Path resourcesPath = Paths.get(PROJECT_PATH_PROPERTIES);
        if (!Files.exists(resourcesPath)) {
            try {
                Files.createDirectories(resourcesPath);
            } catch (IOException e) {
                log.error("❌ Failed to create directories {} ", resourcesPath, e);
            }
        }

        List<String> files = Arrays.asList("application.properties", "application-dev.properties");

        files.forEach(file -> {
            log.info("✅ Creating Properties file: {}", file);
            String propertiesContent = """
				# Spring Boot Application Properties
				
				server.port=9090
				
				spring.jackson.mapper.accept-case-insensitive-properties=true
				
                # H2 Database Configuration
                spring.datasource.url=jdbc:h2:mem:testdb
                spring.datasource.driver-class-name=org.h2.Driver
                spring.datasource.username=sa
                spring.datasource.password=
                
                # JPA Settings
                spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
                spring.jpa.hibernate.ddl-auto=update
                
                # Enable H2 Console
                spring.h2.console.enabled=true
                spring.h2.console.path=/h2-console
				
				""";

            // Create the properties file
            final String FILE_NAME_PROPERTIES = file.toString();
            Path filePath = Paths.get(PROJECT_PATH_PROPERTIES + FILE_NAME_PROPERTIES);
            try {
                Files.writeString(filePath, propertiesContent);
            } catch (IOException e) {
                log.error("❌ Failed to create the properties file {} ", file.toString(), e);
            }

            modelFactory.updateApiResponseModel(ApiResponseModel.PROPERTIES, filePath.toAbsolutePath().toString());

            log.info("✅ {} file generated successfully at: {}", file.toString(), filePath.toAbsolutePath());

        });
    }
}
