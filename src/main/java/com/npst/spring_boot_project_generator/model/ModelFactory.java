package com.npst.spring_boot_project_generator.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ModelFactory {

    private static ApiResponseModel apiResponseModel = new ApiResponseModel();

    private static ApiRequestModel apiRequestModel = null;

    public ApiRequestModel createApiRequestModel(JsonNode jsonNode){
        final ObjectMapper objectMapper = new ObjectMapper();

        try {
            log.info("✅ handleRequest - Received Data: {}", jsonNode);
            apiRequestModel = objectMapper.treeToValue(jsonNode, ApiRequestModel.class);
            log.info("✅ ApiRequestModel: {}", apiRequestModel);

        } catch (Exception e) {
            log.error("❌ Failed to Map Data to Model: ", e);
        }

        return apiRequestModel;
    }

    public ResourceModel createResourceModel(ApiRequestModel apiRequestModel, ApiResponseModel apiResponseModel) {

        ResourceModel resourceModel = new ResourceModel();
        String basePackage = Utility.getBasePackage(apiRequestModel.getGroupId(), apiRequestModel.getArtifactId());
        String projectBasePath = Utility.getProjectBasePath(apiRequestModel.getProjectName(), AppConstants.BASE_JAVA_PATH);
        String projectPath = Utility.getProjectPath(apiRequestModel.getProjectName());

        resourceModel.setAdditionalProperty(AppConstants.BASE_PACKAGE_KEY, basePackage);
        resourceModel.setAdditionalProperty(AppConstants.PROJECT_BASE_PATH_KEY, projectBasePath);
        resourceModel.setAdditionalProperty(AppConstants.PROJECT_PATH_KEY, projectPath);
        resourceModel.setApiRequestModel(apiRequestModel);
        resourceModel.setApiResponseModel(apiResponseModel);

        return resourceModel;

    }

    public ApiResponseModel createApiResponseModel(ApiRequestModel apiRequestModel, String projectPath) {
        //ApiResponseModel apiResponseModel = new ApiResponseModel();
        apiResponseModel.setProjectName(apiRequestModel.getProjectName());
        apiResponseModel.setProjectPath(projectPath);
        return apiResponseModel;
    }

    public ApiRequestModel getApiRequestModel(){
        return apiRequestModel;
    }

    public ApiResponseModel getApiResponseModel(){
        return apiResponseModel;
    }

    public void updateApiResponseModel(String folder, String filePath){

        if (!folder.equalsIgnoreCase(ApiResponseModel.POM)) {
            String[] parts = filePath.split("(?=/src)", 2);
            filePath = parts[1];
        }

        Map<String, Object> filesGenerated = apiResponseModel.getFiles_generated();
        Object o = filesGenerated.get(folder);
        if (o instanceof ArrayList<?>){
            ArrayList<String> arr = (ArrayList<String>) o;
            arr.add(filePath);
            apiResponseModel.setFileGeneratedProperty(folder, arr);
        }
        if (o instanceof HashMap<?,?>){
            HashMap<String, ArrayList<String>> map = (HashMap<String, ArrayList<String>>)o;
            if(filePath.contains("response"))
            {
                ArrayList<String> arr = map.get("response");
                arr.add(filePath);

                //Map<String, List<String>> dtoMap = new HashMap<>();
                map.put("response", arr);

                apiResponseModel.setFileGeneratedProperty(folder, map);

            } else if (filePath.contains("request"))
            {
                ArrayList<String> arr = map.get("request");
                arr.add(filePath);

                //Map<String, List<String>> dtoMap = new HashMap<>();
                map.put("request", arr);

                apiResponseModel.setFileGeneratedProperty(folder, map);
            }

        }
    }
}
