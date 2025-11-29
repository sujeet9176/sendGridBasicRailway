package com.sendgrid.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Template Version Servlet - Handles creating template versions in SendGrid
 */
@WebServlet(name = "TemplateVersionServlet", urlPatterns = {"/template-version"})
public class TemplateVersionServlet extends HttpServlet {
    
    private static final Logger logger = LogManager.getLogger(TemplateVersionServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("TemplateVersionServlet - GET request received, redirecting to template-version.html");
        response.sendRedirect("template-version.html");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("TemplateVersionServlet - POST request received");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        // Get parameters from request
        String templateId = request.getParameter("template_id");
        String active = request.getParameter("active");
        String name = request.getParameter("name");
        String htmlContent = request.getParameter("html_content");
        String generatePlainContent = request.getParameter("generate_plain_content");
        String subject = request.getParameter("subject");
        String updatedAt = request.getParameter("updated_at");
        String editor = request.getParameter("editor");
        
        // Validate required fields
        if (templateId == null || templateId.trim().isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Template ID is required");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        if (name == null || name.trim().isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Version name is required");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        // Get API key
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("SendGrid API key is not configured");
            jsonResponse.put("success", false);
            jsonResponse.put("error", "SendGrid API key is not configured. Please set it in sendgrid.properties file or SENDGRID_API_KEY environment variable.");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        try {
            // Build request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("template_id", templateId.trim());
            requestBody.put("active", active != null && !active.trim().isEmpty() ? Integer.parseInt(active) : 1);
            requestBody.put("name", name.trim());
            requestBody.put("html_content", htmlContent != null ? htmlContent : "<!doctype><html><body></body></html>");
            requestBody.put("generate_plain_content", generatePlainContent != null && 
                          (generatePlainContent.equals("true") || generatePlainContent.equals("1")));
            requestBody.put("subject", subject != null ? subject : "");
            if (updatedAt != null && !updatedAt.trim().isEmpty()) {
                requestBody.put("updated_at", updatedAt.trim());
            }
            requestBody.put("editor", editor != null ? editor : "code");
            
            // Make POST request to SendGrid API
            String apiUrl = "https://api.sendgrid.com/v3/templates/" + templateId.trim() + "/versions";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Write request body
            connection.getOutputStream().write(requestBody.toString().getBytes("UTF-8"));
            
            logger.info("Sending request to SendGrid API: " + apiUrl);
            logger.info("Request body: " + requestBody.toString());
            
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
                    if (responseBody != null && !responseBody.isEmpty()) {
                        errorMessage = responseBody;
                    }
                }
                
                jsonResponse.put("success", false);
                jsonResponse.put("error", errorMessage);
                jsonResponse.put("statusCode", statusCode);
                jsonResponse.put("responseBody", responseBody);
            } else {
                // Success response
                try {
                    JSONObject sendGridResponse = new JSONObject(responseBody);
                    jsonResponse.put("success", true);
                    jsonResponse.put("statusCode", statusCode);
                    jsonResponse.put("data", sendGridResponse);
                } catch (Exception e) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("statusCode", statusCode);
                    jsonResponse.put("responseBody", responseBody);
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

