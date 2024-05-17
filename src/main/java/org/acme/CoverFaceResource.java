package org.acme;


import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;

import ai.djl.modality.cv.output.DetectedObjects;


import ai.djl.translate.TranslateException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Consumes;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.opencv.core.Point;

import java.io.IOException;

import java.util.List;

@Path("/cover")
public class CoverFaceResource {

    @Inject
    ImageFactory imageFactory;

    @Inject
    ImageProcessor imageProcessor;

    @Inject
    FaceDetectionService faceDetectionService;

    @Inject
    AgeDetectionService ageDetectionService;

    @Inject
    @Named("glasses")
    Image fachero;

    @Inject
    @Named("smile")
    Image smile;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] cover(@RestForm("picture") FileUpload picture) throws TranslateException, IOException {
        final Image image = getImage(picture);
        Image finalResult = predict(image);
        return this.imageProcessor.toByteArray(finalResult);
    }

    private Image getImage(FileUpload picture) throws IOException {
        final byte[] content = Files.readAllBytes(picture.uploadedFile());
        return this.imageFactory.fromInputStream(new ByteArrayInputStream(content));
    }

    public Image predict(Image originalImage) throws IOException, TranslateException {

        DetectedObjects detectedFaces = faceDetectionService.detectFaces(originalImage);

        List<DetectedObjects.DetectedObject> detectedFacesBounderies = detectedFaces.items();

        Image finalResult = originalImage;

        for (DetectedObjects.DetectedObject detection : detectedFacesBounderies) {
            Image face = this.imageProcessor.cropImage(originalImage, detection);
            Classifications age = ageDetectionService.age(face);

            Point p = this.imageProcessor.translateToPoint(originalImage, detection.getBoundingBox().getBounds());
            Image banner = getBanner(age);
            Image bannerWithCorrectSize = banner.resize(face.getWidth(), face.getHeight(), true);

            // Appends the emoji to one face

            finalResult = this.imageProcessor.overlayImage(finalResult, bannerWithCorrectSize, p);
        }

        this.imageProcessor.store("final.png", finalResult);
        return finalResult;
    }

    private Image getBanner(Classifications classifications) {
        String ageRange = classifications.best().getClassName();
        return switch(ageRange) {
            case "(0-2)" -> fachero;
            case "(4-6)" -> fachero;
            case "(8-12)" -> fachero;
            case "(15-20)" -> fachero;
            case "(25-32)" -> smile;
            case "(38-43)" -> smile;
            case "(48-53)" -> smile;
            case "(60-100)" -> smile;
            default -> smile;
        };
    }

}
