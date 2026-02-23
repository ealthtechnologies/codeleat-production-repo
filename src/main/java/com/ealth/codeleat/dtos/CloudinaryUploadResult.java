package com.ealth.codeleat.dtos;


public class CloudinaryUploadResult {
    private final String url;
    private final String publicId;

    public CloudinaryUploadResult(String url, String publicId) {
        this.url = url;
        this.publicId = publicId;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicId() {
        return publicId;
    }
}