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


    const uploadButton = document.getElementById('upload-button');
    const imageUpload = document.getElementById('image-upload');
    const profileImage = document.getElementById('profile-image');

    uploadButton.addEventListener('click', function () {
        imageUpload.click();
    });

    imageUpload.addEventListener('change', function () {
        if (this.files && this.files[0]) {
            const reader = new FileReader();

            reader.onload = function (e) {
                profileImage.src = e.target.result;
            }

            reader.readAsDataURL(this.files[0]);
        }
    });

    // Subscription functionality
    const renewButton = document.getElementById('renew-button');
    const addSubscriptionButton = document.getElementById('add-subscription-button');
    const subscriptionDropdown = document.getElementById('subscription-dropdown');

    renewButton.addEventListener('click', function () {
        const selectedSubscription = subscriptionDropdown.value;
        if (selectedSubscription) {
            alert(`Renewing subscription: ${subscriptionDropdown.options[subscriptionDropdown.selectedIndex].text}`);
            // In a real application, you would make an API call here
        } else {
            alert('Please select a subscription to renew.');
        }
    });

    addSubscriptionButton.addEventListener('click', function () {
        alert('Redirecting to subscription plans...');
        // In a real application, you would redirect to a subscription page
    });
});