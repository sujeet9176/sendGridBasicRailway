# SendGrid Basic Railway - Template Creation Web Application

A simple Java web application for creating SendGrid dynamic email templates via a web interface, configured for Railway deployment.

## Features

- **SendGrid Integration** - Create dynamic email templates via web interface
- **Java Servlet** - Server-side API handling
- **HTML/JavaScript** - Modern client-side interface
- **Log4j2** - Comprehensive logging
- **Maven Build** - Standard Maven project structure
- **Railway Ready** - Pre-configured for Railway platform deployment

## Project Structure

```
sendGridBasicRailway/
├── pom.xml                          # Maven configuration
├── railway.json                     # Railway deployment configuration
├── Procfile                         # Railway process file
├── nixpacks.toml                    # Railway build configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/sendgrid/
│   │   │   │   ├── servlet/        # Servlet classes
│   │   │   │   └── EmbeddedTomcatServer.java  # Embedded Tomcat for Railway
│   │   ├── resources/
│   │   │   ├── log4j2.xml          # Logging configuration
│   │   │   └── sendgrid.properties # Configuration file
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml         # Web application configuration
│   │       ├── css/
│   │       │   └── style.css
│   │       ├── js/
│   │       │   └── main.js
│   │       └── *.html              # HTML pages
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

1. Copy the `target/sendGridBasicRailway.war` file to your Tomcat `webapps/` directory
2. Set the `SENDGRID_API_KEY` environment variable
3. Start Tomcat server
4. Access the application at `http://localhost:8080/sendGridBasicRailway`

### Option 3: Run with Embedded Tomcat (Railway-style)

```bash
# Build the project
mvn clean package

# Run with embedded Tomcat
java -cp target/lib/*:target/sendGridBasicRailway.war com.sendgrid.EmbeddedTomcatServer

# The application will be available at http://localhost:8080
# Or use PORT environment variable: PORT=3000 java -cp ...
```

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

### Railway Deployment

This application is configured for Railway platform deployment. Follow these steps:

#### Prerequisites
1. A Railway account (sign up at [railway.app](https://railway.app))
2. Railway CLI installed (optional, but recommended)
3. Git repository (GitHub, GitLab, or Bitbucket)

#### Deployment Steps

1. **Push your code to a Git repository** (GitHub, GitLab, or Bitbucket)

2. **Create a new Railway project:**
   - Go to [railway.app](https://railway.app)
   - Click "New Project"
   - Select "Deploy from GitHub repo" (or your Git provider)
   - Select your repository

3. **Configure Environment Variables:**
   Railway will automatically detect the Java project. You need to set these environment variables in Railway:
   - `SENDGRID_API_KEY` - Your SendGrid API key
   - `OPENAI_API_KEY` - Your OpenAI API key (if using AI features)
   - `PORT` - Railway automatically sets this, but you can override if needed

   To set environment variables:
   - Go to your Railway project
   - Click on "Variables" tab
   - Add the required environment variables

4. **Deploy:**
   - Railway will automatically build and deploy when you push to your repository
   - Or click "Deploy" in the Railway dashboard

5. **Access your application:**
   - Railway will provide a URL like `https://your-app-name.up.railway.app`
   - Your application will be available at the root path (e.g., `https://your-app-name.up.railway.app/home.html`)

#### Railway Configuration Files

The project includes these Railway-specific files:
- `railway.json` - Railway deployment configuration
- `Procfile` - Process file for Railway
- `nixpacks.toml` - Build configuration for Railway

#### Build Process

Railway will:
1. Detect the Java/Maven project
2. Run `mvn clean package` to build the WAR file
3. Start the application using the embedded Tomcat server
4. The application will listen on the PORT environment variable (Railway provides this automatically)

#### Troubleshooting

- **Build fails:** Check that Java 11 and Maven are available (configured in `nixpacks.toml`)
- **Application won't start:** Check logs in Railway dashboard, ensure PORT environment variable is set
- **API calls fail:** Verify `SENDGRID_API_KEY` and `OPENAI_API_KEY` are set correctly in Railway environment variables

### Azure Deployment

The project includes Azure Web App Maven plugin configuration. To deploy:

```bash
mvn azure-webapp:deploy
```

Make sure to configure your Azure credentials and set the `SENDGRID_API_KEY` environment variable in Azure.

## License

This project is open source and available for use.
