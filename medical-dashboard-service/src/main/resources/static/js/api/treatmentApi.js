// API endpoints
const API_BASE_URL = '/api/treatments';

/**
 * Fetch all treatments
 * @returns {Promise<Array>} List of treatments
 */
export const getTreatments = async () => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch treatments');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching treatments:', error);
        throw error;
    }
};

/**
 * Create a new treatment
 * @param {Object} treatmentData - Treatment data to create
 * @returns {Promise<Object>} Created treatment data
 */
export const createTreatment = async (treatmentData) => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify(treatmentData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create treatment');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error creating treatment:', error);
        throw error;
    }
};

/**
 * Update an existing treatment
 * @param {string} id - Treatment ID
 * @param {Object} treatmentData - Updated treatment data
 * @returns {Promise<Object>} Updated treatment data
 */
export const updateTreatment = async (id, treatmentData) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify(treatmentData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to update treatment');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error updating treatment:', error);
        throw error;
    }
};

/**
 * Delete a treatment
 * @param {string} id - Treatment ID
 * @returns {Promise<void>}
 */
export const deleteTreatment = async (id) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to delete treatment');
        }
    } catch (error) {
        console.error('Error deleting treatment:', error);
        throw error;
    }
};

/**
 * Get treatment by ID
 * @param {string} id - Treatment ID
 * @returns {Promise<Object>} Treatment data
 */
export const getTreatmentById = async (id) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch treatment');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching treatment:', error);
        throw error;
    }
};

/**
 * Get treatment statistics
 * @returns {Promise<Object>} Treatment statistics
 */
export const getTreatmentStats = async () => {
    try {
        const response = await fetch(`${API_BASE_URL}/stats`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch treatment statistics');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching treatment statistics:', error);
        throw error;
    }
};
