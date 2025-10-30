/**
 * history.js - Handles the import history page functionality
 */

// Configuration
const config = {
    apiEndpoints: {
        history: '/api/ingestion/history',
        importDetails: (id) => `/api/ingestion/history/${id}`,
        deleteImport: (id) => `/api/ingestion/history/${id}`,
        downloadReport: (id) => `/api/ingestion/history/${id}/report`
    },
    refreshInterval: 30000, // 30 seconds
    defaultPageSize: 10
};

// DOM Elements
const elements = {
    table: null,
    modal: null,
    modalElement: document.getElementById('importDetailsModal'),
    refreshBtn: document.getElementById('refreshBtn'),
    downloadReportBtn: document.getElementById('downloadReportBtn'),
    filterOptions: document.querySelectorAll('.filter-option')
};

/**
 * Initialize the history page
 */
function initHistoryPage() {
    if (!elements.modalElement) return;
    
    // Initialize DataTable
    initDataTable();
    
    // Initialize modal
    elements.modal = new bootstrap.Modal(elements.modalElement);
    
    // Set up event listeners
    setupEventListeners();
    
    // Set up auto-refresh
    setupAutoRefresh();
}

/**
 * Initialize the DataTable
 */
function initDataTable() {
    elements.table = $('#importsTable').DataTable({
        processing: true,
        serverSide: false,
        ajax: {
            url: config.apiEndpoints.history,
            type: 'GET',
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            },
            dataSrc: ''
        },
        columns: [
            { data: 'id' },
            { 
                data: 'fileName',
                render: formatFileName
            },
            { 
                data: 'fileType',
                render: formatFileType
            },
            { 
                data: 'status',
                render: formatStatus
            },
            { 
                data: 'recordCount',
                render: formatRecordCount
            },
            { 
                data: 'createdAt',
                render: formatDate
            },
            {
                data: null,
                orderable: false,
                render: renderActionButtons
            }
        ],
        order: [[5, 'desc']],
        responsive: true,
        pageLength: config.defaultPageSize,
        language: {
            search: "_INPUT_",
            searchPlaceholder: "Search imports...",
            lengthMenu: "Show _MENU_ entries per page",
            info: "Showing _START_ to _END_ of _TOTAL_ entries",
            infoEmpty: "No entries found",
            infoFiltered: "(filtered from _MAX_ total entries)",
            zeroRecords: "No matching records found"
        },
        dom: `
            <"row"<"col-md-6"l><"col-md-6"f>>
            <"row"<"col-12"tr>>
            <"row"<"col-md-5"i><"col-md-7"p>>
        `
    });
}

/**
 * Set up event listeners
 */
