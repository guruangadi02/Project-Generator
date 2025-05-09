package com.npst.spring_boot_project_generator.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/init")
public class ProjectInitializerController {


    @GetMapping("/error")
    public String triggerError() {
        throw new RuntimeException("This is a test exception!");
    }
    
    @GetMapping("/download")
    public ResponseEntity<Resource> generateAndDownloadProject() throws IOException {

        // 1. Generate the project dynamically (e.g. folder structure, pom.xml, etc.)
        Path projectDir = Files.createTempDirectory("spring-boot-project");
        generateSampleProject(projectDir); // Custom method to build the project structure

        // 2. Zip the project folder
        Path zipPath = Files.createTempFile("spring-boot-project", ".zip");
        zipFolder(projectDir, zipPath);

        // 3. Create resource from zip file
        Resource resource = new UrlResource(zipPath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"spring-boot-project.zip\"")
                .body(resource);
    }

    private void generateSampleProject(Path root) throws IOException {
        // Create a basic folder structure (like spring-boot initializer)
        Path srcMainJava = Files.createDirectories(root.resolve("src/main/java/com/example/demo"));
        Path appClass = srcMainJava.resolve("DemoApplication.java");

        String code = """
            package com.example.demo;

            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;

            @SpringBootApplication
            public class DemoApplication {
                public static void main(String[] args) {
                    SpringApplication.run(DemoApplication.class, args);
                }
            }
            """;

        Files.writeString(appClass, code);

        // Add basic pom.xml
        String pomXml = """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """;

        Files.writeString(root.resolve("pom.xml"), pomXml);
    }

    private void zipFolder(Path sourceDir, Path zipFilePath) throws IOException {
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
