# V1 Codebase Snapshot - SendGrid Basic Web Application

## Project Overview
Java web application for managing SendGrid dynamic email templates with the following features:
- Create dynamic email templates
- View/list all templates
- Create template versions
- Fetch HTML content from URLs

## Project Structure

### Java Servlets (4 total)
1. **TemplateServlet.java** - Creates new SendGrid templates
   - Endpoint: POST /template
   - Uses SendGrid SDK
   - Validates template names (lowercase, alphanumeric, underscores, hyphens)

2. **TemplateListServlet.java** - Lists all dynamic templates
   - Endpoint: GET /templates
   - Uses direct HTTP calls to SendGrid API
   - Returns JSON with template list

3. **TemplateVersionServlet.java** - Creates template versions
   - Endpoint: POST /template-version
   - Uses direct HTTP calls to SendGrid API
   - Creates versions for existing templates

4. **PageSourceServlet.java** - Fetches HTML from URLs
   - Endpoint: GET /page-source?url=...
   - Fetches page source server-side
   - Returns HTML content as JSON

### HTML Pages (5 total)
1. **index.html** - Home page with feature cards
2. **template.html** - Create template form
3. **templates.html** - View templates with dropdown
4. **template-version.html** - Create template version form with URL fetch feature
5. **home.html** - Alternative home page

### Configuration Files
- **web.xml** - Servlet mappings for all 4 servlets
- **pom.xml** - Maven dependencies (SendGrid SDK, JSON, Log4j2, etc.)
- **sendgrid.properties** - API key configuration
- **log4j2.xml** - Logging configuration

## Key Features

### 1. Template Creation
- Form validation (client & server side)
- Template name normalization (lowercase)
- Pattern validation
- Error handling with SendGrid API responses

### 2. Template Listing
- Auto-loads on page load
- Dropdown with template names
- Shows template details on selection
- Refresh functionality

### 3. Template Version Creation
- Full form with all version fields
- URL fetch feature to get HTML from web pages
- Auto-populates HTML content textarea
- Validates required fields

### 4. HTML Fetch from URL
- URL input field
- Fetch button
- Server-side fetching (avoids CORS)
- Auto-populates HTML content field

## API Endpoints

1. POST /template - Create template
2. GET /templates - List templates
3. POST /template-version - Create version
4. GET /page-source?url=... - Fetch HTML from URL

## Dependencies
- Java 11
- SendGrid Java SDK 4.10.1
- JSON library
- Log4j2
- Servlet API 4.0.1

## Navigation Structure
All pages have navigation menu with:
- Home
- Create Template
- View Templates
- Create Version

## Current Deployment
- Deployed to: /Users/sujeetrai/Desktop/pythin_work/apache-tomcat-9.0.104/webapps/
- Application name: sendGridBasic
- Port: 8080
- Base URL: http://localhost:8080/sendGridBasic/

## Date: 2025-11-29
This snapshot represents the working V1 state of the application.
