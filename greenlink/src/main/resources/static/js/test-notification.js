/**
 * Test script for challenge notifications
 * Add this script to any page to manually trigger challenge notifications
 */

document.addEventListener('DOMContentLoaded', function() {
    // Create test button
    const testButton = document.createElement('button');
    testButton.id = 'test-challenge-notification';
    testButton.textContent = 'Test Challenge Notification';
    testButton.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        z-index: 9999;
        background-color: #28a745;
        color: white;
        border: none;
        border-radius: 4px;
        padding: 10px 15px;
        cursor: pointer;
        box-shadow: 0 2px 5px rgba(0,0,0,0.2);
    `;
    
    // Add hover effect
    testButton.onmouseover = function() {
        this.style.backgroundColor = '#218838';
    };
    testButton.onmouseout = function() {
        this.style.backgroundColor = '#28a745';
    };
    
    // Add click handler to trigger notification
    testButton.onclick = function() {
        // Check if notification function exists in global scope (added by challenge-notification.js)
        if (typeof window.showChallengeNotification === 'function') {
            window.showChallengeNotification('Test Challenge', 25);
        } else {
            alert('Challenge notification system not loaded. Make sure challenge-notification.js is included on the page.');
        }
    };
    
    // Add button to page
    document.body.appendChild(testButton);
});
