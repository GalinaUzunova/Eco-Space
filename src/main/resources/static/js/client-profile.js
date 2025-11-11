document.addEventListener('DOMContentLoaded', function() {
    const descriptionLists = document.querySelectorAll('.description-ul');

    descriptionLists.forEach(list => {
        if (list.textContent.trim()) {
            const text = list.textContent.trim();
            const sentences = text.split('.').filter(sentence => sentence.trim() !== '');

            // Clear existing content
            list.innerHTML = '';

            // Create new list items for each sentence
            sentences.forEach(sentence => {
                const li = document.createElement('li');
                li.textContent = sentence.trim();
                list.appendChild(li);
            });
        }
    });

});