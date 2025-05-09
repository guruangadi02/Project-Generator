package com.npst.spring_boot_project_generator.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ControllerTask implements Task {

    @Autowired
    ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Controller Layer Files...");

        final String name = AppConstants.CONTROLLER_FOLDER;
        final String file = Utility.capitalize(resourceModel.getResource()) + "Controller";
        final String serviceFile = Utility.capitalize(resourceModel.getResource()) + "Service";

        final String CLASS_NAME_CONTROLLER = file.toString();
        final String BASE_PACKAGE = resourceModel.getAdditionalProperty(AppConstants.BASE_PACKAGE_KEY).toString();
        final String BASE_PACKAGE_CONTROLLER = BASE_PACKAGE + "." + name;
        final String PROJECT_PATH = resourceModel.getAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY).toString();

        // Define @RestController annotation
        AnnotationSpec restControllerAnnotation = AnnotationSpec.builder(RestController.class).build();

        // Define @RequestMapping annotation
        AnnotationSpec requestMappingAnnotation = AnnotationSpec.builder(RequestMapping.class)
                .build();

        // Define the Service field with @Autowired
        ClassName serviceClassType = ClassName.get(
                BASE_PACKAGE + "." + AppConstants.SERVICE_FOLDER, serviceFile);

        FieldSpec serviceField = FieldSpec.builder(
                        Objects.requireNonNull(serviceClassType),
                        Utility.lowercaseFirstChar(serviceFile))
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(Autowired.class) // Add @Autowired annotation
                .build();

        TypeSpec.Builder controllerClassBuilder = TypeSpec.classBuilder(CLASS_NAME_CONTROLLER)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(restControllerAnnotation)
                .addAnnotation(requestMappingAnnotation)
                .addField(serviceField);

        List<Request> requests = resourceModel.getRequests();

        //Load the DTO Class.
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


            AnnotationSpec.Builder methodMappingAnnotationBuilder = AnnotationSpec.builder(Utility.getMappingClass(httpMethod));
            methodMappingAnnotationBuilder.addMember("value", "$S", endPoint);
            AnnotationSpec methodMappingAnnotation = methodMappingAnnotationBuilder.build();

            String dtoObjVarName = Utility.lowercaseFirstChar(dtoRequestClass.simpleName());

            TypeName responseFieldType = dtoResponseClass;
            TypeName requestFieldType = dtoRequestClass;

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(methodMappingAnnotation)
                    .returns(ParameterizedTypeName.get(
                            ClassName.get(ResponseEntity.class),
                            responseFieldType));

            if(httpMethod.equalsIgnoreCase("POST") || httpMethod.equalsIgnoreCase("PUT")){
                /*
                TypeName listOfDto = ParameterizedTypeName.get(ClassName.get(List.class), requestFieldType);

                methodSpecBuilder.addParameter(ParameterSpec.builder(listOfDto, dtoObjVarName)
                        .addAnnotation(RequestBody.class)
                        .build());
                methodSpecBuilder.addStatement("$T obj = $N.$N($N)", dtoResponseClass, serviceField.name, methodName, dtoObjVarName);

                 */

                methodSpecBuilder.addParameter(ParameterSpec.builder(
                                ClassName.get(JsonNode.class), "body")
                                .addAnnotation(ClassName.get("org.springframework.web.bind.annotation", "RequestBody"))
                                .build());

                methodSpecBuilder.addException(JsonProcessingException.class);
                methodSpecBuilder.addStatement("$T objectMapper = new $T()", ObjectMapper.class, ObjectMapper.class);

                methodSpecBuilder.addStatement("$T<$T> $L = new $T<>()",
                        List.class,
                        dtoRequestClass,
                        dtoObjVarName,
                        ArrayList.class);

                methodSpecBuilder.beginControlFlow("if (body.isArray())");
                methodSpecBuilder.addStatement("$L = objectMapper.readValue(body.toString(), new $T<$T<$T>>() {})",
                        dtoObjVarName,
                        TypeReference.class,
                        ClassName.get(List.class),
                        dtoRequestClass);
                methodSpecBuilder.nextControlFlow("else if (body.isObject())");
                methodSpecBuilder.addStatement("$T dtoObj = objectMapper.readValue(body.toString(), $T.class)",
                        dtoRequestClass, dtoRequestClass);
                methodSpecBuilder.addStatement("$L.add(dtoObj)", dtoObjVarName);
                methodSpecBuilder.endControlFlow();
                methodSpecBuilder.addStatement("$T obj = $N.$N($N)", dtoResponseClass, serviceField.name, methodName, dtoObjVarName);


            } else if (!queryParam.isEmpty()) {
                methodSpecBuilder.addParameter(
                        ParameterSpec.builder(Integer.class, "id")
                                .addAnnotation(PathVariable.class)
                                .build());
                methodSpecBuilder.addStatement("$T obj = $N.$N($N)", dtoResponseClass, serviceField.name, methodName, "id");
            }else {
                methodSpecBuilder.addStatement("$T obj = $N.$N()", dtoResponseClass, serviceField.name, methodName);
            }

            methodSpecBuilder.addStatement("return $T.ok(obj)", ResponseEntity.class);

            MethodSpec methodSpec = methodSpecBuilder.build();

            controllerClassBuilder.addMethod(methodSpec);
        });

        TypeSpec controllerClass = controllerClassBuilder.build();

        // Create the Java file
        JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_CONTROLLER, controllerClass).build();

        // Write the generated file to the correct directory
        try {
            javaFile.writeTo(Paths.get(PROJECT_PATH));
        } catch (IOException e) {
            log.error("❌ Failed to create the Controller file {} ", file.toString(), e);
        }

        String controllerFilePath = PROJECT_PATH +
                BASE_PACKAGE_CONTROLLER.replace(".", "/") +"/"+
                CLASS_NAME_CONTROLLER +".java";

        resourceModel.setAdditionalProperty(AppConstants.CONTROLLER_FILE_PATH, controllerFilePath);

        modelFactory.updateApiResponseModel(ApiResponseModel.CONTROLLER, controllerFilePath);

        log.info("✅ Spring Boot Controller File generated successfully at: {}", controllerFilePath);
    }
}
