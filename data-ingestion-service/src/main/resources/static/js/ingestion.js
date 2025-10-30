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

    // Initialize Dropzone if it exists on the page
    if (typeof Dropzone !== 'undefined') {
        initializeDropzone();
    }
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

// Initialize Dropzone for file uploads
function initializeDropzone() {
    const dropzoneElement = document.getElementById('dropzone');
    if (!dropzoneElement) return;

    // Remove any existing Dropzone instances
    if (dropzoneElement.dropzone) {
        dropzoneElement.dropzone.destroy();
    }

    // Initialize Dropzone
    const myDropzone = new Dropzone(dropzoneElement, {
        url: '/api/ingestion/patients/upload', // Default URL, will be updated based on file type
        paramName: 'file',
        maxFiles: 1,
        maxFilesize: 50, // MB
        acceptedFiles: '.csv',
        autoProcessQueue: false,
        uploadMultiple: false,
        createImageThumbnails: false,
        clickable: true,
        dictDefaultMessage: '<i class="fas fa-cloud-upload-alt fa-3x text-muted mb-3"></i><br>Drag & drop your file here or click to browse',
        
        init: function() {
            const dropzone = this;
            const startButton = document.getElementById('startUpload');
            const cancelButton = document.getElementById('cancelUpload');
            const fileTypeRadios = document.querySelectorAll('input[name="fileType"]');
            
            // Update URL based on selected file type
            function updateUrl() {
                const selectedType = document.querySelector('input[name="fileType"]:checked');
                if (selectedType) {
                    const endpoint = selectedType.value === 'patients' ? 
                        '/api/ingestion/patients/upload' : 
                        '/api/ingestion/medical-records/upload';
                    dropzone.options.url = endpoint;
                }
            }

            // Listen for file type changes
            fileTypeRadios.forEach(radio => {
                radio.addEventListener('change', updateUrl);
            });

            // Start upload button
            startButton.addEventListener('click', function() {
                if (dropzone.files.length === 0) {
                    showToast('warning', 'Please select a file to upload.');
                    return;
                }
                dropzone.processQueue();
            });

            // Cancel upload button
            cancelButton.addEventListener('click', function() {
                dropzone.removeAllFiles(true);
                document.getElementById('uploadProgress').classList.add('d-none');
            });

            // When file is added
            this.on('addedfile', function(file) {
                // Enable upload button when file is added
                startButton.disabled = false;
                cancelButton.disabled = false;
                
                // Update file info in progress card
                document.getElementById('fileName').textContent = file.name;
                document.getElementById('fileSize').textContent = (file.size / (1024 * 1024)).toFixed(2) + ' MB';
                
                // Show progress card
                const progressCard = document.getElementById('uploadProgress');
                progressCard.classList.remove('d-none');
                
                // Scroll to progress card
                setTimeout(() => {
                    progressCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                }, 100);
            });
            
            // When file is removed
            this.on('removedfile', function(file) {
                // Disable upload button when file is removed
                startButton.disabled = true;
                cancelButton.disabled = true;
                
                // Hide progress card if no files
                if (this.files.length === 0) {
                    document.getElementById('uploadProgress').classList.add('d-none');
                }
            });
            
            // When sending the file
            this.on('sending', function(file, xhr, formData) {
                // Add CSRF token
                const token = document.querySelector('input[name="_csrf"]').value;
                xhr.setRequestHeader('X-CSRF-TOKEN', token);
                
                // Update status
                const statusElement = document.getElementById('uploadStatus');
                statusElement.innerHTML = `
                    <i class="fas fa-spinner fa-spin me-2"></i>
                    <span>Uploading file...</span>
                `;
                
                // Clear previous details
                document.getElementById('uploadDetails').innerHTML = '';
            });
            
            // Update progress
            this.on('uploadprogress', function(file, progress) {
                // Update progress bar
                const progressBar = document.getElementById('progressBar');
                const percent = Math.round(progress);
                progressBar.style.width = percent + '%';
                progressBar.setAttribute('aria-valuenow', percent);
                
                // Update badge in header
                document.querySelector('#uploadProgress .badge').textContent = percent + '%';
            });
            
            // Handle successful upload
            this.on('success', function(file, response) {
                // Handle successful upload
                const statusElement = document.getElementById('uploadStatus');
                statusElement.innerHTML = `
                    <i class="fas fa-check-circle text-success me-2"></i>
                    <span>${response.message || 'File uploaded successfully!'}</span>
                `;
                
                // Disable upload button after successful upload
                startButton.disabled = true;
                
                // Show success message in details
                const details = document.getElementById('uploadDetails');
                details.innerHTML = `
                    <div class="alert bg-light border border-success border-opacity-25 text-success mb-0">
                        <i class="fas fa-check-circle me-2"></i>
                        <strong>Success!</strong> File has been queued for processing.
                    </div>
                `;
                
                // Redirect to history after delay
                setTimeout(() => {
                    window.location.href = '/ingestion/history';
                }, 2000);
            });
            
            // Handle upload error
            this.on('error', function(file, errorMessage) {
                // Handle upload error
                const statusElement = document.getElementById('uploadStatus');
                statusElement.innerHTML = `
                    <i class="fas fa-exclamation-circle text-danger me-2"></i>
                    <span>Upload failed: ${errorMessage || 'Unknown error'}</span>
                `;
                
                // Show error message in details
                const details = document.getElementById('uploadDetails');
                details.innerHTML = `
                    <div class="alert alert-danger mb-0">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        <strong>Error!</strong> ${errorMessage || 'An error occurred during upload.'}
                    </div>
                `;
            });
        }
    });
}

// Initialize file inputs
function initializeFileInputs() {
    initializeFileInput('fileInput', 'filePreview');
}

// Initialize everything when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeFileInputs();
    
    // If we're on the history page, initialize the data table
    if (typeof initializeHistoryTable === 'function') {
        initializeHistoryTable();
    }
});
