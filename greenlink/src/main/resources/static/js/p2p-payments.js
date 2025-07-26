// P2P Payments JavaScript for GreenLink Marketplace
class P2PPaymentHandler {
    constructor() {
        this.stripe = Stripe(window.stripePublishableKey);
        this.elements = null;
        this.paymentElement = null;
    }

    /**
     * Initialize payment elements
     */
    async initializePaymentElements(clientSecret) {
        const { elements } = await this.stripe.elements({
            clientSecret: clientSecret,
            appearance: {
                theme: 'stripe',
                variables: {
                    colorPrimary: '#28a745',
                    colorBackground: '#ffffff',
                    colorText: '#30313d',
                    colorDanger: '#df1b41',
                    fontFamily: 'Poppins, system-ui, sans-serif',
                    spacingUnit: '4px',
                    borderRadius: '8px'
                }
            }
        });

        this.elements = elements;
        this.paymentElement = elements.create('payment');
        this.paymentElement.mount('#payment-element');
    }

    /**
     * Create direct transfer between users
     */
    async createDirectTransfer(toUserId, amount, description) {
        try {
            const response = await fetch('/payment/direct-transfer', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                },
                body: new URLSearchParams({
                    toUserId: toUserId,
                    amount: amount,
                    description: description
                })
            });

            const result = await response.json();
            
            if (response.ok) {
                this.showSuccessMessage('Transfer created successfully!');
                return result.transferId;
            } else {
                this.showErrorMessage(result.error || 'Transfer failed');
                return null;
            }
        } catch (error) {
            console.error('Error creating direct transfer:', error);
            this.showErrorMessage('Network error occurred');
            return null;
        }
    }

    /**
     * Create instant payout for seller
     */
    async createInstantPayout(amount) {
        try {
            const response = await fetch('/payment/instant-payout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                },
                body: new URLSearchParams({
                    amount: amount
                })
            });

            const result = await response.json();
            
            if (response.ok) {
                this.showSuccessMessage('Instant payout created successfully!');
                return result.payoutId;
            } else {
                this.showErrorMessage(result.error || 'Payout failed');
                return null;
            }
        } catch (error) {
            console.error('Error creating instant payout:', error);
            this.showErrorMessage('Network error occurred');
            return null;
        }
    }

    /**
     * Create payment intent for direct charge
     */
    async createPaymentIntent(sellerId, amount, description) {
        try {
            const response = await fetch('/payment/create-payment-intent', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                },
                body: new URLSearchParams({
                    sellerId: sellerId,
                    amount: amount,
                    description: description
                })
            });

            const result = await response.json();
            
            if (response.ok) {
                return {
                    paymentIntentId: result.paymentIntentId,
                    clientSecret: result.clientSecret
                };
            } else {
                this.showErrorMessage(result.error || 'Payment intent creation failed');
                return null;
            }
        } catch (error) {
            console.error('Error creating payment intent:', error);
            this.showErrorMessage('Network error occurred');
            return null;
        }
    }

    /**
     * Confirm payment with Stripe
     */
    async confirmPayment(clientSecret) {
        try {
            const { error } = await this.stripe.confirmPayment({
                elements: this.elements,
                confirmParams: {
                    return_url: window.location.origin + '/payment/success',
                }
            });

            if (error) {
                this.showErrorMessage(error.message);
                return false;
            }

            return true;
        } catch (error) {
            console.error('Error confirming payment:', error);
            this.showErrorMessage('Payment confirmation failed');
            return false;
        }
    }

    /**
     * Get account balance
     */
    async getAccountBalance() {
        try {
            const response = await fetch('/payment/account-balance', {
                method: 'GET',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                }
            });

            const result = await response.json();
            
            if (response.ok) {
                return result;
            } else {
                this.showErrorMessage(result.error || 'Failed to get balance');
                return null;
            }
        } catch (error) {
            console.error('Error getting account balance:', error);
            this.showErrorMessage('Network error occurred');
            return null;
        }
    }

    /**
     * Create refund
     */
    async createRefund(paymentIntentId, amount, reason) {
        try {
            const response = await fetch('/payment/refund', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                },
                body: new URLSearchParams({
                    paymentIntentId: paymentIntentId,
                    amount: amount,
                    reason: reason
                })
            });

            const result = await response.json();
            
            if (response.ok) {
                this.showSuccessMessage('Refund created successfully!');
                return result.refundId;
            } else {
                this.showErrorMessage(result.error || 'Refund failed');
                return null;
            }
        } catch (error) {
            console.error('Error creating refund:', error);
            this.showErrorMessage('Network error occurred');
            return null;
        }
    }

    /**
     * Show success message
     */
    showSuccessMessage(message) {
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-white bg-success border-0';
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-check-circle me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        const container = document.getElementById('toast-container') || document.body;
        container.appendChild(toast);
        
        const bsToast = new bootstrap.Toast(toast, { autohide: true, delay: 5000 });
        bsToast.show();
    }

    /**
     * Show error message
     */
    showErrorMessage(message) {
        const toast = document.createElement('div');
        toast.className = 'toast align-items-center text-white bg-danger border-0';
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-exclamation-circle me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        const container = document.getElementById('toast-container') || document.body;
        container.appendChild(toast);
        
        const bsToast = new bootstrap.Toast(toast, { autohide: true, delay: 5000 });
        bsToast.show();
    }
}

// Initialize P2P Payment Handler
const p2pHandler = new P2PPaymentHandler();

// Export for use in other scripts
window.P2PPaymentHandler = P2PPaymentHandler;
window.p2pHandler = p2pHandler; 