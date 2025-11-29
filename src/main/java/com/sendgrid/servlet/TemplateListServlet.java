package com.sendgrid.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Template List Servlet - Handles fetching list of SendGrid templates
 */
@WebServlet(name = "TemplateListServlet", urlPatterns = {"/templates"})
public class TemplateListServlet extends HttpServlet {
    
    private static final Logger logger = LogManager.getLogger(TemplateListServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("TemplateListServlet - GET request received");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        // Get API key from properties file or environment variable
        String apiKey = getApiKey();
        
        // Check if API key is set
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("SendGrid API key is not configured");
            jsonResponse.put("success", false);
            jsonResponse.put("error", "SendGrid API key is not configured. Please set it in sendgrid.properties file or SENDGRID_API_KEY environment variable.");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        try {
            // Make direct HTTP call to SendGrid API (same format as Postman)
            String apiUrl = "https://api.sendgrid.com/v3/templates?generations=dynamic";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            
            logger.info("Sending request to SendGrid API: " + apiUrl);
            
            int statusCode = connection.getResponseCode();
            String responseBody;
            
            if (statusCode >= 200 && statusCode < 300) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();
                responseBody = responseBuilder.toString();
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();
                responseBody = responseBuilder.toString();
            }
            
            logger.info("SendGrid API Response - Status Code: " + statusCode);
            
            // Check if the response indicates an error
            if (statusCode >= 400) {
                // Try to parse error message from response body
                String errorMessage = "SendGrid API error";
                try {
                    JSONObject errorJson = new JSONObject(responseBody);
                    if (errorJson.has("errors")) {
                        Object errors = errorJson.get("errors");
                        if (errors instanceof org.json.JSONArray) {
                            org.json.JSONArray errorsArray = (org.json.JSONArray) errors;
                            if (errorsArray.length() > 0) {
                                JSONObject firstError = errorsArray.getJSONObject(0);
                                if (firstError.has("message")) {
                                    errorMessage = firstError.getString("message");
                                } else if (firstError.has("field")) {
                                    errorMessage = "Error in field '" + firstError.getString("field") + "': " + 
                                                  (firstError.has("message") ? firstError.getString("message") : "Invalid value");
                                }
                            }
                        }
                    } else if (errorJson.has("message")) {
                        errorMessage = errorJson.getString("message");
                    }
                } catch (Exception e) {
                    // If parsing fails, use the raw response body
                    if (responseBody != null && !responseBody.isEmpty()) {
                        errorMessage = responseBody;
                    }
                }
                
                jsonResponse.put("success", false);
                jsonResponse.put("error", errorMessage);
                jsonResponse.put("statusCode", statusCode);
                jsonResponse.put("responseBody", responseBody);
            } else {
                // Success response - SendGrid returns a JSON object with "templates" array
                try {
                    JSONObject sendGridResponse = new JSONObject(responseBody);
                    org.json.JSONArray templatesArray = sendGridResponse.getJSONArray("templates");
                    
                    jsonResponse.put("success", true);
                    jsonResponse.put("statusCode", statusCode);
                    jsonResponse.put("data", templatesArray);
                } catch (Exception e) {
                    logger.error("Error parsing SendGrid response: " + e.getMessage());
                    jsonResponse.put("success", false);
                    jsonResponse.put("error", "Error parsing template list: " + e.getMessage());
                }
            }
            
        } catch (IOException ex) {
            logger.error("IOException occurred while calling SendGrid API", ex);
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Error calling SendGrid API: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error occurred", ex);
            jsonResponse.put("success", false);
            jsonResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
        }
        
        out.print(jsonResponse.toString());
        out.flush();
    }
    
    /**
     * Get SendGrid API key from properties file or environment variable
     * @return API key string, or null if not found
     */
    private String getApiKey() {
        // First try to read from properties file
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sendgrid.properties");
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                String apiKey = properties.getProperty("sendgrid.api.key");
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    logger.info("API key loaded from sendgrid.properties file");
                    return apiKey.trim();
                }
                inputStream.close();
            }
        } catch (IOException ex) {
            logger.warn("Could not read sendgrid.properties file: " + ex.getMessage());
        }
        
        // Fall back to environment variable
        String apiKey = System.getenv("SENDGRID_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            logger.info("API key loaded from SENDGRID_API_KEY environment variable");
            return apiKey.trim();
        }
        
        return null;
    }
}

