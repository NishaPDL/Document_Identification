import os
import zipfile
import json
import tempfile
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from typing import List
from google.cloud import vision
from vertexai.preview.generative_models import GenerativeModel
from google.cloud import aiplatform

# === CONFIG ===
PROJECT_ID = "pdlgcpcloud"
LOCATION = "us-central1"
GEMINI_MODEL = "gemini-2.0-flash"

# === Init Vertex AI ===
try:
    aiplatform.init(project=PROJECT_ID, location=LOCATION)
except Exception as e:
    print(f"Failed to initialize Vertex AI: {e}")
    exit(1)

app = FastAPI(title="Document Classifier API")

# === Utils ===

def extract_images_from_zip(zip_path, extract_to):
    image_exts = {".png", ".jpg", ".jpeg", ".webp", ".bmp"}
    extracted_files = []

    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall(extract_to)

    for root, _, files in os.walk(extract_to):
        for file in files:
            if os.path.splitext(file)[1].lower() in image_exts:
                extracted_files.append(os.path.join(root, file))

    return extracted_files

def extract_text_from_image(image_path):
    client = vision.ImageAnnotatorClient()
    with open(image_path, "rb") as img_file:
        content = img_file.read()

    image = vision.Image(content=content)
    response = client.text_detection(image=image)

    if response.error.message:
        raise Exception(f"OCR failed: {response.error.message}")

    return response.full_text_annotation.text.strip()

def classify_document_type(text):
    model = GenerativeModel(GEMINI_MODEL)

    prompt = f"""
You are an assistant that classifies Indian identity documents based on OCR-extracted text.

Respond with only ONE of the following:
- Aadhaar
- PAN
- Voter ID
- Driving License
- None

Here is the extracted text:
{text}

What type of document is this? Respond with only one word.
"""
    response = model.generate_content(prompt)
    return response.text.strip()

# === Endpoint ===

@app.post("/classify-documents")
async def classify_documents(file: UploadFile = File(...)):
    if not file.filename.endswith(".zip"):
        raise HTTPException(status_code=400, detail="Only ZIP files are allowed.")

    with tempfile.TemporaryDirectory() as temp_dir:
        zip_path = os.path.join(temp_dir, "uploaded.zip")
        extract_dir = os.path.join(temp_dir, "extracted")

        # Save uploaded file
        with open(zip_path, "wb") as f:
            f.write(await file.read())

        os.makedirs(extract_dir, exist_ok=True)

        try:
            images = extract_images_from_zip(zip_path, extract_dir)
            if not images:
                raise HTTPException(status_code=400, detail="No image files found in ZIP.")

            results = {}
            for img_path in images:
                filename = os.path.basename(img_path)
                try:
                    text = extract_text_from_image(img_path)
                    doc_type = classify_document_type(text)
                    results[filename] = doc_type
                except Exception as e:
                    results[filename] = f"Error: {str(e)}"

            return JSONResponse(content=results)

        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))
