/**
 * Offer State Manager - Centralized state management for offer cards
 * Prevents race conditions and ensures real-time synchronization
 */
class OfferStateManager {
    constructor() {
        this.offers = new Map(); // messageId -> offer state
        this.pendingActions = new Set(); // Track pending server actions
        this.eventListeners = new Map(); // messageId -> array of listeners
        this.debounceTimers = new Map(); // messageId -> debounce timer
    }
    
    /**
     * Initialize offer state from existing DOM
     */
    initializeFromDOM() {
        const offerMessages = document.querySelectorAll('.offer-message');
        offerMessages.forEach(offerElement => {
            const messageId = offerElement.getAttribute('data-message-id');
            const status = offerElement.getAttribute('data-offer-status');
            const amount = this.extractOfferAmount(offerElement);
            
            if (messageId && status) {
                this.offers.set(messageId, {
                    status: status,
                    amount: amount,
                    isProcessing: false,
                    version: 0,
                    lastUpdated: Date.now()
                });
            }
        });
    }
    
    /**
     * Extract offer amount from DOM element
     */
    extractOfferAmount(offerElement) {
        const amountElement = offerElement.querySelector('.offer-amount');
        if (amountElement) {
            const text = amountElement.textContent;
            const match = text.match(/(\d+\.?\d*)/);
            return match ? parseFloat(match[1]) : 0;
        }
        return 0;
    }
    
    /**
     * Update offer state
     */
    updateOffer(messageId, updates) {
        const currentState = this.offers.get(messageId) || {};
        const newState = { ...currentState, ...updates, lastUpdated: Date.now() };
        
        this.offers.set(messageId, newState);
        this.notifyListeners(messageId, newState);
        
        console.log(`Offer ${messageId} updated:`, newState);
    }
    
    /**
     * Get current offer state
     */
    getOffer(messageId) {
        return this.offers.get(messageId);
    }
    
    /**
     * Check if offer is being processed
     */
    isProcessing(messageId) {
        const offer = this.offers.get(messageId);
        return offer ? offer.isProcessing : false;
    }
    
    /**
     * Mark offer as processing
     */
    setProcessing(messageId, isProcessing) {
        this.updateOffer(messageId, { isProcessing });
    }
    
    /**
     * Add event listener for offer updates
     */
    addListener(messageId, callback) {
        if (!this.eventListeners.has(messageId)) {
            this.eventListeners.set(messageId, []);
        }
        this.eventListeners.get(messageId).push(callback);
    }
    
    /**
     * Remove event listener
     */
    removeListener(messageId, callback) {
        const listeners = this.eventListeners.get(messageId);
        if (listeners) {
            const index = listeners.indexOf(callback);
            if (index > -1) {
                listeners.splice(index, 1);
            }
        }
    }
    
    /**
     * Notify all listeners for an offer
     */
    notifyListeners(messageId, state) {
        const listeners = this.eventListeners.get(messageId);
        if (listeners) {
            listeners.forEach(callback => {
                try {
                    callback(state);
                } catch (error) {
                    console.error('Error in offer state listener:', error);
                }
            });
        }
    }
    
    /**
     * Debounce action to prevent rapid clicks
     */
    debounceAction(messageId, action, delay = 300) {
        // Clear existing timer
        if (this.debounceTimers.has(messageId)) {
            clearTimeout(this.debounceTimers.get(messageId));
        }
        
        // Set new timer
        const timer = setTimeout(() => {
            this.debounceTimers.delete(messageId);
            action();
        }, delay);
        
        this.debounceTimers.set(messageId, timer);
    }
    
    /**
     * Handle server response and reconcile state
     */
    handleServerResponse(messageId, serverState) {
        const currentState = this.offers.get(messageId);
        
        if (!currentState) {
            // New offer, add to state
            this.updateOffer(messageId, {
                status: serverState.offerStatus,
                amount: serverState.offerAmount,
                isProcessing: false,
                version: serverState.version || 0
            });
            return;
        }
        
        // Check for version conflicts
        if (serverState.version && currentState.version && serverState.version < currentState.version) {
            console.warn(`Server state is older than local state for offer ${messageId}`);
            return;
        }
        
        // Update state with server response
        this.updateOffer(messageId, {
            status: serverState.offerStatus,
            amount: serverState.offerAmount,
            isProcessing: false,
            version: serverState.version || currentState.version
        });
    }
    
    /**
     * Handle optimistic updates
     */
    handleOptimisticUpdate(messageId, action, serverAction) {
        const currentState = this.offers.get(messageId);
        if (!currentState) return;
        
        // Mark as processing
        this.setProcessing(messageId, true);
        
        // Apply optimistic update
        const optimisticState = this.applyOptimisticUpdate(currentState, action);
        this.updateOffer(messageId, optimisticState);
        
        // Execute server action
        serverAction().then(response => {
            this.handleServerResponse(messageId, response);
        }).catch(error => {
            console.error('Server action failed:', error);
            // Revert to previous state
            this.updateOffer(messageId, currentState);
        }).finally(() => {
            this.setProcessing(messageId, false);
        });
    }
    
    /**
     * Apply optimistic update based on action
     */
    applyOptimisticUpdate(currentState, action) {
        switch (action.toUpperCase()) {
            case 'ACCEPT':
                return { ...currentState, status: 'ACCEPTED' };
            case 'REJECT':
                return { ...currentState, status: 'REJECTED' };
            case 'COUNTER':
                return { ...currentState, status: 'COUNTERED' };
            default:
                return currentState;
        }
    }
    
    /**
     * Clean up resources
     */
    cleanup() {
        this.offers.clear();
        this.pendingActions.clear();
        this.eventListeners.clear();
        this.debounceTimers.forEach(timer => clearTimeout(timer));
        this.debounceTimers.clear();
    }
}

// Global instance
window.offerStateManager = new OfferStateManager(); 