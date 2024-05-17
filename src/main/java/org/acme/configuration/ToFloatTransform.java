package org.acme.configuration;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Transform;

public class ToFloatTransform implements Transform {
    @Override
    public NDArray transform(NDArray array) {
        if (!array.getDataType().equals(DataType.FLOAT32)) {
           return array.toType(DataType.FLOAT32, false);
        }

        return array;
    }
}
