package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ExceptionTask implements Task {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Exception Handler File.");

        final String name = AppConstants.EXCEPTION_FOLDER;
        List<String> files = Arrays.asList("GlobalExceptionHandler");

        files.forEach(file -> {

            final String CLASS_NAME_EXCEPTION = file.toString();
            final String BASE_PACKAGE = Utility.getBasePackage(resourceModel.getApiRequestModel().getGroupId(),
                    resourceModel.getApiRequestModel().getArtifactId());
            final String BASE_PACKAGE_EXCEPTION = BASE_PACKAGE + "." + name;
            final String PROJECT_PATH = Utility.getProjectBasePath(resourceModel.getApiRequestModel().getProjectName(),
                    AppConstants.BASE_JAVA_PATH);


            // Define @ControllerAdvice annotation
            AnnotationSpec controllerAdviceAnnotation = AnnotationSpec.builder(ControllerAdvice.class).build();

            // Define the exception handler class
            TypeSpec exceptionHandlerClass = TypeSpec.classBuilder(CLASS_NAME_EXCEPTION)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(controllerAdviceAnnotation)
                    .build();

            // Create the Java file
            JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_EXCEPTION, exceptionHandlerClass).build();

            // Write the generated file to the correct directory
            try {
                javaFile.writeTo(Paths.get(PROJECT_PATH));
            } catch (IOException e) {
                log.error("❌ Failed to create the Exception handler file {} ", file.toString(), e);
            }

            String exceptionFilePath = PROJECT_PATH +
                    BASE_PACKAGE_EXCEPTION.replace(".", "/") + "/" +
                    CLASS_NAME_EXCEPTION + ".java";

            resourceModel.setAdditionalProperty(AppConstants.EXCEPTION_FILE_PATH, exceptionFilePath);

            modelFactory.updateApiResponseModel(ApiResponseModel.EXCEPTION, exceptionFilePath);

            log.info("✅ Global Exception Handler file generated successfully at: {}", exceptionFilePath);

        });
    }
}
