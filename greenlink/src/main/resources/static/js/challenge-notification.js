/**
 * Challenge Notification System
 * Shows popup notifications when users complete challenges
 */

// Ensure we have one global instance of the notification system
let notificationSystemInitialized = false;

// Function to initialize notification system
function initializeNotificationSystem() {
    if (notificationSystemInitialized) return;
    notificationSystemInitialized = true;

    console.log('Initializing challenge notification system...');

document.addEventListener('DOMContentLoaded', function() {
    // Create notification container if it doesn't exist
    let notificationContainer = document.getElementById('challenge-notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.id = 'challenge-notification-container';
        document.body.appendChild(notificationContainer);
    }

    // Style for notifications
    const style = document.createElement('style');
    style.textContent = `
        #challenge-notification-container {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 99999;
            display: flex !important;
            flex-direction: column;
            gap: 10px;
            max-width: 350px;
            pointer-events: auto !important;
            visibility: visible !important;
        }
        
        .challenge-notification {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            border-radius: 8px;
            padding: 15px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
            display: flex;
            align-items: center;
            gap: 15px;
            transform: translateX(120%);
            opacity: 0;
            transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            cursor: pointer;
            overflow: hidden;
            position: relative;
        }
        
        .challenge-notification.show {
            transform: translateX(0);
            opacity: 1;
        }
        
        .challenge-notification:hover {
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
            transform: translateY(-3px);
        }
        
        .challenge-notification:active {
            transform: translateY(0);
        }
        
        .notification-icon {
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
        }
        
        .notification-content {
            flex-grow: 1;
        }
        
        .notification-title {
            font-weight: 700;
            font-size: 1rem;
            margin-bottom: 4px;
        }
        
        .notification-description {
            font-size: 0.9rem;
            opacity: 0.9;
            margin-bottom: 4px;
        }
        
        .notification-points {
            font-weight: 600;
            color: #FFD700;
            font-size: 0.9rem;
        }
        
        .notification-close {
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
            transition: all 0.2s ease;
        }
        
        .notification-close:hover {
            color: white;
            background: rgba(0, 0, 0, 0.1);
            border-radius: 4px;
        }
        
        .notification-badge {
            position: absolute;
            top: 10px;
            right: 10px;
            display: flex;
            align-items: center;
            background: rgba(255, 255, 255, 0.2);
            padding: 3px 8px;
            border-radius: 15px;
            font-size: 0.8rem;
            font-weight: 600;
        }
        
        .notification-badge-icon {
            margin-right: 4px;
            font-size: 0.9rem;
        }
        
        .notification-progress {
            position: absolute;
            bottom: 0;
            left: 0;
            height: 3px;
            background: rgba(255, 255, 255, 0.5);
            border-radius: 0 0 8px 8px;
            width: 100%;
            transform-origin: left;
        }
        
        @keyframes progress {
            0% {
                transform: scaleX(1);
            }
            100% {
                transform: scaleX(0);
            }
        }
    `;
    document.head.appendChild(style);

    // Connect to WebSocket for challenge completion notifications
    let stompClient = null;
    function connectWebSocket() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            // Libraries not loaded yet or unavailable
            console.warn('SockJS or Stomp libraries not loaded');
            return;
        }

        try {
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null; // Disable debug logs
            
            stompClient.connect({}, function(frame) {
                console.log('Challenge notifications connected: ' + frame);
                
                // Subscribe to challenge completion events
                stompClient.subscribe('/user/queue/challenge-completed', function(message) {
                    console.log('Received challenge completion notification:', message.body);
                    try {
                        const challengeData = JSON.parse(message.body);
                        // Make sure we properly handle both event format and direct challenge format
                        if (challengeData.challengeId || challengeData.id) {
                            showChallengeNotification({
                                title: challengeData.title || challengeData.challenge?.title || 'Challenge Completed',
                                points: challengeData.points || challengeData.challenge?.points || 10
                            });
                        } else {
                            showChallengeNotification(challengeData);
                        }
                    } catch (e) {
                        console.error('Error processing challenge notification:', e);
                    }
                });
                
                // Also subscribe to the debug topic (for testing)
                stompClient.subscribe('/topic/debug-challenges', function(message) {
                    console.log('Debug challenge message received:', message.body);
                    try {
                        // Try to parse and show debug messages too
                        const debugData = JSON.parse(message.body);
                        if (debugData.title || debugData.challenge?.title) {
                            showChallengeNotification({
                                title: debugData.title || debugData.challenge?.title || 'Debug Challenge',
                                points: debugData.points || debugData.challenge?.points || 5
                            });
                        }
                    } catch (e) {
                        // It's just debug, so don't worry if it fails
                        console.log('Could not process debug message as notification');
                    }
                });
            }, function(error) {
                // On error, try to reconnect after a few seconds
                console.error('WebSocket connection error:', error);
                setTimeout(connectWebSocket, 5000);
            });
        } catch (e) {
            console.error('Error establishing WebSocket connection:', e);
        }
    }

    // Show notification function
    function showChallengeNotification(challenge) {
        console.log('Showing challenge notification:', challenge);
        
        // Make sure container exists
        let notificationContainer = document.getElementById('challenge-notification-container');
        if (!notificationContainer) {
            console.log('Container not found, creating one...');
            notificationContainer = document.createElement('div');
            notificationContainer.id = 'challenge-notification-container';
            document.body.appendChild(notificationContainer);
        }
        
        // Force container visibility
        notificationContainer.style.display = 'flex';
        notificationContainer.style.visibility = 'visible';
        notificationContainer.style.zIndex = '99999';
        notificationContainer.style.pointerEvents = 'auto';
        
        const notification = document.createElement('div');
        notification.className = 'challenge-notification';
        notification.innerHTML = `
            <div class="notification-icon">
                <i class="bi bi-trophy-fill"></i>
            </div>
            <div class="notification-content">
                <div class="notification-title">Challenge Completed!</div>
                <div class="notification-description">${challenge.title || 'You completed a challenge'}</div>
                <div class="notification-points">+${challenge.points || 0} points</div>
            </div>
            <button class="notification-close" aria-label="Close notification">×</button>
            <div class="notification-progress"></div>
        `;

        // Add animation to the progress bar
        const progressBar = notification.querySelector('.notification-progress');
        progressBar.style.animation = 'progress 8s linear forwards';

        // Add notification to container
        notificationContainer.appendChild(notification);

        // Show notification with animation
        setTimeout(() => {
            notification.classList.add('show');
            // Alert for testing
            if (window.location.pathname.includes('test-notification')) {
                console.log('NOTIFICATION SHOULD BE VISIBLE NOW');
            }
        }, 10);

        // Add click listener to redirect to challenges page
        notification.addEventListener('click', function(e) {
            // Don't redirect if the close button was clicked
            if (e.target.classList.contains('notification-close')) {
                return;
            }
            window.location.href = '/provocari';
        });

        // Add close button listener
        const closeButton = notification.querySelector('.notification-close');
        closeButton.addEventListener('click', function(e) {
            e.stopPropagation(); // Prevent the notification click event
            removeNotification(notification);
        });

        // Auto-remove after 8 seconds
        setTimeout(() => {
            removeNotification(notification);
        }, 8000);
    }

    // Helper function to remove notification with animation
    function removeNotification(notification) {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(120%)';
        
        // Remove from DOM after animation completes
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 500);
    }

    // Connect to WebSocket
    // Check if user is authenticated by looking for elements that only appear for logged-in users
    if (document.querySelector('a[href="/dashboard"]') || document.querySelector('a[href="/inbox"]')) {
        // Only connect if user is authenticated
        connectWebSocket();
    } else {
        console.log('User not authenticated, skipping WebSocket connection');
    }

    // For testing - expose function to global scope
    window.showChallengeNotification = function(title, points) {
        showChallengeNotification({
            title: title || 'Test Challenge',
            points: points || 10
        });
    };
});
}

// Initialize immediately
initializeNotificationSystem();
