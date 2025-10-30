// API endpoints
const API_BASE_URL = '/api/patients';

/**
 * Fetch all patients
 * @returns {Promise<Array>} List of patients
 */
export const getPatients = async () => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch patients');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching patients:', error);
        throw error;
    }
};

/**
 * Create a new patient
 * @param {Object} patientData - Patient data to create
 * @returns {Promise<Object>} Created patient data
 */
export const createPatient = async (patientData) => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify(patientData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create patient');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error creating patient:', error);
        throw error;
    }
};

/**
 * Update an existing patient
 * @param {string} id - Patient ID
 * @param {Object} patientData - Updated patient data
 * @returns {Promise<Object>} Updated patient data
 */
export const updatePatient = async (id, patientData) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            },
            body: JSON.stringify(patientData)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to update patient');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error updating patient:', error);
        throw error;
    }
};

/**
 * Delete a patient
 * @param {string} id - Patient ID
 * @returns {Promise<void>}
 */
export const deletePatient = async (id) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to delete patient');
        }
    } catch (error) {
        console.error('Error deleting patient:', error);
        throw error;
    }
};

/**
 * Get patient by ID
 * @param {string} id - Patient ID
 * @returns {Promise<Object>} Patient data
 */
export const getPatientById = async (id) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch patient');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching patient:', error);
        throw error;
    }
};
