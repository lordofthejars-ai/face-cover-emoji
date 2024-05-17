package org.acme;


import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;

import ai.djl.modality.cv.output.DetectedObjects;

import ai.djl.opencv.OpenCVImageFactory;

import ai.djl.translate.TranslateException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.opencv.core.Point;

import java.io.IOException;

import java.nio.file.Paths;
import java.util.List;

@Path("/hello")
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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws TranslateException, IOException {
        Image finalResult = predict();

        this.imageProcessor.store("final.png", finalResult);
        return null;
    }

    public Image predict() throws IOException, TranslateException {
        java.nio.file.Path facePath = Paths.get("src/test/resources/baby.jpg");
        Image originalImage = OpenCVImageFactory.getInstance().fromFile(facePath);

        DetectedObjects detectedFaces = faceDetectionService.detectFaces(originalImage);

        List<DetectedObjects.DetectedObject> detectedFacesBounderies = detectedFaces.items();

        Image finalResult = originalImage;

        for (DetectedObjects.DetectedObject detection : detectedFacesBounderies) {
            Image face = this.imageProcessor.cropImage(originalImage, detection);
            Classifications age = ageDetectionService.age(face);

            Point p = this.imageProcessor.translateToPoint(originalImage, detection.getBoundingBox().getBounds());
            Image banner = getBanner(age);
            Image bannerWithCorrectSize = banner.resize(face.getWidth(), face.getHeight(), true);
            finalResult = this.imageProcessor.overlayImage(finalResult, bannerWithCorrectSize, p);
        }

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
