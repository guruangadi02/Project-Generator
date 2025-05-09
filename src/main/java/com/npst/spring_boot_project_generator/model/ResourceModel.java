package com.npst.spring_boot_project_generator.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResourceModel {

    private String resource;

    private List<Request> requests;

    private ApiRequestModel apiRequestModel;

    private ApiResponseModel apiResponseModel;

    @Getter
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }

    public Object getAdditionalProperty(String key){
        return additionalProperties.get(key);
    }
}
