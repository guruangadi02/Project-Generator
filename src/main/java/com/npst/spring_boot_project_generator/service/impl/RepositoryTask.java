package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@Component
public class RepositoryTask implements Task {

    @Autowired
    ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Repository Layer Files...");

        final String name = AppConstants.REPOSITORY_FOLDER;
        final String file = Utility.capitalize(resourceModel.getResource()) + "Repository";

        final String CLASS_NAME_REPOSITORY = file.toString();
        final String BASE_PACKAGE = resourceModel.getAdditionalProperty(AppConstants.BASE_PACKAGE_KEY).toString();
        final String BASE_PACKAGE_REPOSITORY = BASE_PACKAGE + "." + name;
        final String PROJECT_PATH = resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString();

        // Define @Repository annotation
        AnnotationSpec repositoryAnnotation = AnnotationSpec.builder(Repository.class).build();

        String entityPkgPathInfo = resourceModel.getAdditionalProperty(AppConstants.ENTITY_FILE_PATH).toString();
        String[] entityPkgfullName = Utility.packageSplitter(entityPkgPathInfo,
                resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString());


        // Define repository interface
        TypeSpec repositoryInterface = TypeSpec.interfaceBuilder(CLASS_NAME_REPOSITORY)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(repositoryAnnotation)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get("org.springframework.data.jpa.repository", "JpaRepository"),
                        ClassName.get(entityPkgfullName[0], entityPkgfullName[1]),
                        ClassName.get(Integer.class)
                ))
                .build();

        // Create the Java file
        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_REPOSITORY, repositoryInterface).build();

        // Write the generated file to the correct directory
        try {
            javaFile.writeTo(Paths.get(PROJECT_PATH));
        } catch (IOException e) {
            log.error("❌ Failed to create the Repository file {} ", file.toString(), e);
        }

        String repositoryFilePath = PROJECT_PATH +
                BASE_PACKAGE_REPOSITORY.replace(".", "/") + "/" +
                CLASS_NAME_REPOSITORY + ".java";
        resourceModel.setAdditionalProperty(AppConstants.REPOSITORY_FILE_PATH, repositoryFilePath);

        modelFactory.updateApiResponseModel(ApiResponseModel.REPOSITORY, repositoryFilePath);

        log.info("✅ Spring Boot Repository File generated successfully at: {}", repositoryFilePath);
    }
}
