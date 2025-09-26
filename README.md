📄 Document Classifier using GCP Vertex AI (Gemini Model)

This repository provides a solution for automatically classifying all .doc or .docx files present in a folder using Google Cloud Platform's Vertex AI, specifically leveraging the powerful Gemini multimodal model for fast and intelligent processing.

By combining local file handling with cloud-based AI capabilities, this project offers a scalable, efficient, and accurate document classification pipeline suitable for enterprise-level or research applications.

🚀 Features

✅ Automatically detects and processes all .doc / .docx files in a target directory.

⚡ Fast and efficient classification using Google's Gemini model via Vertex AI.

☁️ Fully integrated with Google Cloud Vertex AI SDK.

🔄 Supports dynamic reloading with FastAPI + Uvicorn (useful during development).

📦 Easily extendable for other file formats or classification schemas.

🧠 How It Works

Document Discovery: The system scans a specified folder for .doc and .docx files.

Text Extraction: Each document's content is extracted (using python-docx or similar tools).

Classification with Gemini (Vertex AI):

Extracted text is sent to the Gemini model deployed via Vertex AI.

The model returns classification labels or tags based on content.

Result Handling: Classification results can be logged, stored, or passed on to other services.
