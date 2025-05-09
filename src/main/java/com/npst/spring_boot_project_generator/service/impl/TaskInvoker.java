package com.npst.spring_boot_project_generator.service.impl;

import com.npst.spring_boot_project_generator.model.ApiRequestModel;
import com.npst.spring_boot_project_generator.model.Request;
import com.npst.spring_boot_project_generator.model.ResourceModel;
import com.npst.spring_boot_project_generator.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TaskInvoker {

    @Autowired
    private MainApplicationTask mainApplicationTask;

    @Autowired
    private PomTask pomTask;

    @Autowired
    private AppConfigTask appConfigTask;

    @Autowired
    private  AppUtilTask appUtilTask;

    @Autowired
    private  ControllerTask controllerTask;

    @Autowired
    private DTOResponseTask dtoResponseTask;

    @Autowired
    private DTORequestTask dtoRequestTask;

    @Autowired
    private  EntityTask entityTask;

    @Autowired
    private ExceptionTask exceptionTask;

    @Autowired
    private PropertiesTask propertiesTask;

    @Autowired
    private RepositoryTask repositoryTask;

    @Autowired
    private ServiceTask serviceTask;

    @Autowired
    private TestTask testTask;

    public CompletableFuture<Void> invokeTasks(ResourceModel resourceModel){

        log.info("✅ ============================== Invoking tasks ====================================");

        CompletableFuture<Void> mainCompletableFuture =
                CompletableFuture.runAsync(() -> createMainApplication(resourceModel));

        CompletableFuture<Void> pomCompletableFuture =
                CompletableFuture.runAsync(() -> createPomXml(resourceModel));

        CompletableFuture<Void> propertiesCompletableFuture =
                CompletableFuture.runAsync(() -> createPropertiesLayer(resourceModel));

        CompletableFuture<Void> configCompletableFuture =
                CompletableFuture.runAsync(() -> createAppConfigLayer(resourceModel));

        CompletableFuture<Void> utilCompletableFuture =
                CompletableFuture.runAsync(() -> createUtilityLayer(resourceModel));

        CompletableFuture<Void> exceptionCompletableFuture =
                CompletableFuture.runAsync(() -> createExceptionLayer(resourceModel));


        Map<String, List<Request>> groupedRequestsByResources = getGroupedRequestsByResources(resourceModel.getApiRequestModel());

        groupedRequestsByResources.forEach((resource, requests) -> {

            log.info("✅ Processing the request: {}", requests);
            resourceModel.setResource(resource);
            resourceModel.setRequests(requests);

            processTemplateLayers(resourceModel);
        });


        CompletableFuture.allOf(
                mainCompletableFuture,
                pomCompletableFuture,
                propertiesCompletableFuture,
                configCompletableFuture,
                utilCompletableFuture,
                exceptionCompletableFuture).join();

        log.info("✅ ===================================== Tasks Completed =======================================");

        return CompletableFuture.completedFuture(null);
    }

    public void createMainApplication(final ResourceModel resourceModel){
        try {
            mainApplicationTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while Creating Main Application !! ", e);
        }
    }

    public void createPomXml(final ResourceModel resourceModel){
        try {
            pomTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while Creating POM XML !! ", e);
        }
    }

    public void createPropertiesLayer(final ResourceModel resourceModel){
        try {
            propertiesTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while Creating Properties File !! ", e);
        }
    }

    public void createAppConfigLayer(final ResourceModel resourceModel){
        try {
            appConfigTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while Creating App Config Layer !! ", e);
        }
    }

    public void createUtilityLayer(final ResourceModel resourceModel){
        try {
            appUtilTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while Creating Utility Layer !! ", e);
        }
    }

    public void createExceptionLayer(final ResourceModel resourceModel){
        try {
            exceptionTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating Exception Layer !! ", e);
        }
    }

    public void processTemplateLayers(final ResourceModel resourceModel){
        createEntityLayer(resourceModel);
        createDTOLayer(resourceModel);
        createRepositoryLayer(resourceModel);
        createServiceLayer(resourceModel);
        createControllerLayer(resourceModel);
        createTestLayer(resourceModel);
    }

    private void createTestLayer(final ResourceModel resourceModel){
        try {
            testTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating Test Layer !! ", e);
        }
    }

    private void createControllerLayer(final ResourceModel resourceModel){
        try {
            controllerTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating Controller Layer !! ", e);
        }
    }

    private void createServiceLayer(final ResourceModel resourceModel){
        try {
            serviceTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating Service Layer !! ", e);
        }
    }

    private void createEntityLayer(final ResourceModel resourceModel){
        try {
            entityTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating Entity Layer !! ", e);
        }
    }

    private void createDTOLayer(final ResourceModel resourceModel){
        try {
            dtoRequestTask.execute(resourceModel);
            dtoResponseTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating DTO Layer !! ", e);
        }
    }

    private void createRepositoryLayer(final ResourceModel resourceModel){
        try {
            repositoryTask.execute(resourceModel);
        } catch (Exception e){
            log.error("❌ Exception occurred while creating Repository Layer !! ", e);
        }
    }

    private Map<String, List<Request>> getGroupedRequestsByResources(final ApiRequestModel apiRequestModel){

        Map<String, List<Request>> groupedRequests = new HashMap<>();
        try {
            List<Request> requests = apiRequestModel.getRequests();
            requests.forEach(request -> {

                String path = request.getPath();
                String resource = Utility.extractResource(path);

                // Store request under the respective resource
                groupedRequests.computeIfAbsent(resource, k -> new ArrayList<>()).add(request);

            });

        } catch (Exception e) {
            log.error("❌ Failed to get getGroupedRequestsByResources: ", e);
        }

        log.info("✅ groupedRequests :{}", groupedRequests);
        return groupedRequests;
    }

}
