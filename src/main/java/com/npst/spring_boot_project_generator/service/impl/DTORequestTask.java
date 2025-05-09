package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.*;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
public class DTORequestTask implements Task {

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private NestedEntityTask nestedEntityTask;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating DTO Request Layer Files...");

        final String name = AppConstants.DTO_FOLDER + "." + AppConstants.DTO_REQUEST_FOLDER;
        final String file = Utility.capitalize(resourceModel.getResource()) + "Request" + "DTO";

        final String CLASS_NAME_ENTITY = file.toString();
        final String BASE_PACKAGE = resourceModel.getAdditionalProperty(AppConstants.BASE_PACKAGE_KEY).toString();
        final String BASE_PACKAGE_ENTITY = BASE_PACKAGE + "." + name;
        final String PROJECT_PATH = resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString();

        Optional<Map<String, Object>> stringObjectMap = Optional.ofNullable(resourceModel.getRequests())
                .filter(list -> !list.isEmpty())
                .flatMap(list -> list.stream()
                        .filter(req -> Utility.isPostOrPut(req.getHttpMethod()))
                        .findFirst()
                        .flatMap(req -> Utility.getFirstHttpRequest(req))
                );

        // Import statements
        ClassName jsonAnySetter = ClassName.get("com.fasterxml.jackson.annotation", "JsonAnySetter");

        stringObjectMap.ifPresentOrElse(
                reqestData -> {

                    DTOWithJson dtoClass = nestedEntityTask.buildNestedDTO(CLASS_NAME_ENTITY, reqestData, false);

                    // Create the Java file
                    JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_ENTITY, dtoClass.getTypeSpec()).build();

                    // Write the generated file to the correct directory
                    try {
                        javaFile.writeTo(Paths.get(PROJECT_PATH));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String entityFilePath = PROJECT_PATH +
                            BASE_PACKAGE_ENTITY.replace(".", "/") + "/" +
                            CLASS_NAME_ENTITY+".java";

                    resourceModel.setAdditionalProperty(AppConstants.DTO_REQUEST_PATH, entityFilePath);

                    modelFactory.updateApiResponseModel(ApiResponseModel.DTO, entityFilePath);

                    log.info("✅ Spring Boot DTO Request File generated successfully at: {}", entityFilePath);

                },
                () -> log.warn("No Request found")
        );
    }
}
