# Instructions for Course Pages Translation

To make course pages fully translatable, follow these steps for each course page (curs.html, curs2.html, etc.):

## Common Elements

Replace the following hardcoded text with translation keys:

1. Back button:
```html
<a href="/educatie" class="back-button">
    <i class="bi bi-arrow-left"></i>
    <span th:text="#{course.back.button}">Înapoi la harta cursurilor</span>
</a>
```

2. Lesson completed message:
```html
<div class="lesson-completed" th:if="${lessonCompleted}" sec:authorize="isAuthenticated()">
    <i class="bi bi-check-circle-fill me-2" style="font-size: 1.5rem;"></i>
    <span th:text="#{course.lesson.completed}">Felicitări! Ai completat această lecție cu succes!</span>
</div>
```

3. Lesson progress:
```html
<div class="progress-label">
    <span th:text="#{course.lesson.progress}">Progresul lecției</span>
    <span id="progress-text">0/5</span>
</div>
```

4. Quiz questions:
```html
<h4><i class="bi bi-question-circle me-2"></i><span th:text="#{course.question}">Întrebare</span> 1:</h4>
```

5. Submit button:
```html
<button type="submit" class="btn btn-success" th:text="#{course.submit.answers}">Trimite Răspunsurile</button>
```

## Lesson-specific Content

The lesson-specific content (like paragraphs, headings, etc.) can be left in Romanian, but you may want to create specific translation keys for these in the future.

For example:
- Lesson 1 title/description
- Lesson 2 title/description
- etc.

## Testing the Translation

After implementing these changes, you can test the translation by:

1. Changing the language using the language switcher in the navbar
2. Ensuring that all common elements are properly translated based on the selected language

All translation keys are defined in:
- `messages_en.properties` for English translations
- `messages_ro.properties` for Romanian translations
