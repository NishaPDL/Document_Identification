/**
 * HRMS Document Upload Integration
 * Integrates with Java Document Classification Service
 * For use with https://predemoui.paisalo.in:4022/hrms/Employee
 */

class DocumentUploadIntegration {
    constructor(config = {}) {
        this.apiBaseUrl = config.apiBaseUrl || 'http://localhost:8080/api';
        this.employeeIdField = config.employeeIdField || 'employeeId';
        this.debug = config.debug || false;
    }

    /**
     * Initialize the integration by attaching event listeners
     */
    init() {
        this.log('Initializing Document Upload Integration...');
        
        // Find the Aadhaar file input
        const aadhaarInput = document.getElementById('employeeaadhar');
        if (!aadhaarInput) {
            console.error('Employee Aadhaar file input not found (ID: employeeaadhar)');
            return;
        }

        // Add change event listener
        aadhaarInput.addEventListener('change', (event) => {
            this.handleAadhaarUpload(event);
        });

        // Add visual feedback elements
        this.createUploadFeedbackElements(aadhaarInput);
        
        this.log('Integration initialized successfully');
    }

    /**
     * Handle Aadhaar document upload
     */
    async handleAadhaarUpload(event) {
        const file = event.target.files[0];
        if (!file) return;

        this.log('Aadhaar file selected:', file.name);
        
        try {
            // Show loading state
            this.showUploadStatus('uploading', 'Processing Aadhaar document...');
            
            // Get employee ID
            const employeeId = this.getEmployeeId();
            if (!employeeId) {
                throw new Error('Employee ID not found. Please ensure employee information is loaded.');
            }

            // Validate file type
            if (!this.isValidImageFile(file)) {
                throw new Error('Please select a valid image file (PNG, JPG, JPEG, WebP, BMP)');
            }

            // Create ZIP file containing the Aadhaar document
            const zipFile = await this.createZipFile(file);
            
            // Upload and classify
            const result = await this.uploadAndClassify(zipFile, employeeId);
            
            // Process result
            this.handleUploadResult(result, file.name);
            
        } catch (error) {
            this.log('Upload error:', error);
            this.showUploadStatus('error', error.message);
        }
    }

    /**
     * Get employee ID from the page
     */
    getEmployeeId() {
        // Try multiple ways to get employee ID
        
        // Method 1: Look for a field with employeeId
        let employeeIdElement = document.getElementById(this.employeeIdField) || 
                               document.querySelector(`[name="${this.employeeIdField}"]`);
        
        if (employeeIdElement && employeeIdElement.value) {
            return employeeIdElement.value;
        }

        // Method 2: Look for employee code field
        employeeIdElement = document.getElementById('employeeCode') || 
                           document.querySelector('[name="employeeCode"]');
        
        if (employeeIdElement && employeeIdElement.value) {
            return employeeIdElement.value;
        }

        // Method 3: Look for any input with 'employee' in name/id
        const employeeFields = document.querySelectorAll('input[id*="employee"], input[name*="employee"]');
        for (const field of employeeFields) {
            if (field.value && field.value.trim()) {
                this.log('Using employee ID from field:', field.id || field.name, '=', field.value);
                return field.value.trim();
            }
        }

        // Method 4: Generate temporary ID based on timestamp
        const tempId = `emp_${Date.now()}`;
        this.log('No employee ID found, using temporary ID:', tempId);
        return tempId;
    }

    /**
     * Validate if file is a supported image format
     */
    isValidImageFile(file) {
        const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp', 'image/bmp'];
        const validExtensions = ['.png', '.jpg', '.jpeg', '.webp', '.bmp'];
        
        const isValidType = validTypes.includes(file.type.toLowerCase());
        const isValidExtension = validExtensions.some(ext => 
            file.name.toLowerCase().endsWith(ext)
        );
        
        return isValidType || isValidExtension;
    }

    /**
     * Create ZIP file containing the single document
     */
    async createZipFile(file) {
        // For browser compatibility, we'll send the single file directly
        // The Java service can be modified to accept single files for Aadhaar
        return file;
    }

