package com.npst.spring_boot_project_generator.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestModel {

    private String projectName;
    private String groupId;
    private String artifactId;
    private String version;
    private String description;
    private String javaVersion;
    private List<Request> requests;

    private Map<String, Object> additionalProperties = new HashMap<>();

    public Map<String, Object> getAdditionalProperties() { return additionalProperties; }

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }

    public Object getAdditionalProperty(String key){
        return additionalProperties.get(key);
    }
}