function setupEventListeners() {
    // Handle view details button
    $('#importsTable tbody').on('click', '.btn-view', function() {
        const importId = $(this).data('id');
        showImportDetails(importId);
    });

    // Handle delete button
    $('#importsTable tbody').on('click', '.btn-delete', function() {
        const importId = $(this).data('id');
        if (confirm('Are you sure you want to delete this import record?')) {
            deleteImport(importId);
        }
    });

    // Handle filter buttons
    elements.filterOptions.forEach(option => {
        option.addEventListener('click', function(e) {
            e.preventDefault();
            const status = this.dataset.status;
            
            // Update active state
            elements.filterOptions.forEach(opt => opt.classList.remove('active'));
            this.classList.add('active');
            
            // Apply filter
            if (status === 'all') {
                elements.table.column(3).search('').draw();
            } else {
                elements.table.column(3).search('^' + status + '$', true, false).draw();
            }
        });
    });

    // Handle refresh button
    elements.refreshBtn.addEventListener('click', function() {
        refreshTable();
    });

    // Handle download report button
    if (elements.downloadReportBtn) {
        elements.downloadReportBtn.addEventListener('click', function() {
            const importId = document.getElementById('importId').textContent;
            if (importId && importId !== '-') {
                const downloadUrl = config.apiEndpoints.downloadReport(importId);
                const link = document.createElement('a');
                link.href = downloadUrl;
                link.download = `import-report-${importId}.csv`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        });
    }
}

/**
 * Set up auto-refresh for processing imports
 */
function setupAutoRefresh() {
    setInterval(() => {
        const hasProcessing = elements.table.column(3).data().toArray()
            .some(status => status === 'PROCESSING' || status === 'QUEUED');
            
        if (hasProcessing) {
            refreshTable();
        }
    }, config.refreshInterval);
}

/**
 * Refresh the data table
 */
function refreshTable() {
    const $refreshBtn = $(elements.refreshBtn);
    const originalContent = $refreshBtn.html();
    
    // Show loading state
    $refreshBtn.prop('disabled', true)
              .html('<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Refreshing...');
    
    // Refresh the table
    elements.table.ajax.reload(() => {
        // Restore button state
        $refreshBtn.prop('disabled', false).html(originalContent);
    }, false); // false means don't reset paging
}

/**
 * Show import details in a modal
 * @param {string} importId - The ID of the import to show details for
 */
async function showImportDetails(importId) {
    if (!importId) return;
    
    const modalBody = document.querySelector('#importDetailsModal .modal-body');
    const originalContent = modalBody.innerHTML;
    
    try {
        // Show loading state
        modalBody.innerHTML = `
            <div class="d-flex justify-content-center my-5">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `;
        
        // Show the modal
        elements.modal.show();
        
        // Fetch import details
        const response = await fetch(config.apiEndpoints.importDetails(importId), {
            headers: {
                'X-CSRF-TOKEN': getCsrfToken()
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Update modal with data
        updateModalContent(data);
        
    } catch (error) {
        console.error('Error fetching import details:', error);
        modalBody.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle me-2"></i>
                Failed to load import details. Please try again.
            </div>
            <div class="text-end">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
        `;
    }
}

/**
 * Update the modal content with import details
 * @param {Object} data - The import details data
 */
function updateModalContent(data) {
    // Update basic info
    document.getElementById('importId').textContent = data.id || '-';
    document.getElementById('fileName').textContent = data.fileName || '-';
    document.getElementById('fileType').textContent = data.fileType ? 
        data.fileType.charAt(0).toUpperCase() + data.fileType.slice(1) : '-';
    
    // Update status badge
    const statusBadge = document.getElementById('statusBadge');
    statusBadge.className = 'badge ' + getStatusClass(data.status);
    statusBadge.innerHTML = `<i class="fas ${getStatusIcon(data.status)} me-1"></i> ${data.status || '-'}`;
    
    // Update user and timestamp
    document.getElementById('uploadedBy').textContent = data.uploadedBy || 'System';
    document.getElementById('uploadedAt').textContent = formatDate(data.createdAt);
    
    // Calculate statistics
    const totalRecords = data.recordCount || 0;
    const errorCount = data.errorCount || 0;
    const successCount = Math.max(0, totalRecords - errorCount);
    const progress = totalRecords > 0 ? Math.round((successCount / totalRecords) * 100) : 0;
    
    // Update statistics
    document.getElementById('totalRecords').textContent = totalRecords;
    document.getElementById('successCount').textContent = successCount;
    document.getElementById('errorCount').textContent = errorCount;
    
    // Update progress bar
    const progressBar = document.getElementById('progressBar');
    progressBar.className = 'progress-bar progress-bar-striped ' + 
                          (data.status === 'COMPLETED' ? 'bg-success' : 
                           data.status === 'FAILED' ? 'bg-danger' : 'progress-bar-animated bg-warning');
    progressBar.style.width = progress + '%';
    progressBar.setAttribute('aria-valuenow', progress);
    progressBar.textContent = progress + '%';
    
    // Update error logs
    const errorLogs = document.getElementById('errorLogs');
    if (data.errorMessages && data.errorMessages.length > 0) {
        let errorHtml = '<ul class="mb-0">';
        data.errorMessages.forEach(error => {
            errorHtml += `<li>${error}</li>`;
        });
        errorHtml += '</ul>';
        errorLogs.innerHTML = errorHtml;
    } else {
        errorLogs.innerHTML = '<p class="text-muted mb-0">No errors found.</p>';
    }
    
    // Show/hide download button based on status
    if (elements.downloadReportBtn) {
        elements.downloadReportBtn.style.display = 
            (data.status === 'COMPLETED' || data.status === 'FAILED') ? 'inline-block' : 'none';
    }
}

/**
 * Delete an import
 * @param {string} importId - The ID of the import to delete
 */
async function deleteImport(importId) {
    if (!importId) return;
    
    try {
        const response = await fetch(config.apiEndpoints.deleteImport(importId), {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': getCsrfToken(),
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        // Show success message
        showToast('success', 'Import deleted successfully');
        
        // Refresh the table
        refreshTable();
        
    } catch (error) {
        console.error('Error deleting import:', error);
        showToast('danger', 'Failed to delete import. Please try again.');
    }
}

/**
 * Format file name
 */
function formatFileName(data, type, row) {
    return data || '-';
}

/**
 * Format file type
 */
function formatFileType(data) {
    const typeMap = {
        'patients': { class: 'bg-info', icon: 'users', label: 'Patients' },
        'medical-records': { class: 'bg-primary', icon: 'file-medical', label: 'Medical Records' }
    };
    
    const typeInfo = typeMap[data] || { class: 'bg-secondary', icon: 'file', label: data || 'Unknown' };
    return `<span class="badge ${typeInfo.class}">
        <i class="fas ${typeInfo.icon} me-1"></i> ${typeInfo.label}
    </span>`;
}

/**
 * Format status
 */
function formatStatus(data) {
    return `<span class="badge ${getStatusClass(data)}">
        <i class="fas ${getStatusIcon(data)} me-1"></i> ${data || '-'}
    </span>`;
}

/**
 * Get status class
 */
function getStatusClass(status) {
    const statusMap = {
        'COMPLETED': 'bg-success',
        'FAILED': 'bg-danger',
        'PROCESSING': 'bg-warning',
        'QUEUED': 'bg-info'
    };
    return statusMap[status] || 'bg-secondary';
}

/**
 * Get status icon
 */
function getStatusIcon(status) {
    const iconMap = {
        'COMPLETED': 'fa-check-circle',
        'FAILED': 'fa-times-circle',
        'PROCESSING': 'fa-spinner fa-spin',
        'QUEUED': 'fa-clock'
    };
    return iconMap[status] || 'fa-question-circle';
}

/**
 * Format record count
 */
function formatRecordCount(data, type, row) {
    if (row.status === 'COMPLETED' || row.status === 'FAILED') {
        return `${row.processedRecords || 0} / ${data || 0}`;
    }
    return data || '-';
}

/**
 * Format date
 */
function formatDate(data) {
    if (!data) return '-';
    return new Date(data).toLocaleString();
}

/**
 * Render action buttons
 */
function renderActionButtons(data, type, row) {
    return `
        <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-primary btn-view" data-id="${row.id}" title="View Details">
                <i class="fas fa-eye"></i>
            </button>
            ${row.status === 'COMPLETED' || row.status === 'FAILED' ? `
                <a href="${config.apiEndpoints.downloadReport(row.id)}" class="btn btn-outline-success" title="Download Report">
                    <i class="fas fa-download"></i>
                </a>
            ` : ''}
            ${row.status !== 'PROCESSING' && row.status !== 'QUEUED' ? `
                <button class="btn btn-outline-danger btn-delete" data-id="${row.id}" title="Delete">
                    <i class="fas fa-trash"></i>
                </button>
            ` : ''}
        </div>
    `;
}

/**
 * Get CSRF token
 */
function getCsrfToken() {
    return document.querySelector('input[name="_csrf"]').value;
}

/**
 * Show a toast message
 * @param {string} type - The type of toast (success, danger, warning, info)
 * @param {string} message - The message to display
 */
function showToast(type, message) {
    // Create toast element
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
    
    // Add toast to container
    const toastContainer = document.getElementById('toastContainer');
    if (toastContainer) {
        toastContainer.insertAdjacentHTML('beforeend', toastHtml);
        
        // Initialize and show the toast
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement, {
            autohide: true,
            delay: 5000
        });
        
        toast.show();
        
        // Remove toast after it's hidden
        toastElement.addEventListener('hidden.bs.toast', function() {
            toastElement.remove();
        });
    }
}

// Initialize the page when the DOM is fully loaded
document.addEventListener('DOMContentLoaded', initHistoryPage);
