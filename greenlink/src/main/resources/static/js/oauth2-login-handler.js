/**
 * OAuth2 Login Handler
 * 
 * This script handles the OAuth2 login flow for Google and GitHub authentication.
 * It opens a popup window for the OAuth2 provider and handles the redirect back to the application.
 */

// Global namespace for OAuth2 functionality
const OAuth2Login = {
    // Config
    popupWidth: 600,
    popupHeight: 700,
    popupWindowName: 'oauth2Login',
    
    /**
     * Initialize OAuth2 login buttons
     */
    init: function() {
        console.log('Initializing OAuth2 login handlers');
        
        // Add click handlers for OAuth2 buttons
        document.querySelectorAll('a[href^="/oauth2/authorization/"]').forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const href = this.getAttribute('href');
                OAuth2Login.openAuthWindow(href);
            });
        });
        
        // Listen for message from popup window
        window.addEventListener('message', function(event) {
            // Verify the origin of the message for security
            if (event.origin !== window.location.origin) {
                return;
            }
            
            if (event.data && event.data.type === 'oauth2-success') {
                console.log('OAuth2 login successful');
                // Reload the page or redirect to dashboard
                window.location.href = event.data.redirectUrl || '/dashboard';
            }
        });
    },
    
    /**
     * Open a popup window for OAuth2 authentication
     * @param {string} url - The OAuth2 authorization URL
     */
    openAuthWindow: function(url) {
        // Calculate center position
        const left = (window.innerWidth - OAuth2Login.popupWidth) / 2;
        const top = (window.innerHeight - OAuth2Login.popupHeight) / 2;
        
        // Open popup window
        const popupWindow = window.open(
            url,
            OAuth2Login.popupWindowName,
            `width=${OAuth2Login.popupWidth},height=${OAuth2Login.popupHeight},left=${left},top=${top},resizable=yes,scrollbars=yes`
        );
        
        // Focus on the popup
        if (popupWindow && popupWindow.focus) {
            popupWindow.focus();
        }
        
        // Check if popup was blocked
        if (!popupWindow) {
            alert('Please enable popups for this site to use social login.');
        }
        
        return false;
    }
};

// Initialize when DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    OAuth2Login.init();
});
