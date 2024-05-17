package org.acme.configuration;

import ai.djl.ndarray.NDArray;
import ai.djl.translate.Transform;

public class TransposeTransform implements Transform {

    private int[] axes;

    public TransposeTransform(int...axes) {
        this.axes = axes;
    }

    @Override
    public NDArray transform(NDArray array) {
        return array.transpose(axes).flip(0);
    }
}
