document.addEventListener('DOMContentLoaded', function() {
    // Challenge category filter
    const filterButtons = document.querySelectorAll('[data-challenge-type]');
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const type = this.dataset.challengeType;
            filterChallenges(type);
            
            // Update active state
            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Start challenge
    const startButtons = document.querySelectorAll('[data-challenge-start]');
    startButtons.forEach(button => {
        button.addEventListener('click', function() {
            const challengeId = this.dataset.challengeStart;
            startChallenge(challengeId);
        });
    });

    // Update progress
    const progressButtons = document.querySelectorAll('[data-challenge-progress]');
    progressButtons.forEach(button => {
        button.addEventListener('click', function() {
            const challengeId = this.dataset.challengeProgress;
            const progress = prompt('Enter progress percentage (0-100):');
            if (progress !== null && !isNaN(progress)) {
                updateProgress(challengeId, Math.min(100, Math.max(0, parseInt(progress))));
            }
        });
    });
});

function filterChallenges(type) {
    fetch(`/provocari/type/${type}`)
        .then(response => response.text())
        .then(html => {
            document.querySelector('#challengeList').innerHTML = html;
        })
        .catch(error => console.error('Error:', error));
}

function startChallenge(challengeId) {
    fetch(`/provocari/${challengeId}/start`, {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            location.reload();
        } else {
            alert('Error: ' + result);
        }
    })
    .catch(error => console.error('Error:', error));
}

function updateProgress(challengeId, progress) {
    fetch(`/provocari/${challengeId}/progress?progress=${progress}`, {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            location.reload();
        } else {
            alert('Error: ' + result);
        }
    })
    .catch(error => console.error('Error:', error));
} 