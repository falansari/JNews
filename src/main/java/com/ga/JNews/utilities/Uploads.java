package com.ga.JNews.utilities;

import com.ga.JNews.exceptions.BadRequestException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Component utility class to handle files
 */
@Component
public class Uploads {

    /**
     * Upload an image.
     * @param uploadPath Path resolved filepath including storage location + file name.
     * @param image MultipartFile JPEG, PNG only
     * @return String new uploaded file's name
     * @exception BadRequestException invalid image
     */
    public String uploadImage(String uploadPath, MultipartFile image) {

        if (image.isEmpty()) throw new BadRequestException("Image is empty");

        String fileType = image.getContentType();

        if (!Objects.equals(fileType, "image/jpeg")
                && !Objects.equals(fileType, "image/png")) {
            throw new BadRequestException("Invalid image file type. Only .PNG and .JPEG allowed");
        }

        return upload(uploadPath, image);
    }

    /**
     * Upload an HTML file.
     * @param uploadPath Path resolved filepath including storage location + file name.
     * @param document MultipartFile .html file only
     * @return String new uploaded file's name
     * @exception BadRequestException Bad upload request handling
     */
    public String uploadHtmlFile(String uploadPath, MultipartFile document) {

        if (document.isEmpty()) throw new BadRequestException("File is empty");

        String fileType = document.getContentType();

        if (!Objects.equals(fileType, "text/html")) {
            throw new BadRequestException("Invalid file type. Only .html and .htm allowed");
        }

        return upload(uploadPath, document);
    }

    /**
     * Upload a plain text file.
     * @param uploadPath Path resolved filepath including storage location + file name.
     * @param document MultipartFile .txt file only
     * @return String new uploaded file's name
     * @exception BadRequestException Bad upload request handling
     */
    public String uploadTextFile(String uploadPath, MultipartFile document) {

        if (document.isEmpty()) throw new BadRequestException("File is empty");

        String fileType = document.getContentType();

        if (!Objects.equals(fileType, "text/plain")) {
            throw new BadRequestException("Invalid file type. Only .txt allowed");
        }

        return upload(uploadPath, document);
    }

    /**
     * Download a stored image file.
     * @param uploadPath String Image's upload path [cpr-images, model-images, car-images]
     * @param fileName String image's stored filename in database
     * @return ResponseEntity Resource the image.
     */
    public ResponseEntity<Resource> downloadFile(String uploadPath, String fileName) {
        try {
            Path path = Paths.get(uploadPath, fileName);
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)))
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete an existing image from storage.
     * @param uploadPath String File's upload path ["uploads/cpr-images", "uploads/model-images", ...]
     * @param filename String name of the stored file
     */
    public void deleteFile(String uploadPath, String filename) {
        try {
            Path path = Paths.get(uploadPath, filename);

            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    /**
     * Upload a file to the server.
     * @param uploadPath String Path to upload file to.
     * @param file MultipartFile File to upload
     * @return String Uploaded file's name
     */
    private String upload(String uploadPath, MultipartFile file) {

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(uploadPath);

            Files.createDirectories(path);

            Files.copy(
                    file.getInputStream(),
                    path.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
