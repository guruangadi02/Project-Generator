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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@Component
public class MainApplicationTask implements Task {

    @Autowired
    ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Main Application file");

        final String PROJECT_PATH = Utility.getProjectBasePath(resourceModel.getApiRequestModel().getProjectName(), AppConstants.BASE_JAVA_PATH);
        final String BASE_PACKAGE = Utility.getBasePackage(resourceModel.getApiRequestModel().getGroupId(),
                resourceModel.getApiRequestModel().getArtifactId());
        final String CLASS_NAME = Utility.getMainApplicationClassName(resourceModel.getApiRequestModel().getArtifactId());


        // Define the @SpringBootApplication annotation
        AnnotationSpec springBootApplication = AnnotationSpec.builder(SpringBootApplication.class)
                //.addMember("exclude", "{$T.class}", DataSourceAutoConfiguration.class)
                .build();


        // Define the main method
        MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.run($L.class, args)", SpringApplication.class, CLASS_NAME)
                .addStatement("$T.out.println($S)", System.class, "***** STARTING "+ CLASS_NAME + "*****")
                .build();

        // Define the main application class
        TypeSpec mainClass = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(springBootApplication)
                .addMethod(mainMethod)
                .build();

        // Create the Java file
        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE, mainClass)
                .build();

        // Write the generated file
        try {
            javaFile.writeTo(Paths.get(PROJECT_PATH));
        } catch (IOException e) {
            log.error("❌ Main Application File creation failed:", e);
        }

        String mainApplicationFilePath = PROJECT_PATH +
                BASE_PACKAGE.replace(".", "/") + "/" +
                CLASS_NAME + ".java";

        resourceModel.setAdditionalProperty(AppConstants.MAIN_APPLICATION_FILE_PATH, mainApplicationFilePath);

        modelFactory.updateApiResponseModel(ApiResponseModel.MAIN_APPLICATION, mainApplicationFilePath);

        log.info("✅ Spring Boot Main Application generated successfully at: {}", mainApplicationFilePath);

    }
}
