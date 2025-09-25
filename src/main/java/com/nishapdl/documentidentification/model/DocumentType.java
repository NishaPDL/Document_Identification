package com.nishapdl.documentidentification.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Enumeration of supported Indian identity document types.
 * 
 * This enum provides a type-safe way to handle document classifications
 * and includes additional metadata for each document type.
 */
public enum DocumentType {
    AADHAAR("Aadhaar", "Indian national identity card issued by UIDAI", "aadhaar"),
    PAN("PAN", "Permanent Account Number card issued by Income Tax Department", "pan"),
    VOTER_ID("Voter ID", "Election Commission identity card", "voter_id"),
    DRIVING_LICENSE("Driving License", "Motor vehicle driving license", "driving_license"),
    PASSPORT("Passport", "Indian passport for international travel", "passport"),
    RATION_CARD("Ration Card", "Public distribution system card", "ration_card"),
    NONE("None", "Unrecognized or invalid document type", "none");

    private final String displayName;
    private final String description;
    private final String code;

    DocumentType(String displayName, String description, String code) {
        this.displayName = displayName;
        this.description = description;
        this.code = code;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    /**
     * Creates DocumentType from string value (case-insensitive).
     * 
     * @param value the string value to convert
     * @return corresponding DocumentType or NONE if not found
     */
    @JsonCreator
    public static DocumentType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        
        return Arrays.stream(values())
                .filter(type -> type.displayName.equalsIgnoreCase(value.trim()) || 
                               type.code.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(NONE);
    }

    /**
     * Checks if this document type is a valid identification document.
     * 
     * @return true if this is a valid document type (not NONE)
     */
    public boolean isValid() {
        return this != NONE;
    }

    /**
     * Checks if this document type is a government-issued ID.
     * 
     * @return true if this is a government-issued document
     */
    public boolean isGovernmentIssued() {
        return this == AADHAAR || this == PAN || this == VOTER_ID || 
               this == DRIVING_LICENSE || this == PASSPORT || this == RATION_CARD;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
