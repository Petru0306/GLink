// Theme Switcher JavaScript
class ThemeSwitcher {
    constructor() {
        this.currentTheme = localStorage.getItem('theme') || 'light';
        this.themeToggleBtn = null;
        this.init();
    }

    init() {
        // Apply saved theme on page load
        this.applyTheme(this.currentTheme);
        
        // Create and add theme toggle button
        this.createThemeToggleButton();
        
        // Add event listener for theme toggle
        this.addThemeToggleListener();
        
        // Add keyboard shortcut (Ctrl/Cmd + T)
        this.addKeyboardShortcut();
    }

    createThemeToggleButton() {
        // Create theme toggle button
        this.themeToggleBtn = document.createElement('button');
        this.themeToggleBtn.className = 'theme-toggle-btn';
        this.themeToggleBtn.setAttribute('aria-label', 'Toggle theme');
        this.themeToggleBtn.setAttribute('title', 'Toggle theme (Ctrl/Cmd + T)');
        
        // Set initial icon
        this.updateThemeIcon();
        
        // Find the navbar and insert the button before the language dropdown
        const navbar = document.querySelector('.navbar-nav');
        if (navbar) {
            const languageDropdown = navbar.nextElementSibling;
            if (languageDropdown && languageDropdown.classList.contains('dropdown')) {
                navbar.parentNode.insertBefore(this.themeToggleBtn, languageDropdown);
            } else {
                navbar.parentNode.appendChild(this.themeToggleBtn);
            }
        }
    }

    updateThemeIcon() {
        if (this.currentTheme === 'dark') {
            this.themeToggleBtn.innerHTML = '<i class="bi bi-sun-fill"></i>';
        } else {
            this.themeToggleBtn.innerHTML = '<i class="bi bi-moon-fill"></i>';
        }
    }

    addThemeToggleListener() {
        this.themeToggleBtn.addEventListener('click', () => {
            this.toggleTheme();
        });
    }

    addKeyboardShortcut() {
        document.addEventListener('keydown', (e) => {
            // Ctrl/Cmd + T to toggle theme
            if ((e.ctrlKey || e.metaKey) && e.key === 't') {
                e.preventDefault();
                this.toggleTheme();
            }
        });
    }

    toggleTheme() {
        const newTheme = this.currentTheme === 'light' ? 'dark' : 'light';
        this.switchTheme(newTheme);
    }

    switchTheme(theme) {
        // Add flicker effect
        this.addFlickerEffect();
        
        // Switch theme after a short delay to show flicker
        setTimeout(() => {
            this.applyTheme(theme);
            this.currentTheme = theme;
            this.updateThemeIcon();
            localStorage.setItem('theme', theme);
            
            // Remove flicker class
            setTimeout(() => {
                this.removeFlickerEffect();
            }, 300);
        }, 150);
    }

    applyTheme(theme) {
        const html = document.documentElement;
        
        if (theme === 'dark') {
            html.setAttribute('data-theme', 'dark');
        } else {
            html.removeAttribute('data-theme');
        }
        
        // Dispatch custom event for other scripts to listen to
        window.dispatchEvent(new CustomEvent('themeChanged', { 
            detail: { theme: theme } 
        }));
    }

    addFlickerEffect() {
        // Add flicker class to body and main elements
        document.body.classList.add('flicker');
        
        // Add flicker to main content areas
        const mainElements = document.querySelectorAll('.container, .container-fluid, .navbar, .card, .modal-content');
        mainElements.forEach(element => {
            element.classList.add('flicker');
        });
    }

    removeFlickerEffect() {
        // Remove flicker class from all elements
        document.body.classList.remove('flicker');
        
        const flickerElements = document.querySelectorAll('.flicker');
        flickerElements.forEach(element => {
            element.classList.remove('flicker');
        });
    }

    // Public method to get current theme
    getCurrentTheme() {
        return this.currentTheme;
    }

    // Public method to set theme programmatically
    setTheme(theme) {
        if (theme === 'light' || theme === 'dark') {
            this.switchTheme(theme);
        }
    }
}

// Initialize theme switcher when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.themeSwitcher = new ThemeSwitcher();
});

// Also initialize if DOM is already loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.themeSwitcher = new ThemeSwitcher();
    });
} else {
    window.themeSwitcher = new ThemeSwitcher();
}

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ThemeSwitcher;
} 