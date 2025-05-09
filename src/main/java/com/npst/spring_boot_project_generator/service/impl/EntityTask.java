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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class EntityTask implements Task {

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private NestedEntityTask nestedEntityTask;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Entity Layer Files...");

        final String name = AppConstants.ENTITY_FOLDER;
        final String file = Utility.capitalize(resourceModel.getResource()) + "Entity";

        final String CLASS_NAME_ENTITY = file.toString();
        final String BASE_PACKAGE = resourceModel.getAdditionalProperty(AppConstants.BASE_PACKAGE_KEY).toString();
        final String BASE_PACKAGE_ENTITY = BASE_PACKAGE + "." + name;
        final String PROJECT_PATH = resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString();

        // Define @Entity annotation
        AnnotationSpec entityAnnotation = AnnotationSpec.builder(Entity.class).build();

        // Define @Table annotation
        AnnotationSpec tableAnnotation = AnnotationSpec.builder(Table.class)
                .build();

        // Define Lombok annotations
        AnnotationSpec getterAnnotation = AnnotationSpec.builder(ClassName.get("lombok", "Getter")).build();
        AnnotationSpec setterAnnotation = AnnotationSpec.builder(ClassName.get("lombok", "Setter")).build();
        AnnotationSpec noArgsConstructor = AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor")).build();
        AnnotationSpec allArgsConstructor = AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor")).build();
        AnnotationSpec dataAnnotation = AnnotationSpec.builder(ClassName.get("lombok", "Data")).build();

        // Define the entity class with Lombok annotations
        TypeSpec.Builder entityClassBuilder = TypeSpec.classBuilder(CLASS_NAME_ENTITY)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(noArgsConstructor)
                .addAnnotation(dataAnnotation)
                .addAnnotation(entityAnnotation)
                .addAnnotation(tableAnnotation);

        Optional<Map<String, Object>> stringObjectMap = Optional.ofNullable(resourceModel.getRequests())
                .filter(list -> !list.isEmpty())
                .flatMap(list -> list.stream()
                        .filter(req -> Utility.isPostOrPut(req.getHttpMethod()))
                        .findFirst()
                        .flatMap(req -> Utility.getFirstHttpRequest(req))
                );

        //Add id field..
        FieldSpec idField = FieldSpec.builder(Integer.class, "id", Modifier.PRIVATE)
                .addAnnotation(Id.class)
                .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                        .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                        .build())
                .build();

        entityClassBuilder.addField(idField);

        stringObjectMap.ifPresentOrElse(
                reqestData -> {

                    DTOWithJson dtoClass = nestedEntityTask.buildNestedDTO(CLASS_NAME_ENTITY, reqestData, true);

                     //Create the Java file
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

                    resourceModel.setAdditionalProperty(AppConstants.ENTITY_FILE_PATH, entityFilePath);

                    modelFactory.updateApiResponseModel(ApiResponseModel.ENTITY, entityFilePath);

                    log.info("✅ Spring Boot Entity File generated successfully at: {}", entityFilePath);

                },
                () -> log.warn("No Request found")
        );
    }
}
