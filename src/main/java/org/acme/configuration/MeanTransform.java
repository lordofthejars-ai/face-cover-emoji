package org.acme.configuration;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.internal.NDArrayEx;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Transform;
import ai.djl.translate.TranslatorContext;

public class MeanTransform implements Transform {

    private final float[] mean;

    public MeanTransform(float[] mean) {
        this.mean = mean;
    }

    @Override
    public NDArray transform(NDArray array) {

        NDArrayEx ndArrayInternal = array.getNDArrayInternal();

        NDManager manager = ndArrayInternal.getArray().getManager();
        int dim = array.getShape().dimension();
        Shape shape = (dim == 3) ? new Shape(3, 1, 1) : new Shape(1, 3, 1, 1);
        try (NDArray meanArr = manager.create(mean, shape);
             NDArray stdArr = manager.create(new float[]{1, 1, 1}, shape)) {
            return array.sub(meanArr);
        }
    }
}
