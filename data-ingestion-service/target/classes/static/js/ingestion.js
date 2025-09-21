// Main JavaScript for Data Ingestion UI
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Handle file type selection
    const fileTypeRadios = document.querySelectorAll('input[name="fileType"]');
    fileTypeRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            const exampleSection = document.getElementById('exampleSection');
            if (this.value === 'patients') {
                document.getElementById('patientExample').classList.remove('d-none');
                document.getElementById('recordExample').classList.add('d-none');
            } else {
                document.getElementById('patientExample').classList.add('d-none');
                document.getElementById('recordExample').classList.remove('d-none');
            }
        });
    });
});

// Handle CSRF token for AJAX requests
function getCsrfToken() {
    return document.querySelector('input[name="_csrf"]').value;
}

// Show loading state for buttons
function setButtonLoading(button, isLoading) {
    const originalContent = button.innerHTML;
    if (isLoading) {
        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Processing...';
    } else {
        button.disabled = false;
        button.innerHTML = originalContent;
    }
}

// Show toast notifications
function showToast(type, message) {
    const toastContainer = document.getElementById('toastContainer');
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
    
    // Remove toast after it's hidden
    toastElement.addEventListener('hidden.bs.toast', function () {
        toastElement.remove();
    });
}

// Format file size
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Initialize file input preview
function initializeFileInput(inputId, previewId) {
    const fileInput = document.getElementById(inputId);
    const filePreview = document.getElementById(previewId);
    
    if (!fileInput || !filePreview) return;
    
    fileInput.addEventListener('change', function() {
        if (this.files && this.files[0]) {
            const file = this.files[0];
            const fileType = file.type;
            const fileSize = formatFileSize(file.size);
            
            let previewContent = `
                <div class="d-flex align-items-center">
                    <i class="fas fa-file-alt fa-2x text-primary me-3"></i>
                    <div>
                        <h6 class="mb-1">${file.name}</h6>
                        <small class="text-muted">${fileType} • ${fileSize}</small>
                    </div>
                    <button type="button" class="btn-close ms-auto" onclick="document.getElementById('${inputId}').value = ''; this.parentNode.parentNode.remove();"></button>
                </div>
            `;
            
            // If it's an image, show thumbnail
            if (fileType.match('image.*')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    previewContent = `
                        <div class="d-flex align-items-center">
                            <img src="${e.target.result}" class="img-thumbnail me-3" style="width: 60px; height: 60px; object-fit: cover;">
                            <div>
                                <h6 class="mb-1">${file.name}</h6>
                                <small class="text-muted">${fileType} • ${fileSize}</small>
                            </div>
                            <button type="button" class="btn-close ms-auto" onclick="document.getElementById('${inputId}').value = ''; this.parentNode.parentNode.remove();"></button>
                        </div>
                    `;
                    filePreview.innerHTML = previewContent;
                };
                reader.readAsDataURL(file);
            } else {
                filePreview.innerHTML = previewContent;
            }
            
            filePreview.classList.remove('d-none');
        }
    });
}

// Initialize all file inputs on the page
document.addEventListener('DOMContentLoaded', function() {
    initializeFileInput('fileInput', 'filePreview');
    
    // Handle form submission
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            const submitButton = this.querySelector('button[type="submit"]');
            
            setButtonLoading(submitButton, true);
            
            fetch(this.action, {
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': getCsrfToken()
                }
            })
            .then(response => response.json())
            .then(data => {
                setButtonLoading(submitButton, false);
                if (data.success) {
                    showToast('success', 'File uploaded successfully!');
                    // Reset form
                    this.reset();
                    document.getElementById('filePreview').classList.add('d-none');
                    
                    // If there's a table to refresh
                    if (typeof refreshDataTable === 'function') {
                        refreshDataTable();
                    }
                } else {
                    showToast('danger', data.message || 'An error occurred during upload.');
                }
            })
            .catch(error => {
                setButtonLoading(submitButton, false);
                console.error('Error:', error);
                showToast('danger', 'Failed to upload file. Please try again.');
            });
        });
    }
});
