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
    
    public static void main(String[] args) throws LifecycleException, IOException {
        // Get port from environment variable (Railway provides PORT)
        String portEnv = System.getenv("PORT");
        int port = portEnv != null ? Integer.parseInt(portEnv) : DEFAULT_PORT;
        
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setHostname("0.0.0.0");
        
        // Get the base directory
        String baseDir = System.getProperty("java.io.tmpdir") + "/tomcat." + port;
        new File(baseDir).mkdirs();
        tomcat.setBaseDir(baseDir);
        
        // Get the WAR file location
        String warPath = findWarFile();
        if (warPath == null) {
            System.err.println("ERROR: Could not find WAR file!");
            System.err.println("Current directory: " + System.getProperty("user.dir"));
            System.exit(1);
        }
        
        System.out.println("Found WAR file at: " + warPath);
        
        // Extract WAR to a temporary directory for deployment
        String webappDir = baseDir + "/webapp";
        extractWar(warPath, webappDir);
        
        // Deploy the extracted webapp
        String contextPath = "";
        tomcat.addWebapp(contextPath, webappDir);
        
        // Start the server
        tomcat.start();
        System.out.println("Tomcat started on port " + port);
        System.out.println("Application available at http://0.0.0.0:" + port);
        
        // Keep the server running
        tomcat.getServer().await();
    }
    
    private static String findWarFile() {
        // Try multiple locations where the WAR might be
        String[] possiblePaths = {
            "target/sendGridBasicRailway.war",
            "sendGridBasicRailway.war",
            "../target/sendGridBasicRailway.war",
            System.getProperty("user.dir") + "/target/sendGridBasicRailway.war"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return file.getAbsolutePath();
            }
        }
        
        // If WAR not found, try to find any WAR file in target directory
        File targetDir = new File("target");
        if (targetDir.exists() && targetDir.isDirectory()) {
            File[] files = targetDir.listFiles((dir, name) -> name.endsWith(".war"));
            if (files != null && files.length > 0) {
                return files[0].getAbsolutePath();
            }
        }
        
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

