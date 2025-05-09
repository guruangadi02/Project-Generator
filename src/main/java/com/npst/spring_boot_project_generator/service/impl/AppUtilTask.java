package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiResponseModel;
import com.npst.spring_boot_project_generator.model.ModelFactory;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.service.Task;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AppUtilTask implements Task {

    @Autowired
    private ModelFactory modelFactory;

    @Override
    public void execute(ResourceModel resourceModel) {

        log.info("✅ Creating Utility Layer Files.");

        final String name = AppConstants.UTILITY_FOLDER;
        List<String> files = Arrays.asList("AppUtility");

        files.forEach(file -> {

            final String CLASS_NAME_UTILITY = file.toString();
            final String BASE_PACKAGE = Utility.getBasePackage(resourceModel.getApiRequestModel().getGroupId(),
                    resourceModel.getApiRequestModel().getArtifactId());
            final String BASE_PACKAGE_UTILITY = BASE_PACKAGE + "." + name;
            final String PROJECT_PATH = Utility.getProjectBasePath(resourceModel.getApiRequestModel().getProjectName(),
                    AppConstants.BASE_JAVA_PATH);

            String getMockResponseDataForPostUsers = "getMockResponseDataForPostUsers";

            MethodSpec sampleMethodPostUsers = MethodSpec.methodBuilder(getMockResponseDataForPostUsers)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return $S", Utility.getMockResponseDataForPostUsers())
                    .build();

            // Define the Util class
            TypeSpec utilClass = TypeSpec.classBuilder(CLASS_NAME_UTILITY)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(sampleMethodPostUsers)
                    .build();

            // Create the Java file
            JavaFile javaFile = JavaFile.builder(BASE_PACKAGE_UTILITY, utilClass).build();

            // Write the generated file to the correct directory
            try {
                javaFile.writeTo(Paths.get(PROJECT_PATH));
            } catch (IOException e) {
                log.error("❌ Failed to create the App config file {} ", file.toString(), e);
            }

            String utilityFilePath = PROJECT_PATH +
                    BASE_PACKAGE_UTILITY.replace(".", "/") + "/" +
                    CLASS_NAME_UTILITY + ".java";

            resourceModel.setAdditionalProperty(AppConstants.UTILITY_FILE_PATH, utilityFilePath);

            modelFactory.updateApiResponseModel(ApiResponseModel.UTILITY, utilityFilePath);

            log.info("✅ App Utility class generated successfully at: {}", utilityFilePath);

        });
    }
}
