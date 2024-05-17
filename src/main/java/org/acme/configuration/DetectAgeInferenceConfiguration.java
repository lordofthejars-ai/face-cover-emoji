package org.acme.configuration;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class DetectAgeInferenceConfiguration {

    @ConfigProperty(name = "fraud.model.path", defaultValue = "src/main/resources/age_googlenet.onnx")
    private String modelPath;

    @Produces
    public Criteria<Image, Classifications> criteria() {

        ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                .addTransform(new CenterCrop())
                .addTransform(new Resize(224, 224))
                .addTransform(new TransposeTransform(2, 0, 1))
                //.addTransform(new MeanTransform(new float[]{ 104, 117, 123 }))

                .addTransform(new ToFloatTransform())
                .build();


        Path modelPath = Paths.get(this.modelPath);
        return
                Criteria.builder()
                        .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                        .setTypes(Image.class, Classifications.class)
                        .optModelPath(modelPath)
                        .optEngine("OnnxRuntime")
                        .optTranslator(translator)
                        // for performance optimization maxBox parameter can reduce number of
                        // considered boxes from 8400
                        //.optArgument("maxBox", 1000)
                        //.optTranslatorFactory(new YoloV8TranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();
    }

    @Produces
    public ZooModel<Image, Classifications> zooModel(Criteria<Image, Classifications>  criteria) throws ModelNotFoundException, MalformedModelException, IOException {
        return criteria.loadModel();
    }

    @Produces
    public Predictor<Image, Classifications> predictor(ZooModel<Image, Classifications>  zooModel) {
        Predictor<Image, Classifications> preditor = zooModel.newPredictor();
        System.out.println(preditor.toString());
        return preditor;
    }

}
