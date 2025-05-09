package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class TestTask implements Task {

    @Autowired
    ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Test Layer Files.");

        final String PROJECT_PATH_TEST = Utility.getResourcePath(resourceModel.getApiRequestModel().getProjectName(),
                AppConstants.BASE_TEST_PATH);
        final String BASE_PACKAGE = Utility.getBasePackage(resourceModel.getApiRequestModel().getGroupId(),
                resourceModel.getApiRequestModel().getArtifactId());

        final String file = Utility.capitalize(resourceModel.getResource()) + "Controller" + "Test";

        // Import statements
        ClassName testAnnotation = ClassName.get("org.junit.jupiter.api", "Test");

        // Create the Test file
        final String FILE_NAME_TEST = file.toString();
        Path filePath = Paths.get(PROJECT_PATH_TEST +"/"+ BASE_PACKAGE +"/"+ FILE_NAME_TEST);

        TypeSpec.Builder testClassBuilder = TypeSpec.classBuilder(FILE_NAME_TEST)
                .addModifiers(Modifier.PUBLIC);

        //TODO
//        List<Method> methods = file.getMethods();
//        methods.forEach(method -> {
//            String methodName = method.getName();
//
//            // Sample JUnit test method
//            MethodSpec testMethod = MethodSpec.methodBuilder(methodName)
//                    .addModifiers(Modifier.PUBLIC)
//                    .addAnnotation(AnnotationSpec.builder(testAnnotation).build())
//                    .returns(void.class)
//                    .build();
//
//            testClassBuilder.addMethod(testMethod);
//
//        });

        // Create the test class
        TypeSpec testClass = testClassBuilder.build();

        // Generate the Java file
        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE, testClass).build();
        try {
            javaFile.writeTo(Paths.get(PROJECT_PATH_TEST));
        } catch (IOException e) {
            log.error("❌ Failed to create the Test file {} ", file.toString(), e);
        }

        resourceModel.setAdditionalProperty(AppConstants.TEST_FILE_PATH, filePath.toAbsolutePath());

        modelFactory.updateApiResponseModel(ApiResponseModel.TEST, filePath.toAbsolutePath().toString());

        log.info("✅ Test File generated successfully at: " + filePath.toAbsolutePath());
    }
}
