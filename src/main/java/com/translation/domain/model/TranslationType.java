package com.translation.domain.model;

public enum TranslationType {
    TEXT("text/plain"),
    DOCUMENT("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    IMAGE("image/jpeg", "image/png", "image/gif", "image/bmp"),
    HTML("text/html");

    private final String[] mimeTypes;

    TranslationType(String... mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String[] getMimeTypes() {
        return mimeTypes;
    }

    public static TranslationType fromMimeType(String mimeType) {
        for (TranslationType type : values()) {
            for (String mime : type.getMimeTypes()) {
                if (mime.equalsIgnoreCase(mimeType)) {
                    return type;
                }
            }
        }
        return TEXT; // default
    }
}
