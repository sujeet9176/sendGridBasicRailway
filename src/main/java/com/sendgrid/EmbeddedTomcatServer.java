package com.sendgrid;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class EmbeddedTomcatServer {
    
    private static final int DEFAULT_PORT = 8080;
    
    public static void main(String[] args) {
        try {
            // Get port from environment variable (Railway provides PORT)
            String portEnv = System.getenv("PORT");
            int port = DEFAULT_PORT;
            if (portEnv != null && !portEnv.trim().isEmpty()) {
                try {
                    port = Integer.parseInt(portEnv.trim());
                } catch (NumberFormatException e) {
                    System.err.println("WARNING: Invalid PORT environment variable: " + portEnv + ". Using default: " + DEFAULT_PORT);
                }
            } else {
                System.out.println("INFO: PORT environment variable not set. Using default: " + DEFAULT_PORT);
            }
            
            System.out.println("Starting Embedded Tomcat Server on port: " + port);
            
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(port);
            tomcat.setHostname("0.0.0.0");
            
            // Get the base directory
            String baseDir = System.getProperty("java.io.tmpdir") + "/tomcat." + port;
            File baseDirFile = new File(baseDir);
            baseDirFile.mkdirs();
            if (!baseDirFile.exists() || !baseDirFile.isDirectory()) {
                throw new IOException("Failed to create base directory: " + baseDir);
            }
            tomcat.setBaseDir(baseDir);
            System.out.println("Tomcat base directory: " + baseDir);
            
            // Get the WAR file location
            String warPath = findWarFile();
            if (warPath == null) {
                System.err.println("ERROR: Could not find WAR file!");
                System.err.println("Current directory: " + System.getProperty("user.dir"));
                System.err.println("Looking for: sendGridBasicRailway.war");
                System.exit(1);
            }
            
            System.out.println("Found WAR file at: " + warPath);
            
            // Extract WAR to a temporary directory for deployment
            String webappDir = baseDir + "/webapp";
            System.out.println("Extracting WAR to: " + webappDir);
            extractWar(warPath, webappDir);
            
            // Verify extraction
            File webappDirFile = new File(webappDir);
            if (!webappDirFile.exists() || !webappDirFile.isDirectory()) {
                throw new IOException("Failed to extract WAR to: " + webappDir);
            }
            System.out.println("WAR extracted successfully");
            
            // Deploy the extracted webapp
            String contextPath = "";
            tomcat.addWebapp(contextPath, webappDir);
            System.out.println("Webapp deployed at context path: " + (contextPath.isEmpty() ? "/" : contextPath));
            
            // Start the server
            System.out.println("Starting Tomcat server...");
            tomcat.start();
            System.out.println("Tomcat started successfully on port " + port);
            System.out.println("Application available at http://0.0.0.0:" + port);
            System.out.println("Server is ready to accept connections");
            
            // Keep the server running
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            System.err.println("ERROR: Failed to start Tomcat server");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("ERROR: IO error during server startup");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error during server startup");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String findWarFile() {
        // First, check if WAR path is provided as system property
        String warPathProperty = System.getProperty("war.file.path");
        if (warPathProperty != null && !warPathProperty.trim().isEmpty()) {
            File warFile = new File(warPathProperty);
            if (warFile.exists() && warFile.isFile()) {
                System.out.println("Found WAR file from system property: " + warFile.getAbsolutePath());
                return warFile.getAbsolutePath();
            } else {
                System.out.println("WARNING: WAR file from system property does not exist: " + warPathProperty);
            }
        }
        
        String userDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + userDir);
        
        // Try multiple locations where the WAR might be
        String[] possiblePaths = {
            "target/sendGridBasicRailway.war",
            userDir + "/target/sendGridBasicRailway.war",
            "sendGridBasicRailway.war",
            userDir + "/sendGridBasicRailway.war",
            "../target/sendGridBasicRailway.war"
        };
        
        System.out.println("Searching for WAR file...");
        for (String path : possiblePaths) {
            File file = new File(path);
            System.out.println("  Checking: " + path + " -> " + file.getAbsolutePath() + " (exists: " + file.exists() + ")");
            if (file.exists() && file.isFile()) {
                System.out.println("  Found WAR file at: " + file.getAbsolutePath());
                return file.getAbsolutePath();
            }
        }
        
        // If WAR not found, try to find any WAR file in target directory
        System.out.println("WAR not found in standard locations, searching target directory...");
        File targetDir = new File("target");
        if (!targetDir.exists()) {
            targetDir = new File(userDir + "/target");
        }
        
        if (targetDir.exists() && targetDir.isDirectory()) {
            System.out.println("  Target directory exists: " + targetDir.getAbsolutePath());
            File[] files = targetDir.listFiles((dir, name) -> name.endsWith(".war"));
            if (files != null && files.length > 0) {
                System.out.println("  Found WAR file: " + files[0].getAbsolutePath());
                return files[0].getAbsolutePath();
            } else {
                System.out.println("  No WAR files found in target directory");
            }
        } else {
            System.out.println("  Target directory does not exist: " + targetDir.getAbsolutePath());
        }
        
        System.out.println("ERROR: Could not find WAR file in any location");
        return null;
    }
    
    private static void extractWar(String warPath, String destDir) throws IOException {
        File dest = new File(destDir);
        if (dest.exists()) {
            deleteDirectory(dest);
        }
        dest.mkdirs();
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(warPath))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                File file = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                entry = zis.getNextEntry();
            }
        }
        System.out.println("WAR extracted to: " + destDir);
    }
    
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}

