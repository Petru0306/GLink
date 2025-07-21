/**
 * GreenLink Custom Cursor Effects
 * Creates a particle trail effect that follows the cursor with glowing green particles
 */

document.addEventListener('DOMContentLoaded', function() {
    // Create style for cursor animations
    const style = document.createElement('style');
    style.textContent = `
        @keyframes particleFloat {
            0% {
                opacity: 1;
                transform: translateY(0) scale(1);
            }
            100% {
                opacity: 0;
                transform: translateY(-50px) scale(0);
            }
        }
        
        @keyframes ripple {
            to {
                transform: scale(4);
                opacity: 0;
            }
        }
        
        @keyframes clickParticle {
            0% {
                transform: scale(0) rotate(0deg);
                opacity: 1;
            }
            50% {
                transform: scale(1) rotate(180deg);
                opacity: 1;
            }
            100% {
                transform: scale(0) rotate(360deg);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);

    // Initialize cursor trail
    let cursorTrail = [];
    const maxTrailLength = 15;

    document.addEventListener('mousemove', function(e) {
        // Create main cursor dot
        const dot = document.createElement('div');
        dot.style.position = 'fixed';
        dot.style.left = e.clientX + 'px';
        dot.style.top = e.clientY + 'px';
        dot.style.width = '6px';
        dot.style.height = '6px';
        dot.style.background = 'var(--gradient-neon, linear-gradient(45deg, #00ff88, #39ff14, #50c878))';
        dot.style.borderRadius = '50%';
        dot.style.pointerEvents = 'none';
        dot.style.zIndex = '9999';
        dot.style.transition = 'all 0.3s ease';
        dot.style.boxShadow = 'var(--shadow-neon, 0 0 20px rgba(0, 255, 136, 0.5))';
        
        document.body.appendChild(dot);
        
        // Create particle effect
        for (let i = 0; i < 3; i++) {
            const particle = document.createElement('div');
            particle.style.position = 'fixed';
            particle.style.left = (e.clientX + Math.random() * 20 - 10) + 'px';
            particle.style.top = (e.clientY + Math.random() * 20 - 10) + 'px';
            particle.style.width = '3px';
            particle.style.height = '3px';
            particle.style.background = `hsl(${120 + Math.random() * 60}, 100%, 60%)`;
            particle.style.borderRadius = '50%';
            particle.style.pointerEvents = 'none';
            particle.style.zIndex = '9998';
            particle.style.animation = `particleFloat 1s ease-out forwards`;
            
            document.body.appendChild(particle);
            
            setTimeout(() => {
                if (particle.parentNode) {
                    particle.parentNode.removeChild(particle);
                }
            }, 1000);
        }
        
        cursorTrail.push(dot);
        
        if (cursorTrail.length > maxTrailLength) {
            const oldDot = cursorTrail.shift();
            oldDot.style.opacity = '0';
            oldDot.style.transform = 'scale(0.5)';
            setTimeout(() => {
                if (oldDot.parentNode) {
                    oldDot.parentNode.removeChild(oldDot);
                }
            }, 300);
        }
        
        setTimeout(() => {
            dot.style.opacity = '0';
            dot.style.transform = 'scale(0.8)';
        }, 150);
    });

    // Add click effect
    document.addEventListener('click', function(e) {
        // Don't create effect for clicks on form elements to avoid interfering with their functionality
        if (e.target.tagName === 'INPUT' || 
            e.target.tagName === 'TEXTAREA' || 
            e.target.tagName === 'SELECT' || 
            e.target.tagName === 'BUTTON') {
            return;
        }

        const particle = document.createElement('div');
        particle.style.position = 'fixed';
        particle.style.left = e.clientX + 'px';
        particle.style.top = e.clientY + 'px';
        particle.style.width = '8px';
        particle.style.height = '8px';
        particle.style.background = 'var(--gradient-neon, linear-gradient(45deg, #00ff88, #39ff14, #50c878))';
        particle.style.borderRadius = '50%';
        particle.style.pointerEvents = 'none';
        particle.style.zIndex = '9999';
        particle.style.animation = 'clickParticle 0.6s ease-out forwards';
        
        document.body.appendChild(particle);
        
        setTimeout(() => {
            if (particle.parentNode) {
                particle.parentNode.removeChild(particle);
            }
        }, 600);
    });
});
