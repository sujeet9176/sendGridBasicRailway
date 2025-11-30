# SendGrid Basic - Template Creation Web Application

A simple Java web application for creating SendGrid dynamic email templates via a web interface.

## Features

- **SendGrid Integration** - Create dynamic email templates via web interface
- **Java Servlet** - Server-side API handling
- **HTML/JavaScript** - Modern client-side interface
- **Log4j2** - Comprehensive logging
- **Maven Build** - Standard Maven project structure

## Project Structure

```
sendGridBasic/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sendgrid/servlet/
│   │   │       └── TemplateServlet.java
│   │   ├── resources/
│   │   │   └── log4j2.xml          # Logging configuration
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml         # Web application configuration
│   │       ├── css/
│   │       │   └── style.css
│   │       ├── js/
│   │       │   └── main.js
│   │       ├── index.html
│   │       └── template.html
└── README.md
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Tomcat 9.0 (or use embedded Tomcat via Maven plugin)
- SendGrid API Key (for template creation feature)

## Building the Project

```bash
# Compile and package the project
mvn clean package

# This will create a WAR file in the target/ directory
```

## Running the Application

### Option 1: Using Maven Tomcat Plugin

```bash
# Set your SendGrid API key
export SENDGRID_API_KEY=your_api_key_here

# Run the application on embedded Tomcat
mvn tomcat7:run

# The application will be available at http://localhost:8080
```

### Option 2: Deploy to Tomcat Server

1. Copy the `target/sendGridBasic.war` file to your Tomcat `webapps/` directory
2. Set the `SENDGRID_API_KEY` environment variable
3. Start Tomcat server
4. Access the application at `http://localhost:8080/sendGridBasic`

## Usage

1. Navigate to `http://localhost:8080/template.html`
2. Enter a template name (e.g., "welcome_email")
3. Click "Create Template"
4. View the API response with status code, response body, and headers

## API Endpoint

- `POST /template` - Creates a SendGrid dynamic email template
  - Request: Form data with `templateName` parameter
  - Response: JSON with success status, template name, status code, response body, and headers

## Configuration

### Logging

Logging is configured in `src/main/resources/log4j2.xml`. Logs are written to:
- Console output
- `logs/application.log` file

### Web Application

Web application configuration is in `src/main/webapp/WEB-INF/web.xml`.

### SendGrid API Key Configuration

To use the SendGrid template creation feature, you need to set the `SENDGRID_API_KEY` environment variable:

**On Linux/Mac:**
```bash
export SENDGRID_API_KEY=your_api_key_here
```

**On Windows:**
```cmd
set SENDGRID_API_KEY=your_api_key_here
```

**For Maven Tomcat Plugin:**
```bash
SENDGRID_API_KEY=your_api_key_here mvn tomcat7:run
```

**Getting Your SendGrid API Key:**
1. Sign up or log in to [SendGrid](https://sendgrid.com/)
2. Go to Settings > API Keys
3. Create a new API key with "Full Access" or "Template Editor" permissions
4. Copy the API key and set it as an environment variable

**Note:** Never commit your API key to version control. Always use environment variables or secure configuration management.

## Deployment

### Azure Deployment

The project includes Azure Web App Maven plugin configuration. To deploy:

```bash
mvn azure-webapp:deploy
```

Make sure to configure your Azure credentials and set the `SENDGRID_API_KEY` environment variable in Azure.

## License

This project is open source and available for use.
# sendgridbasic
