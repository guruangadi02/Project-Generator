package com.npst.spring_boot_project_generator.model;

import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ApiResponseModel {

    public static final String MAIN_APPLICATION = "main_application";
    public static final String POM = "pom";
    public static final String PROPERTIES = "properties";
    public static final String CONFIGURATION = "configuration";
    public static final String UTILITY = "utility";
    public static final String EXCEPTION = "exception";
    public static final String ENTITY = "entity";
    public static final String DTO = "dto";
    public static final String REPOSITORY = "repository";
    public static final String SERVICE = "service";
    public static final String CONTROLLER = "controller";
    public static final String TEST = "test";

    private String projectName;

    private String projectPath;

    //private Map<String, ArrayList<String>> files_generated = new HashMap<>();;
    private Map<String, Object> files_generated = new HashMap<>();;

    public void setFileGeneratedProperty(String key, Object value){
        files_generated.put(key, value);
    }

    public ApiResponseModel(){
        setUp();
    }

    public void setUp(){
        setFileGeneratedProperty(MAIN_APPLICATION, new ArrayList<>());
        setFileGeneratedProperty(POM, new ArrayList<>());
        setFileGeneratedProperty(PROPERTIES, new ArrayList<>());
        setFileGeneratedProperty(CONFIGURATION, new ArrayList<>());
        setFileGeneratedProperty(UTILITY, new ArrayList<>());
        setFileGeneratedProperty(EXCEPTION, new ArrayList<>());
        setFileGeneratedProperty(ENTITY, new ArrayList<>());

        Map<String, List<String>> dtoMap = new HashMap<>();
        dtoMap.put("request", new ArrayList<>());
        dtoMap.put("response", new ArrayList<>());
        setFileGeneratedProperty(DTO, dtoMap);

        setFileGeneratedProperty(REPOSITORY, new ArrayList<>());
        setFileGeneratedProperty(SERVICE, new ArrayList<>());
        setFileGeneratedProperty(CONTROLLER, new ArrayList<>());
        setFileGeneratedProperty(TEST, new ArrayList<>());
    }
}
