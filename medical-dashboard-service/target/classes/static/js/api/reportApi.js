// API endpoints
const API_BASE_URL = '/api/reports';

/**
 * Fetch all reports
 * @returns {Promise<Array>} List of reports
 */
export const getReports = async () => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch reports');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching reports:', error);
        throw error;
    }
};

/**
 * Generate a new report
 * @param {string} reportType - Type of report to generate
 * @param {Object} filters - Report filters
 * @returns {Promise<Object>} Generated report data
 */
export const generateReport = async (reportType, filters = {}) => {
    try {
        const response = await fetch(`${API_BASE_URL}/generate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify({ reportType, ...filters })
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to generate report');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error generating report:', error);
        throw error;
    }
};

/**
 * Get report by ID
 * @param {string} id - Report ID
 * @returns {Promise<Object>} Report data
 */
export const getReportById = async (id) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch report');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching report:', error);
        throw error;
    }
};

/**
 * Delete a report
 * @param {string} id - Report ID
 * @returns {Promise<void>}
 */
export const deleteReport = async (id) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to delete report');
        }
    } catch (error) {
        console.error('Error deleting report:', error);
        throw error;
    }
};

/**
 * Get report statistics
 * @returns {Promise<Object>} Report statistics
 */
export const getReportStats = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch report statistics');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching report statistics:', error);
        throw error;
    }
};