    /**
     * Upload and classify document
     */
    async uploadAndClassify(file, employeeId) {
        const formData = new FormData();
        
        // If it's a single file, create a simple FormData
        if (file instanceof File) {
            formData.append('file', file);
        } else {
            formData.append('file', file);
        }
        
        formData.append('userId', employeeId);

        const response = await fetch(`${this.apiBaseUrl}/classify-and-upload-single`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    /**
     * Handle upload result
     */
    handleUploadResult(result, fileName) {
        this.log('Upload result:', result);

        if (result.classification === 'Aadhaar' && result.uploaded) {
            this.showUploadStatus('success', 
                `✅ Aadhaar document verified and uploaded successfully!<br>
                 <small>Stored at: ${result.path}</small>`
            );
            
            // Store the upload path for form submission
            this.storeUploadPath(result.path);
            
        } else if (result.classification !== 'Aadhaar') {
            this.showUploadStatus('warning', 
                `⚠️ Document classified as "${result.classification}" instead of Aadhaar.<br>
                 <small>Please upload a valid Aadhaar document.</small>`
            );
        } else if (!result.uploaded) {
            this.showUploadStatus('error', 
                `❌ Upload failed: ${result.reason || result.error || 'Unknown error'}`
            );
        }
    }

    /**
     * Store upload path in a hidden field for form submission
     */
    storeUploadPath(path) {
        let hiddenField = document.getElementById('aadhaarUploadPath');
        if (!hiddenField) {
            hiddenField = document.createElement('input');
            hiddenField.type = 'hidden';
            hiddenField.id = 'aadhaarUploadPath';
            hiddenField.name = 'aadhaarUploadPath';
            document.body.appendChild(hiddenField);
        }
        hiddenField.value = path;
        this.log('Stored upload path:', path);
    }

    /**
     * Create visual feedback elements
     */
    createUploadFeedbackElements(inputElement) {
        // Create status div
        const statusDiv = document.createElement('div');
        statusDiv.id = 'aadhaarUploadStatus';
        statusDiv.style.cssText = `
            margin-top: 10px;
            padding: 10px;
            border-radius: 4px;
            display: none;
            font-size: 14px;
        `;
        
        // Insert after the file input
        inputElement.parentNode.insertBefore(statusDiv, inputElement.nextSibling);
    }

    /**
     * Show upload status with visual feedback
     */
    showUploadStatus(type, message) {
        const statusDiv = document.getElementById('aadhaarUploadStatus');
        if (!statusDiv) return;

        const styles = {
            uploading: { bg: '#e3f2fd', border: '#2196f3', color: '#1976d2' },
            success: { bg: '#e8f5e8', border: '#4caf50', color: '#2e7d32' },
            error: { bg: '#ffebee', border: '#f44336', color: '#c62828' },
            warning: { bg: '#fff3e0', border: '#ff9800', color: '#ef6c00' }
        };

        const style = styles[type] || styles.error;
        
        statusDiv.style.display = 'block';
        statusDiv.style.backgroundColor = style.bg;
        statusDiv.style.border = `1px solid ${style.border}`;
        statusDiv.style.color = style.color;
        statusDiv.innerHTML = message;

        // Auto-hide success messages after 5 seconds
        if (type === 'success') {
            setTimeout(() => {
                statusDiv.style.display = 'none';
            }, 5000);
        }
    }

    /**
     * Debug logging
     */
    log(...args) {
        if (this.debug) {
            console.log('[DocumentUpload]', ...args);
        }
    }
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Configuration - adjust these values as needed
    const config = {
        apiBaseUrl: 'http://localhost:8080/api', // Change to your Java service URL
        employeeIdField: 'employeeId', // Change to match your employee ID field
        debug: true // Set to false in production
    };

    const integration = new DocumentUploadIntegration(config);
    integration.init();
});

// Export for manual initialization if needed
window.DocumentUploadIntegration = DocumentUploadIntegration;
