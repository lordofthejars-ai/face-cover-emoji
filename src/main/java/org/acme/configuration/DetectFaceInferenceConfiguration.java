package org.acme.configuration;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class DetectFaceInferenceConfiguration {

    private ZooModel<Image, DetectedObjects> model;

    @Startup
    public void initializeModel() throws ModelNotFoundException, MalformedModelException, IOException {

        double confThresh = 0.85f;
        double nmsThresh = 0.45f;
        double[] variance = {0.1f, 0.2f};
        int topK = 5000;
        int[][] scales = {{10, 16, 24}, {32, 48}, {64, 96}, {128, 192, 256}};
        int[] steps = {8, 16, 32, 64};

        final FaceDetectionTranslator faceDetectionTranslator = new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);

        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls("https://resources.djl.ai/test-models/pytorch/ultranet.zip")
                        .optTranslator(faceDetectionTranslator)
                        .optProgress(new ProgressBar())
                        .optEngine("PyTorch") // Use PyTorch engine
                        .build();

        this.model = criteria.loadModel();

    }

    @Produces
    ZooModel<Image, DetectedObjects> getZooModel() {
        return this.model;
    }

    @Produces
    @RequestScoped
    public Predictor<Image, DetectedObjects> predictor(ZooModel<Image, DetectedObjects> zooModel) {
        System.out.println("Create Predictor");
        return zooModel.newPredictor();
    }

    void close(@Disposes Predictor<Image, DetectedObjects>  predictor) {
        System.out.println("Closes Predictor");
        predictor.close();
    }

}
