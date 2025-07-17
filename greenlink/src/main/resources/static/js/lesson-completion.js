/**
 * GreenLink Lesson Completion Script
 * This script handles:
 * - Quiz answer tracking
 * - Lesson completion submission with answers and reflection
 * - Integration with API for saving progress
 */

// Store CSRF tokens from meta tags
const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

// Track quiz answers
let quizAnswers = {};
let reflectionText = "";

// Function to check if user is authenticated
function isAuthenticated() {
    return document.getElementById('authenticated-status') !== null;
}

// Helper function to make API calls
async function apiCall(url, method, data) {
    const headers = {
        'Content-Type': 'application/json'
    };
    
    // Add CSRF token for POST requests
    if (method === 'POST') {
        headers[header] = token;
    }
    
    try {
        const response = await fetch(url, {
            method: method,
            headers: headers,
            body: data ? JSON.stringify(data) : null
        });
        
        return await response.json();
    } catch (error) {
        console.error('API call error:', error);
        return { success: false, message: 'Error connecting to server: ' + error.message };
    }
}

// Save an answer when a user selects an option
function trackAnswer(questionNumber, selectedOption, isCorrect) {
    quizAnswers[`question_${questionNumber}`] = {
        questionNumber: questionNumber,
        selectedOption: selectedOption,
        isCorrect: isCorrect
    };
}

// Add this function call to the quiz option click handler
document.querySelectorAll('.quiz-option').forEach(option => {
    const originalClickHandler = option.onclick || function() {};
    
    option.onclick = function(event) {
        // Call the original handler first
        const result = originalClickHandler.call(this, event);
        
        // Now track the answer
        const questionSection = this.closest('.quiz-section');
        const questionNumber = Array.from(document.querySelectorAll('.quiz-section')).indexOf(questionSection) + 1;
        const optionLetter = this.querySelector('strong').textContent.replace(/[^A-D]/g, '');
        const isCorrect = this.getAttribute('data-correct') === 'true';
        
        trackAnswer(questionNumber, optionLetter, isCorrect);
        
        return result;
    };
});

// Track reflection text changes
document.getElementById('reflection-text').addEventListener('input', function() {
    reflectionText = this.value.trim();
});

// Hook into the complete lesson button
document.getElementById('complete-lesson').addEventListener('click', async function(event) {
    // Prevent default action temporarily
    event.preventDefault();
    
    // If not authenticated, show login modal (original behavior)
    if (!isAuthenticated()) {
        const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
        loginModal.show();
        return;
    }
    
    // Get lesson ID from the URL
    const urlPath = window.location.pathname;
    const lessonIdMatch = urlPath.match(/\/curs\/(\d+)/) || urlPath.match(/\/curs(\d+)/);
    const lessonId = lessonIdMatch ? parseInt(lessonIdMatch[1]) : 1;
    
    // Calculate score from answers
    const correctAnswers = Object.values(quizAnswers).filter(a => a.isCorrect).length;
    const totalQuestions = document.querySelectorAll('.quiz-section').length;
    const pointsEarned = correctAnswers * 10;
    
    // Gather image data if uploaded
    const imageInput = document.getElementById('image-upload');
    const hasImage = imageInput && imageInput.files.length > 0;
    
    // Create submission data
    const submissionData = {
        correctAnswers: correctAnswers,
        totalQuestions: totalQuestions,
        pointsEarned: pointsEarned + (reflectionText ? 5 : 0) + (hasImage ? 10 : 0),
        reflectionText: reflectionText,
        answers: quizAnswers
    };
    
    // Show loading state
    this.disabled = true;
    this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Se salvează...';
    
    try {
        // Call the API to save progress
        const result = await apiCall(`/api/user-progress/complete-lesson/${lessonId}`, 'POST', submissionData);
        
        if (result.success) {
            // Show success message
            const successMessage = `Felicitări! Ai completat lecția cu succes! 🎉\nPuncte finale: ${result.pointsEarned}/40`;
            alert(successMessage);
            
            // Redirect to education map with completion parameter
            window.location.href = `/educatie?completed=${lessonId}`;
        } else {
            // Show error message
            alert('Eroare: ' + (result.message || 'Nu s-a putut salva progresul.'));
            this.disabled = false;
            this.innerHTML = '<i class="bi bi-check-circle me-2"></i>Completează lecția';
        }
    } catch (error) {
        console.error('Error submitting lesson completion:', error);
        alert('Eroare tehnică: ' + error.message);
        this.disabled = false;
        this.innerHTML = '<i class="bi bi-check-circle me-2"></i>Completează lecția';
    }
});

// On page load, check if lesson already completed
document.addEventListener('DOMContentLoaded', async function() {
    // Get lesson ID from the URL
    const urlPath = window.location.pathname;
    const lessonIdMatch = urlPath.match(/\/curs\/(\d+)/) || urlPath.match(/\/curs(\d+)/);
    const lessonId = lessonIdMatch ? parseInt(lessonIdMatch[1]) : 1;
    
    if (isAuthenticated()) {
        // Check if already completed
        try {
            const result = await apiCall(`/api/user-progress?lessonId=${lessonId}`, 'GET');
            if (result.completed) {
                // Show completed message or update UI as needed
                const completedMessage = document.querySelector('.lesson-completed');
                if (completedMessage) {
                    completedMessage.style.display = 'block';
                }
            }
        } catch (error) {
            console.error('Error checking lesson completion status:', error);
        }
    }
});
