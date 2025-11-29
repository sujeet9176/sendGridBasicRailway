// Main JavaScript file for SendGrid Basic

document.addEventListener('DOMContentLoaded', function() {
    console.log('SendGrid Basic - Page loaded');
    
    // Add any global JavaScript functionality here
});

// Utility function for API calls
async function apiCall(endpoint, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    if (data) {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(endpoint, options);
        return await response.json();
    } catch (error) {
        console.error('API call error:', error);
        throw error;
    }
}

