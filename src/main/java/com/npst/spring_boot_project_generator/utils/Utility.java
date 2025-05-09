package com.npst.spring_boot_project_generator.utils;

import com.npst.spring_boot_project_generator.model.Request;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utility {

    public static String getProjectPath(String projectName){
        return System.getProperty("user.dir") + "/" +
                projectName;
    }

    public static String getProjectBasePath(String projectName, String baseJavaPath){
        return System.getProperty("user.dir") + "/" +
                projectName + "/" +
                baseJavaPath;
    }

    public static String getPomPath(String projectName){
        return System.getProperty("user.dir") + "/" +
                projectName + "/" +
                "pom.xml";
    }

    public static String getResourcePath(String projectName, String baseResourcePath){
        return System.getProperty("user.dir") + "/" +
                projectName + "/" +
                baseResourcePath;
    }

    public static String formatArtifactId(String artifactId){
        String result = artifactId.replaceAll("[^a-zA-Z0-9]", "_");
        result = result.replaceAll("_+$", "");
        result = result.toLowerCase();
        return result;
    }

    public static String getBasePackage(String groupId, String artifactId){
        return groupId + "." + formatArtifactId(artifactId);
    }

    public static String getMainApplicationClassName(String artifactId){
        String[] words = artifactId.split("[^a-zA-Z0-9]+");
        String class_name = Arrays.stream(words)
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining()) + "Application";
        return class_name;
    }

    public static String splitApiByEndPoint(String url){
        try {
            // Extract domain
            String domain = url.replaceFirst("^(https?://)", "").split("/")[0];

            // Extract path (remove domain)
            String endPoint = url.replaceFirst("^(https?://[^/]+)", "").split("\\?")[0]; // Remove query params

            return endPoint;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return as is for null or empty string
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String getMethodNameByHttpMethod(String httpMethod){
        return switch (httpMethod.toUpperCase()) {
            case "POST" -> "create";
            case "GET" -> "get";
            case "PUT" -> "update";
            case "DELETE" -> "delete";
            default -> "unknownMethod";
        };
    }

    public static String extractResource(String url) {
        try {
            // Extract the path after the domain
            String path = url.replaceFirst("^(https?://[^/]+)", "").split("\\?")[0]; // Remove query parameters

            // Split by "/" to get path segments
            String[] pathSegments = path.split("/");


            //URI uri = new URI(url);
            //String[] pathSegments = uri.getPath().split("/");

            // Traverse from right to left, skipping numeric segments
            for (int i = pathSegments.length - 1; i >= 0; i--) {
                String segment = pathSegments[i];

                // Skip placeholders inside { } brackets dynamically
                if (segment.startsWith("{") && segment.endsWith("}")) {
                    continue;
                }

                // Ignore empty segments and numeric IDs
                if (!segment.isEmpty() && !segment.matches("\\d+")) {
                    return segment; // First non-numeric segment is the resource
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    public static String lowercaseFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return as is for null or empty strings
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static boolean isPostOrPut(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method);
    }

    public static boolean isPostOrGet(String method) {
        return "POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method);
    }

    public static Optional<Map<String, Object>> getFirstHttpRequest(Request request) {
        return Optional.ofNullable(request.getHttpRequest())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public static Optional<Map<String, Object>> getFirstHttpResponse(Request request) {
        return Optional.ofNullable(request.getHttpResponse())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public static Class<?> identifyType(Object value) {
        return (value != null) ? value.getClass() : Objects.class;
    }

    public static String[] packageSplitter(String filePath, String projectPath){

        String fullClassName = filePath.replace(projectPath, "")
                                .replace("/", ".")
                                .replace(".java", "");

        var lastDotIndex = fullClassName.lastIndexOf(".");
        var result = lastDotIndex != -1
                ? new String[]{fullClassName.substring(0, lastDotIndex), fullClassName.substring(lastDotIndex + 1)}
                : new String[]{"Invalid class name format", ""};

        return result;
    }

    public static String getMockResponseDataForPostUsers(){
        return """
        {
                "id": 1,
                "name": "John Doe",
                "email": "johndoe@example.com",
                "age": 30
        }
        """;
    }

    public static final Map<String, Class<?>> HTTP_METHOD_TO_MAPPING = Map.of(
            "GET", GetMapping.class,
            "POST", PostMapping.class,
            "PUT", PutMapping.class,
            "DELETE", DeleteMapping.class
    );

    public static Class<?> getMappingClass(String httpMethod) {
        return HTTP_METHOD_TO_MAPPING.getOrDefault(httpMethod.toUpperCase(), null);
    }

    public static void zipFolder(Path sourceDir, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString().replace("\\", "/"));
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
