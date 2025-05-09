package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;


import java.io.IOException;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AppConfigTask implements Task {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating App Configurtaion file.");

        final String name = AppConstants.CONFIG_FOLDER;
        List<String> files = Arrays.asList("AppConfig");

        files.forEach(file -> {

            final String CLASS_NAME_CONFIG = file.toString();
            final String BASE_PACKAGE = Utility.getBasePackage(resourceModel.getApiRequestModel().getGroupId(),
                    resourceModel.getApiRequestModel().getArtifactId());
            final String BASE_PACKAGE_CONFIG = BASE_PACKAGE + "." + name;
            final String PROJECT_PATH = Utility.getProjectBasePath(resourceModel.getApiRequestModel().getProjectName(),
                    AppConstants.BASE_JAVA_PATH);

            // Define @Configuration annotation
            AnnotationSpec configurationAnnotation = AnnotationSpec.builder(Configuration.class).build();

            // Define a sample Bean method
            MethodSpec sampleBeanMethod = MethodSpec.methodBuilder(CLASS_NAME_CONFIG+"_Bean")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Bean.class)
                    .returns(String.class)
                    .addStatement("return $S", "This is a sample Bean")
                    .build();

            // Define the configuration class
            TypeSpec configClass = TypeSpec.classBuilder(CLASS_NAME_CONFIG)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(configurationAnnotation)
                    .addMethod(sampleBeanMethod)
                    .build();

            // Create the Java file
            JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_CONFIG, configClass).build();

            // Write the generated file to the correct directory
            try {
                javaFile.writeTo(Paths.get(PROJECT_PATH));
            } catch (IOException e) {
                log.error("❌ Failed to create the App config file {} ", file.toString(), e);
            }

            String appConfigFilePath = PROJECT_PATH +
                    BASE_PACKAGE_CONFIG.replace(".", "/") + "/" +
                    CLASS_NAME_CONFIG + ".java";
            resourceModel.setAdditionalProperty(AppConstants.APP_CONFIG_FILE_PATH, appConfigFilePath);

            modelFactory.updateApiResponseModel(ApiResponseModel.CONFIGURATION, appConfigFilePath);

            log.info("✅ Configuration class generated successfully at: ", appConfigFilePath);

        });
    }
}
