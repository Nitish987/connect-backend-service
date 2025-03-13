package com.conceptune.connect.database.template;

import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageTemplate {

    @Autowired
    private Storage storage;

    @Value("${google.cloud.storage.bucket-name}")
    private String bucketName;

    public String upload(String filePath, String filename, String contentType, byte[] fileBytes) throws StorageException {
        BlobId blobId = BlobId.of(bucketName, filePath + "/" + filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        Blob blob = storage.create(blobInfo, fileBytes);
        return blob.getMediaLink();
    }
}
