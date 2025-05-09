package com.npst.spring_boot_project_generator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderUtil {

    public static Class<?> getClassType(String javaFilePath, boolean loadMavenProject) {
        // Step 1: Optionally load the project using Maven compiler
        if (loadMavenProject && !loadProjectWithMavenCompiler(javaFilePath)) {
            return null; // Return null if project cannot be loaded and the flag is set to true
        }

        try {
            // Step 2: Extract the project directory and class name from the file path
            String[] pathParts = javaFilePath.split("(?=/src)", 2);
            String projectDir = pathParts[0];
            String className = extractClassName(pathParts[1]);

            // Step 3: Construct path to the compiled class directory
            String compiledClassPath = projectDir + "/target/classes";

            // Step 4: Load the class from the compiled classes directory
            URLClassLoader classLoader = createClassLoader(compiledClassPath);
            Class<?> loadedClass = classLoader.loadClass(className);
            classLoader.close();  // Close the class loader

            return loadedClass;  // Return the loaded class

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static String extractClassName(String relativePath) {
        return relativePath.replace("/src/main/java/", "")
                .replace("//", "/")
                .replace("/", ".")
                .replace(".java", "");
    }


    private static URLClassLoader createClassLoader(String compiledClassPath) throws Exception {
        File classDir = new File(compiledClassPath);
        URL classUrl = classDir.toURI().toURL();
        return new URLClassLoader(new URL[]{classUrl});
    }


    private static boolean loadProjectWithMavenCompiler(String javaFilePath) {
        String[] parts = javaFilePath.split("(?=/src)", 2);
        String projectPathDir = parts[0];

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "compile");
            processBuilder.directory(new File(projectPathDir)); // Set project directory
            processBuilder.redirectErrorStream(true); // Merge error stream with output
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    //System.out.println(line);
                }
            }

            // Wait for process to finish and get exit code
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return true;
            } else {
                System.out.println("❌ Compilation failed. Check the output for errors.");
                return false;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
