package com.npst.spring_boot_project_generator.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Transient;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String type;
    private String httpMethod;
    private String path;

    private List<Map<String, Object>> httpRequest;
    private List<Map<String, Object>> httpResponse;

}
