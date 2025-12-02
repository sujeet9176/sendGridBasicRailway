# Railway Deployment Guide

This document outlines the changes made to prepare the application for Railway deployment.

## Changes Made

### 1. Project Name Update
- Updated `pom.xml`: Changed `artifactId` and `finalName` from `sendGridBasic` to `sendGridBasicRailway`
- Updated all references throughout the project

### 2. Embedded Tomcat Server
- Created `EmbeddedTomcatServer.java` - A standalone Tomcat server that can run the WAR file
- Configured to read `PORT` environment variable (Railway requirement)
- Extracts and deploys the WAR file automatically

### 3. Railway Configuration Files
- **railway.json**: Railway deployment configuration
- **Procfile**: Process file specifying how to start the application
- **nixpacks.toml**: Build configuration for Railway (Java 11, Maven 3.9)
- **railway-start.sh**: Startup script that sets up the classpath and runs the server

### 4. HTML Path Updates
- Removed hardcoded `/sendGridBasic` paths from all HTML files
- Updated API endpoints to use root-relative paths (e.g., `/templates` instead of `/sendGridBasic/templates`)
- Files updated:
  - `template-ai.html`
  - `templates-lists.html`
  - `send-email-template-code.html`
  - `create-a-dynamic-template.html`

### 5. Build Configuration
- Added embedded Tomcat dependencies to `pom.xml`
- Configured WAR plugin to exclude `EmbeddedTomcatServer` from the WAR (so it can run separately)
- Added Maven dependency plugin to copy dependencies to `target/lib`

## Deployment Steps

1. **Push to Git Repository**
   ```bash
   git add .
   git commit -m "Prepare for Railway deployment"
   git push
   ```

2. **Create Railway Project**
   - Go to [railway.app](https://railway.app)
   - Create new project
   - Connect your Git repository

3. **Set Environment Variables**
   In Railway dashboard, add these variables:
   - `SENDGRID_API_KEY` - Your SendGrid API key
   - `OPENAI_API_KEY` - Your OpenAI API key (for AI features)
   - `PORT` - Automatically set by Railway (don't override unless needed)

4. **Deploy**
   - Railway will automatically detect the project and start building
   - Monitor the build logs in Railway dashboard
   - Once deployed, Railway will provide a URL

## How It Works

1. **Build Phase**: Railway runs `mvn clean package` which:
   - Compiles Java classes (including `EmbeddedTomcatServer`) to `target/classes`
   - Packages the application into `target/sendGridBasicRailway.war`
   - Copies dependencies to `target/lib/`
   - Note: `EmbeddedTomcatServer` is excluded from the WAR

2. **Start Phase**: Railway runs `railway-start.sh` which:
   - Sets up the classpath: `target/classes:target/lib/*:target/sendGridBasicRailway.war`
   - Runs `com.sendgrid.EmbeddedTomcatServer`
   - The server reads the `PORT` environment variable
   - Extracts the WAR file and deploys it
   - Starts listening on the specified port

## Testing Locally

To test the Railway deployment setup locally:

```bash
# Build the project
mvn clean package

# Set environment variables
export PORT=8080
export SENDGRID_API_KEY=your_key_here
export OPENAI_API_KEY=your_key_here

# Run the startup script
./railway-start.sh
```

The application should be available at `http://localhost:8080`

## Troubleshooting

### Build Fails
- Check that Java 11 and Maven are available
- Verify `nixpacks.toml` configuration

### Application Won't Start
- Check Railway logs for errors
- Verify `PORT` environment variable is set
- Ensure `SENDGRID_API_KEY` is set if using SendGrid features

### API Calls Fail
- Verify environment variables are set correctly in Railway
- Check that API keys are valid
- Review application logs in Railway dashboard

## Notes

- The application deploys at the root path (not `/sendGridBasicRailway`)
- All API endpoints are now root-relative (e.g., `/templates`, `/template-ai`)
- The embedded Tomcat server automatically handles WAR extraction and deployment

