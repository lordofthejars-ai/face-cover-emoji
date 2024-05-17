package org.acme.configuration;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class ImageConfiguration {

    @Produces
    public ImageFactory imageFactory() {
        return ImageFactory.getInstance();
    }

    @Produces
    @Named("glasses")
    public Image minor(ImageFactory imageFactory) throws IOException {
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("facherofacherito.jpg")) {
            return imageFactory.fromInputStream(is);
        }
    }

    @Produces
    @Named("smile")
    public Image adult(ImageFactory imageFactory) throws IOException {
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("smile.jpg")) {
            return imageFactory.fromInputStream(is);
        }
    }

}
