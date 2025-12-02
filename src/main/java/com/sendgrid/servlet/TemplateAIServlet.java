package com.sendgrid.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Template AI Servlet - Handles ChatGPT API calls to generate HTML content
 */
@WebServlet(name = "TemplateAIServlet", urlPatterns = {"/template-ai"})
public class TemplateAIServlet extends HttpServlet {
    
    private static final Logger logger = LogManager.getLogger(TemplateAIServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("TemplateAIServlet - GET request received, redirecting to template-ai.html");
        response.sendRedirect("template-ai.html");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("TemplateAIServlet - POST request received");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        // Get prompt from request
        String prompt = request.getParameter("prompt");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Prompt is required");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        // Get OpenAI API key
        String apiKey = getOpenAIApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("OpenAI API key is not configured");
            jsonResponse.put("success", false);
            jsonResponse.put("error", "OpenAI API key is not configured. Please set it in sendgrid.properties file (openai.api.key) or OPENAI_API_KEY environment variable.");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        try {
            // Set the endpoint URL for ChatGPT
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            
            // Open HTTP connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Create request body (in JSON format)
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            
            JSONArray messages = new JSONArray();
            
            // System message
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", getSystemPrompt());
            messages.put(systemMessage);
            
            // User message with prompt
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);
            
            requestBody.put("messages", messages);
            
            String jsonInputString = requestBody.toString();
            
            logger.info("Sending request to OpenAI API");
            logger.debug("Request body: " + jsonInputString);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            logger.info("OpenAI API Response Code: " + responseCode);
            
            String responseBody;
            
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();
                responseBody = responseBuilder.toString();
                
                // Parse response to extract HTML content
                JSONObject openAIResponse = new JSONObject(responseBody);
                JSONArray choices = openAIResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    String content = message.getString("content");
                    
                    // Clean up the content - remove markdown code blocks if present
                    String htmlContent = content.trim();
                    if (htmlContent.startsWith("```html")) {
                        htmlContent = htmlContent.substring(7);
                    } else if (htmlContent.startsWith("```")) {
                        htmlContent = htmlContent.substring(3);
                    }
                    if (htmlContent.endsWith("```")) {
                        htmlContent = htmlContent.substring(0, htmlContent.length() - 3);
                    }
                    htmlContent = htmlContent.trim();
                    
                    // Wrap AI-generated content with header/footer template
                    String finalHtmlContent = wrapWithTemplate(htmlContent);
                    
                    jsonResponse.put("success", true);
                    jsonResponse.put("html_content", finalHtmlContent);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("error", "No response from AI");
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();
                responseBody = responseBuilder.toString();
                
                String errorMessage = "OpenAI API error";
                try {
                    JSONObject errorJson = new JSONObject(responseBody);
                    if (errorJson.has("error")) {
                        JSONObject error = errorJson.getJSONObject("error");
                        if (error.has("message")) {
                            errorMessage = error.getString("message");
                        }
                    }
                } catch (Exception e) {
                    if (responseBody != null && !responseBody.isEmpty()) {
                        errorMessage = responseBody;
                    }
                }
                
                jsonResponse.put("success", false);
                jsonResponse.put("error", errorMessage);
                jsonResponse.put("statusCode", responseCode);
            }
            
        } catch (IOException ex) {
            logger.error("IOException occurred while calling OpenAI API", ex);
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Error calling OpenAI API: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error occurred", ex);
            jsonResponse.put("success", false);
            jsonResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
        }
        
        out.print(jsonResponse.toString());
        out.flush();
    }
    
    /**
     * Get OpenAI API key from properties file or environment variable
     * @return API key string, or null if not found
     */
    private String getOpenAIApiKey() {
        // First try to read from properties file
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sendgrid.properties");
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                String apiKey = properties.getProperty("openai.api.key");
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    logger.info("OpenAI API key loaded from sendgrid.properties file");
                    return apiKey.trim();
                }
                inputStream.close();
            }
        } catch (IOException ex) {
            logger.warn("Could not read sendgrid.properties file: " + ex.getMessage());
        }
        
        // Fall back to environment variable
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            logger.info("OpenAI API key loaded from OPENAI_API_KEY environment variable");
            return apiKey.trim();
        }
        
        return null;
    }
    
    /**
     * Get OpenAI system prompt from properties file
     * @return System prompt string, or default prompt if not found
     */
    private String getSystemPrompt() {
        // Default prompt
        String defaultPrompt = "You are an expert HTML email template designer. Generate complete, valid HTML email templates. Always return only the HTML code without any markdown formatting, explanations, or code blocks. Return pure HTML that can be used directly in email templates.";
        
        // Try to read from properties file
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sendgrid.properties");
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                String prompt = properties.getProperty("openai.system.prompt");
                if (prompt != null && !prompt.trim().isEmpty()) {
                    logger.info("OpenAI system prompt loaded from sendgrid.properties file");
                    inputStream.close();
                    return prompt.trim();
                }
                inputStream.close();
            }
        } catch (IOException ex) {
            logger.warn("Could not read sendgrid.properties file for system prompt: " + ex.getMessage());
        }
        
        // Fall back to default prompt
        logger.info("Using default OpenAI system prompt");
        return defaultPrompt;
    }
    
    /**
     * Wrap AI-generated content with header/footer template
     * @param aiContent The AI-generated HTML content
     * @return Complete HTML with header, AI content, and footer
     */
    private String wrapWithTemplate(String aiContent) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("email-template-wrapper.html");
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder templateBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    templateBuilder.append(line).append("\n");
                }
                reader.close();
                inputStream.close();
                
                String template = templateBuilder.toString();
                
                // Replace the placeholder with AI-generated content
                // Match the exact indentation from the template file (28 spaces)
                String placeholder = "                            <!-- AI Code goes here Start -->\n                            <!-- AI Code goes here End-->";
                String replacement = "                            <!-- AI Code goes here Start -->\n                            " + aiContent + "\n                            <!-- AI Code goes here End-->";
                
                return template.replace(placeholder, replacement);
            }
        } catch (IOException ex) {
            logger.warn("Could not read email-template-wrapper.html file: " + ex.getMessage());
        }
        
        // Fallback: return AI content as-is if template not found
        logger.warn("Template wrapper not found, returning AI content without wrapper");
        return aiContent;
    }
}

