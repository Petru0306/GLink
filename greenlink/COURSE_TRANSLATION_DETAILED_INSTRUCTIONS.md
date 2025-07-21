# Detailed Instructions for Translating All Course Pages

The first two course pages (curs.html and curs2.html) have already been updated with translation keys. Here are detailed instructions for updating the remaining course pages (curs3.html through curs6.html).

## Common Elements to Update in Each File

For each course page (curs3.html, curs4.html, curs5.html, curs6.html), update the following sections:

### 1. Course Header
```html
<!-- Course Header -->
<header class="course-header text-center">
    <div class="container">
        <h1 class="display-4" th:text="#{home.lesson.center.X.title}">🌱 Lecția X: [Title]</h1>
        <p class="lead" th:text="#{home.lesson.center.X.desc}">[Description]</p>
    </div>
</header>
```
Replace `X` with the course number (3, 4, 5, or 6).

### 2. Back Button
```html
<!-- Back Button -->
<a href="/educatie" class="back-button">
    <i class="bi bi-arrow-left"></i>
    <span th:text="#{course.back.button}">Înapoi la harta cursurilor</span>
</a>
```

### 3. Lesson Completed Message
```html
<!-- Lesson completed message -->
<div class="lesson-completed" th:if="${lessonCompleted}" sec:authorize="isAuthenticated()">
    <i class="bi bi-check-circle-fill me-2" style="font-size: 1.5rem;"></i>
    <span th:text="#{course.lesson.completed}">Felicitări! Ai completat această lecție cu succes!</span>
</div>
```

### 4. Progress Bar Label
```html
<div class="progress-label">
    <span th:text="#{course.lesson.progress}">Progresul lecției</span>
    <span id="progress-text">0/5</span>
</div>
```

### 5. Quiz Questions (for each question in the course)
```html
<h4><i class="bi bi-question-circle me-2"></i><span th:text="#{course.question}">Întrebare</span> X:</h4>
```
Replace `X` with the question number (1, 2, 3, etc.).

### 6. Final Task Elements (Optional)
If you want to translate the final task buttons and labels:
```html
<h4><i class="bi bi-camera me-2"></i><span th:text="#{course.upload.photo}">Foto (10 pts)</span></h4>
```

```html
<h4><i class="bi bi-pencil me-2"></i><span th:text="#{course.write.reflection}">Scrie (5 pts)</span></h4>
```

## Testing Your Changes

After updating all course pages, test the translations by:

1. Starting the application
2. Visiting each course page
3. Switching the language between Romanian and English using the language switcher in the navigation bar
4. Verifying that all the translated elements change appropriately

## Notes for Developers

- Make sure to maintain the original text as the default content, so the pages still display correctly if translation keys are missing.
- Check the HTML structure carefully when adding the `th:text` attributes to ensure proper nesting.
- The provided translation keys are compatible with both Romanian (`messages_ro.properties`) and English (`messages_en.properties`) translations.

## Message Keys Reference

Here's a quick reference of all the message keys used for course pages:

- `home.lesson.center.X.title` - Course title (X is the course number)
- `home.lesson.center.X.desc` - Course description (X is the course number)
- `course.back.button` - Back to courses map button text
- `course.lesson.completed` - Lesson completed success message
- `course.lesson.progress` - Lesson progress label
- `course.question` - Question label
- `course.submit.answers` - Submit answers button
- `course.write.reflection` - Write reflection label
- `course.upload.photo` - Upload photo label
- `course.photo.instructions` - Photo upload instructions
- `course.completion.message` - Lesson completion message
- `course.points.earned` - Points earned label
