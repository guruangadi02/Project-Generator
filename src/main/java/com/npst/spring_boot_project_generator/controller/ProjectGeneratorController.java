package com.npst.spring_boot_project_generator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npst.spring_boot_project_generator.model.*;
import com.npst.spring_boot_project_generator.service.impl.TaskInvoker;
import com.npst.spring_boot_project_generator.utils.AppConstants;
import com.npst.spring_boot_project_generator.utils.Utility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
//@RestController
@Controller
@RequestMapping("/api/v1")
public class ProjectGeneratorController {

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private TaskInvoker taskInvoker;

    @GetMapping("/input")
    public String showForm(Model model){

        ApiRequestModel apiRequestModel = new ApiRequestModel();
        apiRequestModel.setRequests(List.of(new Request()));
        model.addAttribute("project", apiRequestModel);
        return "input-form";

    }


    @PostMapping("/submit")
    public ResponseEntity<Resource> submit_old(HttpServletRequest request, Model model) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ApiRequestModel project = new ApiRequestModel();
        Request reqData = new Request();

        // Set top-level project fields
        project.setProjectName(request.getParameter("projectName"));
        project.setGroupId(request.getParameter("groupId"));
        project.setArtifactId(request.getParameter("artifactId"));
        project.setVersion("1.0.0");
        project.setDescription(request.getParameter("description"));
        project.setJavaVersion("17");

        // Set request fields
        reqData.setType(request.getParameter("requests[0].type"));
        reqData.setHttpMethod(request.getParameter("requests[0].httpMethod"));
        reqData.setPath(request.getParameter("requests[0].path"));

        // Parse and set HTTP Request JSON
        String httpReqJson = request.getParameter("requests[0].httpRequest");
        if (httpReqJson != null && !httpReqJson.isBlank()) {
            JsonNode jsonNode = mapper.readTree(httpReqJson);

            List<Map<String, Object>> parsedReq;

            if (jsonNode.isArray()) {
                // Already an array
                parsedReq = mapper.readValue(httpReqJson, new TypeReference<>() {});
            } else if (jsonNode.isObject()) {
                // Single object — wrap in a list
                Map<String, Object> singleReq = mapper.convertValue(jsonNode, new TypeReference<>() {});
                parsedReq = List.of(singleReq);
            } else {
                throw new IllegalArgumentException("Invalid httpRequest JSON format");
            }

            reqData.setHttpRequest(parsedReq);
        }

        // Parse and set HTTP Response JSON
        String httpRespJson = request.getParameter("requests[0].httpResponse");
        if (httpRespJson != null && !httpRespJson.isBlank()) {
            JsonNode jsonNode = mapper.readTree(httpRespJson);

            List<Map<String, Object>> parsedResp;

            if (jsonNode.isArray()) {
                // JSON is an array: parse directly
                parsedResp = mapper.readValue(httpRespJson, new TypeReference<>() {});
            } else if (jsonNode.isObject()) {
                // JSON is a single object: wrap in a list
                Map<String, Object> singleResp = mapper.convertValue(jsonNode, new TypeReference<>() {});
                parsedResp = List.of(singleResp);
            } else {
                throw new IllegalArgumentException("Invalid httpResponse JSON format");
            }

            reqData.setHttpResponse(parsedResp);
        }

        // Set the request inside the project
        project.setRequests(List.of(reqData));

        ApiRequestModel apiRequestModel = project;
        ApiResponseModel apiResponseModel = modelFactory.createApiResponseModel(apiRequestModel, Utility.getProjectPath(apiRequestModel.getProjectName()));
        ResourceModel resourceModel = modelFactory.createResourceModel(apiRequestModel, apiResponseModel);

        //Invoke the tasks using TaskInvoker.
        CompletableFuture<Void> taskFuture = taskInvoker.invokeTasks(resourceModel);

        taskFuture.join(); // Wait for the tasks to complete.

        // 2. Zip the project folder
        Path zipPath = Files.createTempFile("spring-boot-project", ".zip");
        Utility.zipFolder(Path.of(resourceModel.getAdditionalProperty(AppConstants.PROJECT_PATH_KEY).toString()), zipPath);


        // 3. Create resource from a zip file
        Resource resource = new UrlResource(zipPath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"spring-boot-project.zip\"")
                .body(resource);

    }




    @PostMapping("/create")
    public ResponseEntity<Object> handleRequest(@RequestBody JsonNode jsonNode) throws IOException {
        // Use ModelFactory to create and ApiRequestModel, ApiResponseModel & ResourceModel
        ApiRequestModel apiRequestModel = modelFactory.createApiRequestModel(jsonNode);
        ApiResponseModel apiResponseModel = modelFactory.createApiResponseModel(apiRequestModel, Utility.getProjectPath(apiRequestModel.getProjectName()));
        ResourceModel resourceModel = modelFactory.createResourceModel(apiRequestModel, apiResponseModel);

        //Invoke the tasks using TaskInvoker.
        CompletableFuture<Void> taskFuture = taskInvoker.invokeTasks(resourceModel);

        taskFuture.join(); // Wait for the tasks to complete.

        // 2. Zip the project folder
        Path zipPath = Files.createTempFile("spring-boot-project", ".zip");
        Utility.zipFolder(Path.of(resourceModel.getAdditionalProperty(AppConstants.PROJECT_PATH_KEY).toString()), zipPath);

        return ResponseEntity.ok(modelFactory.getApiResponseModel());
    }


}
