# 🏢 HRMS Document Upload Integration

This integration allows your HRMS Employee page to automatically classify and upload Aadhaar documents using the Java Document Classification Service.

## 📋 Overview

The integration provides:
- **Automatic Document Classification**: Verifies uploaded files are Aadhaar documents
- **Organized Storage**: Uploads to `/uploads/{employeeId}/aadhaar/` folder structure
- **Real-time Feedback**: Shows success/error messages to users
- **Form Integration**: Stores upload path for form submission

## 🚀 Quick Setup

### 1. Include the JavaScript File

Add this to your HRMS Employee page:

```html
<script src="document-upload-integration.js"></script>
```

### 2. Configure the Integration

Update the configuration in the JavaScript file:

```javascript
const config = {
    apiBaseUrl: 'http://your-java-service:8080/api', // Your Java service URL
    employeeIdField: 'employeeId', // Your employee ID field name
    debug: false // Set to true for development
};
```

### 3. Ensure Correct HTML Structure

Your file input should have the ID `employeeaadhar`:

```html
<input type="file" id="employeeaadhar" name="employeeaadhar" accept="image/*">
```

## 🔧 How It Works

1. **File Selection**: User selects an Aadhaar document
2. **Validation**: System validates it's an image file
3. **Classification**: Document is sent to Java service for OCR and classification
4. **Verification**: System verifies it's classified as "Aadhaar"
5. **Upload**: Document is uploaded to organized folder structure
6. **Feedback**: User sees success/error message
7. **Storage**: Upload path is stored in hidden field for form submission

## 📁 Folder Structure

Documents are organized as:

```
/uploads/
├── EMP001/
│   └── aadhaar/
│       └── aadhaar_document_1640995200000.jpg
├── EMP002/
│   └── aadhaar/
│       └── employee_aadhaar_1640995201000.png
└── ...
```

## 🎯 User Experience

### ✅ Successful Upload
- Green success message: "✅ Aadhaar document verified and uploaded successfully!"
- Shows storage path
- Hidden field `aadhaarUploadPath` contains the file path

### ⚠️ Wrong Document Type
- Orange warning: "⚠️ Document classified as 'PAN' instead of Aadhaar"
- Prompts user to upload correct document type

### ❌ Upload Error
- Red error message with specific error details
- Common errors: invalid file type, network issues, classification failures

## 🔌 API Integration

### Endpoint Used
- `POST /api/classify-and-upload-single`

### Request Format
```javascript
FormData {
    file: [File object],
    userId: "EMP001"
}
```

### Response Format
```json
{
    "classification": "Aadhaar",
    "uploaded": true,
    "path": "/uploads/EMP001/aadhaar/document_1640995200000.jpg"
}
```

## 🛠️ Customization

### Employee ID Detection

The integration automatically detects employee ID from multiple sources:

1. Field with ID matching `employeeIdField` config
2. Field with ID `employeeCode`
3. Any field with "employee" in the name/ID
4. Generates temporary ID if none found

### Custom Configuration

```javascript
const integration = new DocumentUploadIntegration({
    apiBaseUrl: 'https://your-domain.com/api',
    employeeIdField: 'empId', // Custom employee ID field
    debug: true // Enable debug logging
});

integration.init();
```

## 🔍 Troubleshooting

### Common Issues

1. **File Input Not Found**
   - Ensure your file input has ID `employeeaadhar`
   - Check browser console for error messages

2. **API Connection Failed**
   - Verify `apiBaseUrl` in configuration
   - Check if Java service is running
   - Ensure CORS is configured for your domain

3. **Employee ID Not Found**
   - Check `employeeIdField` configuration
   - Ensure employee ID field has a value
   - Enable debug mode to see ID detection process

4. **Classification Errors**
   - Ensure uploaded file is a clear image
   - Check if Google Cloud credentials are configured
   - Verify Gemini API access

### Debug Mode

Enable debug mode to see detailed logging:

```javascript
const config = {
    debug: true
};
```

Check browser console for detailed logs.

## 📊 Form Integration

After successful upload, access the file path:

```javascript
// Get the upload path
const uploadPath = document.getElementById('aadhaarUploadPath').value;

// Include in form submission
const formData = new FormData();
formData.append('employeeId', employeeId);
formData.append('aadhaarPath', uploadPath);
```

## 🔒 Security Considerations

- Files are validated on both client and server side
- Only image files are accepted
- Documents are classified before upload
- Temporary files are cleaned up after processing
- Upload paths are sanitized

## 📞 Support

For issues or questions:
1. Check the browser console for error messages
2. Enable debug mode for detailed logging
3. Verify Java service is running and accessible
4. Check network connectivity and CORS configuration

## 🎯 Example Implementation

See `hrms-integration-example.html` for a complete working example that demonstrates the integration in action.
