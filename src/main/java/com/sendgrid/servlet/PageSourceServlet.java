package com.sendgrid.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Page Source Servlet - Fetches HTML source from a given URL
 */
@WebServlet(name = "PageSourceServlet", urlPatterns = {"/page-source"})
public class PageSourceServlet extends HttpServlet {
    
    private static final Logger logger = LogManager.getLogger(PageSourceServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        String pageUrl = request.getParameter("url");
        
        if (pageUrl == null || pageUrl.trim().isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", "URL parameter is required");
            out.print(jsonResponse.toString());
            out.flush();
            return;
        }
        
        try {
            // Validate URL
            URL url = new URL(pageUrl.trim());
            
            logger.info("Fetching page source from: " + pageUrl);
            
            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
            
            int statusCode = connection.getResponseCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                // Read the HTML content
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder htmlContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line).append("\n");
                }
                reader.close();
                
                jsonResponse.put("success", true);
                jsonResponse.put("html_content", htmlContent.toString());
                jsonResponse.put("statusCode", statusCode);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("error", "Failed to fetch page. HTTP Status: " + statusCode);
                jsonResponse.put("statusCode", statusCode);
            }
            
        } catch (Exception ex) {
            logger.error("Error fetching page source: " + ex.getMessage(), ex);
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Error fetching page source: " + ex.getMessage());
        }
        
        out.print(jsonResponse.toString());
        out.flush();
    }
}

