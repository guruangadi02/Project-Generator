package com.npst.spring_boot_project_generator.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class NestedEntityTask {

    @Autowired
    private ModelFactory modelFactory;

    private String execute(String str, Object value, String folder, ResourceModel resourceModel) {

        log.info("✅ Creating Nested Entity Layer Files...");

        final String name = folder; //AppConstants.ENTITY_FOLDER;
        final String file = Utility.capitalize(str); //+ "Entity";

        final String CLASS_NAME_ENTITY = file.toString();
        final String BASE_PACKAGE = resourceModel.getAdditionalProperty(AppConstants.BASE_PACKAGE_KEY).toString();
        final String BASE_PACKAGE_ENTITY = BASE_PACKAGE + "." + name;
        final String PROJECT_PATH = resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString();


        // Define Lombok annotations
        AnnotationSpec getterAnnotation = AnnotationSpec.builder(ClassName.get("lombok", "Getter")).build();
        AnnotationSpec setterAnnotation = AnnotationSpec.builder(ClassName.get("lombok", "Setter")).build();
        AnnotationSpec noArgsConstructor = AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor")).build();
        AnnotationSpec allArgsConstructor = AnnotationSpec.builder(ClassName.get("lombok", "AllArgsConstructor")).build();
        AnnotationSpec dataAnnotation = AnnotationSpec.builder(ClassName.get("lombok", "Data")).build();
        AnnotationSpec embeddableAnnotation = AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Embeddable")).build();


        // Define the entity class with Lombok annotations
        TypeSpec.Builder entityClassBuilder = TypeSpec.classBuilder(CLASS_NAME_ENTITY)
                .addModifiers(Modifier.PUBLIC)
                //.addAnnotation(getterAnnotation)
                //.addAnnotation(setterAnnotation)
                .addAnnotation(noArgsConstructor)
                .addAnnotation(dataAnnotation)
                //.addAnnotation(allArgsConstructor)
                .addAnnotation(embeddableAnnotation);


        LinkedHashMap<?, ?> map = new LinkedHashMap<>();

        if(value instanceof ArrayList<?>){
            ArrayList<?> list = (ArrayList<?>) value;

            if (!list.isEmpty()) {
                Object item = list.get(0);
                if (item instanceof LinkedHashMap<?, ?>) {
                    map = (LinkedHashMap<?, ?>) item;
                }
            }
        }

        if (value instanceof LinkedHashMap<?,?>) {
            map = (LinkedHashMap<?, ?>) value;
        }

        map.forEach((k, v) -> {

            String fieldName = k.toString(); //Utility.lowercaseFirstChar(k.toString()); //key;
            Class<?> clazz = Utility.identifyType(v);

            FieldSpec otherField = FieldSpec.builder(clazz, fieldName, Modifier.PRIVATE)
                    .build();

            entityClassBuilder.addField(otherField);

            // Build the class
            TypeSpec entityClass = entityClassBuilder.build();

            // Create the Java file
            JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_ENTITY, entityClass).build();

            // Write the generated file to the correct directory
            try {
                javaFile.writeTo(Paths.get(PROJECT_PATH));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        String entityFilePath = PROJECT_PATH +
                BASE_PACKAGE_ENTITY.replace(".", "/") + "/" +
                CLASS_NAME_ENTITY+".java";

        //resourceModel.setAdditionalProperty(AppConstants.ENTITY_FILE_PATH, entityFilePath);

        //modelFactory.updateApiResponseModel(ApiResponseModel.ENTITY, entityFilePath);

        log.info("✅ Spring Boot Nested Entity File generated successfully at: {}", entityFilePath);

        return CLASS_NAME_ENTITY;
    }

    public static DTOWithJson buildNestedDTO(String className, Map<String, Object> jsonMap, boolean isEntityModule) {

        ClassName dataAnnotation = ClassName.get("lombok", "Data");
        AnnotationSpec noArgsConstructor = AnnotationSpec.builder(ClassName.get("lombok", "NoArgsConstructor")).build();

        TypeSpec.Builder parentBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(dataAnnotation)
                .addAnnotation(noArgsConstructor);

        boolean isEntity = className.contains("Entity");

        if (isEntity) {
            parentBuilder
                    .addAnnotation(AnnotationSpec.builder(Entity.class).build())
                    .addAnnotation(AnnotationSpec.builder(Table.class).build());

            FieldSpec idField = FieldSpec.builder(Integer.class, "id", Modifier.PRIVATE)
                    .addAnnotation(AnnotationSpec.builder(Id.class).build())
                    .addAnnotation(AnnotationSpec.builder(GeneratedValue.class)
                            .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                            .build())
                    .build();

            parentBuilder.addField(idField);
        } else if (isEntityModule) {
            parentBuilder
                    .addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Embeddable")).build());
        }

        Map<String, TypeSpec> nestedClasses = new LinkedHashMap<>();

        Map<String, Object> dummyMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            FieldSpec field;
            Object dummyValue;

            if (value instanceof Map mapValue)
            {
                String nestedClassName = toPascal(key) + "DTO";
                DTOWithJson nested = buildNestedDTO(nestedClassName, mapValue, isEntityModule);
                nestedClasses.put(nestedClassName, nested.getTypeSpec());

                FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.bestGuess(nestedClassName), key, Modifier.PRIVATE);

                if (isEntity) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Embedded")).build());
                }

                field = fieldBuilder.build();
                dummyValue = generateDummyJsonMap(mapValue);


            } else if (value instanceof List list && !list.isEmpty() && list.get(0) instanceof Map)
            {
                String nestedClassName = toPascal(key.substring(0, key.length() - 1)) + "DTO";
                DTOWithJson nested = buildNestedDTO(nestedClassName, (Map<String, Object>) list.get(0), isEntityModule);
                nestedClasses.put(nestedClassName, nested.getTypeSpec());
                ParameterizedTypeName listType = ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.bestGuess(nestedClassName)
                );
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(listType, key, Modifier.PRIVATE);

                if (isEntity) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "ElementCollection")).build());
                }

                field = fieldBuilder.build();
                dummyValue = List.of(generateDummyJsonMap((Map<String, Object>) list.get(0)));

            } else if (value instanceof Integer)
            {
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(TypeName.INT, key, Modifier.PRIVATE);

                if (isEntity) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column")).build());
                }

                field = fieldBuilder.build();
                dummyValue = 123;
            } else
            {
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(String.class), key, Modifier.PRIVATE);

                if (isEntity) {
                    fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.persistence", "Column")).build());
                }

                field = fieldBuilder.build();
                dummyValue = "dummy_" + key;
            }

            parentBuilder.addField(field);
            dummyMap.put(key, dummyValue);
        }

        for (TypeSpec nested : nestedClasses.values()) {
            parentBuilder.addType(nested.toBuilder().addModifiers(Modifier.STATIC).build());
        }

        TypeSpec finalClass = parentBuilder.build();

        String jsonOutput = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dummyMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DTOWithJson(finalClass, jsonOutput);
    }

    public static DTOWithJson buildNestedDTO_(String className, Map<String, Object> jsonMap) {
        ClassName dataAnnotation = ClassName.get("lombok", "Data");
        TypeSpec.Builder parentBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(dataAnnotation);

        Map<String, TypeSpec> nestedClasses = new LinkedHashMap<>();
        Map<String, Object> dummyMap = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            FieldSpec field;
            Object dummyValue;

            if (value instanceof Map mapValue) {
                String nestedClassName = toPascal(key) + "DTO";
                DTOWithJson nested = buildNestedDTO_(nestedClassName, mapValue);
                nestedClasses.put(nestedClassName, nested.getTypeSpec());
                field = FieldSpec.builder(ClassName.bestGuess(nestedClassName), key, Modifier.PRIVATE).build();
                dummyValue = generateDummyJsonMap(mapValue);
            } else if (value instanceof List list && !list.isEmpty() && list.get(0) instanceof Map) {
                String nestedClassName = toPascal(key.substring(0, key.length() - 1)) + "DTO";
                DTOWithJson nested = buildNestedDTO_(nestedClassName, (Map<String, Object>) list.get(0));
                nestedClasses.put(nestedClassName, nested.getTypeSpec());

                ParameterizedTypeName listType = ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.bestGuess(nestedClassName)
                );
                field = FieldSpec.builder(listType, key, Modifier.PRIVATE).build();
                dummyValue = List.of(generateDummyJsonMap((Map<String, Object>) list.get(0)));
            } else if (value instanceof Integer) {
                field = FieldSpec.builder(TypeName.INT, key, Modifier.PRIVATE).build();
                dummyValue = 123;
            } else {
                field = FieldSpec.builder(ClassName.get(String.class), key, Modifier.PRIVATE).build();
                dummyValue = "dummy_" + key;
            }

            parentBuilder.addField(field);
            dummyMap.put(key, dummyValue);
        }

        for (TypeSpec nested : nestedClasses.values()) {
            parentBuilder.addType(nested.toBuilder().addModifiers(Modifier.STATIC).build());
        }

        TypeSpec finalClass = parentBuilder.build();

        String jsonOutput = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dummyMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DTOWithJson(finalClass, jsonOutput);
    }

    private static String toPascal(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static Map<String, Object> generateDummyJsonMap(Map<String, Object> map) {
        Map<String, Object> dummyMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map mapVal) {
                dummyMap.put(key, generateDummyJsonMap(mapVal));
            } else if (value instanceof List list && !list.isEmpty() && list.get(0) instanceof Map) {
                dummyMap.put(key, List.of(generateDummyJsonMap((Map<String, Object>) list.get(0))));
            } else if (value instanceof Integer) {
                dummyMap.put(key, 123);
            } else {
                dummyMap.put(key, "dummy_" + key);
            }
        }
        return dummyMap;
    }
}

class DTOWithJson {
    private final TypeSpec typeSpec;
    private final String json;

    public DTOWithJson(TypeSpec typeSpec, String json) {
        this.typeSpec = typeSpec;
        this.json = json;
    }

    public TypeSpec getTypeSpec() {
        return typeSpec;
    }

    public String getJson() {
        return json;
    }
}
