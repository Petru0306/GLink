/**
 * Basic notification system as a fallback
 */

// Create global notification function
window.showBasicNotification = function(title, points) {
    console.log('Showing basic notification:', title, points);
    
    // Create container if needed
    let container = document.getElementById('basic-notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'basic-notification-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 999999;
            max-width: 350px;
        `;
        document.body.appendChild(container);
    }
    
    // Create notification
    const notification = document.createElement('div');
    notification.style.cssText = `
        background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
        color: white;
        border-radius: 8px;
        padding: 15px;
        margin-bottom: 10px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        display: flex;
        align-items: center;
        gap: 15px;
        cursor: pointer;
        overflow: hidden;
        position: relative;
    `;
    
    // Create icon
    const icon = document.createElement('div');
    icon.style.cssText = `
        font-size: 2rem;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        padding: 10px;
        height: 60px;
        width: 60px;
        flex-shrink: 0;
    `;
    icon.innerHTML = '<i class="bi bi-trophy-fill"></i>';
    
    // Create content
    const content = document.createElement('div');
    content.style.cssText = `flex-grow: 1;`;
    
    const notificationTitle = document.createElement('div');
    notificationTitle.style.cssText = `font-weight: 700; font-size: 1rem; margin-bottom: 4px;`;
    notificationTitle.textContent = 'Challenge Completed!';
    
    const description = document.createElement('div');
    description.style.cssText = `font-size: 0.9rem; opacity: 0.9; margin-bottom: 4px;`;
    description.textContent = title || 'You completed a challenge';
    
    const pointsElem = document.createElement('div');
    pointsElem.style.cssText = `font-weight: 600; color: #FFD700; font-size: 0.9rem;`;
    pointsElem.textContent = '+' + (points || 0) + ' points';
    
    content.appendChild(notificationTitle);
    content.appendChild(description);
    content.appendChild(pointsElem);
    
    // Add close button
    const closeBtn = document.createElement('button');
    closeBtn.style.cssText = `
        position: absolute;
        top: 5px;
        right: 5px;
        background: transparent;
        border: none;
        color: rgba(255, 255, 255, 0.7);
        font-size: 1rem;
        cursor: pointer;
        padding: 2px 5px;
        line-height: 1;
    `;
    closeBtn.textContent = '×';
    closeBtn.setAttribute('aria-label', 'Close notification');
    
    // Assemble notification
    notification.appendChild(icon);
    notification.appendChild(content);
    notification.appendChild(closeBtn);
    
    // Add to container
    container.appendChild(notification);
    
    // Click handlers
    notification.addEventListener('click', function(e) {
        // Don't redirect if close button clicked
        if (e.target === closeBtn) {
            return;
        }
        window.location.href = '/provocari';
    });
    
    closeBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        container.removeChild(notification);
    });
    
    // Auto-remove after 8 seconds
    setTimeout(function() {
        if (notification.parentNode) {
            container.removeChild(notification);
        }
    }, 8000);
    
    return notification;
};

// Subscribe to WebSocket for backup notifications
document.addEventListener('DOMContentLoaded', function() {
    // Check if SockJS is available (already included on the page)
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        try {
            const socket = new SockJS('/ws');
            const stompClient = Stomp.over(socket);
            stompClient.debug = null; // Disable debug logs
            
            stompClient.connect({}, function(frame) {
                console.log('Basic notification system connected');
                
                // Subscribe to challenge completion events
                stompClient.subscribe('/user/queue/challenge-completed', function(message) {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('Basic notification received challenge event:', data);
                        showBasicNotification(data.title, data.points);
                    } catch (e) {
                        console.error('Error parsing message:', e);
                    }
                });
                
                // Subscribe to debug topic
                stompClient.subscribe('/topic/debug-challenges', function(message) {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('Basic notification received debug event:', data);
                        showBasicNotification(data.title, data.points);
                    } catch (e) {
                        // Just log, don't show error
                        console.log('Could not parse debug message as notification');
                    }
                });
            });
        } catch (e) {
            console.error('Error setting up basic notifications:', e);
        }
    }
});
