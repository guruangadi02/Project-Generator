package com.npst.spring_boot_project_generator.service.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.Request;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.ClassLoaderUtil;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ServiceTask implements Task {

    @Autowired
    ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Service Layer Files...");

        final String name = AppConstants.SERVICE_FOLDER;
        final String file = Utility.capitalize(resourceModel.getResource()) + "Service";
        final String repositoryFile = Utility.capitalize(resourceModel.getResource()) + "Repository";


        final String CLASS_NAME_SERVICE = file.toString();
        final String BASE_PACKAGE = resourceModel.getAdditionalProperty(AppConstants.BASE_PACKAGE_KEY).toString();
        final String BASE_PACKAGE_SERVICE = BASE_PACKAGE + "." + name;
        final String PROJECT_PATH = resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString();

        // Define the @Service annotation
        AnnotationSpec serviceAnnotation = AnnotationSpec.builder(Service.class).build();

        // Define the Repository field with @Autowired
        ClassName repoclassType = ClassName.get(BASE_PACKAGE + "." + AppConstants.REPOSITORY_FOLDER, repositoryFile);

        FieldSpec repositoryField = FieldSpec.builder(
                        Objects.requireNonNull(repoclassType),
                        Utility.lowercaseFirstChar(repositoryFile))
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(Autowired.class) // Add @Autowired annotation
                .build();

        // Define the service class
        TypeSpec.Builder serviceClassBuilder = TypeSpec.classBuilder(CLASS_NAME_SERVICE)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(serviceAnnotation)
                .addField(repositoryField);

        List<Request> requests = resourceModel.getRequests();

        // Get DTO Class Type
        String[] stringsResponse = Utility.packageSplitter(resourceModel.getAdditionalProperty(AppConstants.DTO_RESPONSE_PATH).toString(), PROJECT_PATH);
        String[] stringsRequest = Utility.packageSplitter(resourceModel.getAdditionalProperty(AppConstants.DTO_REQUEST_PATH).toString(), PROJECT_PATH);

        ClassName dtoResponseClass = ClassName.get(stringsResponse[0], stringsResponse[1]);
        ClassName dtoRequestClass = ClassName.get(stringsRequest[0], stringsRequest[1]);

        requests.forEach(request -> {
            final String httpMethod = request.getHttpMethod();
            final String path = request.getPath();
            String endPoint = Utility.splitApiByEndPoint(path);
            String queryParam = "";
            if (endPoint.contains("{") || endPoint.contains("}") || endPoint.contains("?")) {
                queryParam = "ByQueryParam";
            }

            String methodName = Utility.getMethodNameByHttpMethod(httpMethod);
            methodName = methodName + Utility.capitalize(resourceModel.getResource()) + queryParam;

            String dtoObjVarName = Utility.lowercaseFirstChar(dtoRequestClass.simpleName());

            String mockData = resourceModel.getAdditionalProperty(resourceModel.getResource()).toString();

            TypeName responseFieldType = dtoResponseClass;
            TypeName requestFieldType = dtoRequestClass;

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("$T data = $S", String.class, mockData)
                    .addStatement("$T objectMapper = new $T()", ObjectMapper.class, ObjectMapper.class)
                    .addStatement("objectMapper.setVisibility($T.FIELD, $T.ANY)", PropertyAccessor.class, JsonAutoDetect.Visibility.class)
                    .addStatement("$T obj = null", dtoResponseClass)
                    .beginControlFlow("try")
                    .addStatement("obj = objectMapper.readValue(data, $T.class)", dtoResponseClass)
                    .nextControlFlow("catch ($T e)", Exception.class)
                    .addStatement("e.printStackTrace()")
                    .endControlFlow()
                    .addStatement("return obj")
                    .returns(responseFieldType);


            if (httpMethod.equalsIgnoreCase("POST") || httpMethod.equalsIgnoreCase("PUT")){
                TypeName listOfDto = ParameterizedTypeName.get(ClassName.get(List.class), requestFieldType);

                methodSpecBuilder.addParameter(ParameterSpec.builder(listOfDto, dtoObjVarName).build());
            } else if (!queryParam.isEmpty()) {
                methodSpecBuilder.addParameter(Integer.class, "id");
            }

            MethodSpec methodSpec = methodSpecBuilder.build();

            serviceClassBuilder.addMethod(methodSpec);
        });

        TypeSpec serviceClass = serviceClassBuilder.build();

        // Create the Java file
        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_SERVICE, serviceClass)
                .build();

        // Write the generated file
        try {
            javaFile.writeTo(Paths.get(PROJECT_PATH));
        } catch (IOException e) {
            log.error("❌ Failed to create the Service file {} ", file.toString(), e);
        }

        String serviceFilePath = PROJECT_PATH+
                BASE_PACKAGE_SERVICE.replace(".", "/")+"/"+
                CLASS_NAME_SERVICE+".java";

        resourceModel.setAdditionalProperty(AppConstants.SERVICE_FILE_PATH, serviceFilePath);

        modelFactory.updateApiResponseModel(ApiResponseModel.SERVICE, serviceFilePath);

        log.info("✅ Spring Boot Service Class generated successfully at: {}", serviceFilePath);
    }
}
