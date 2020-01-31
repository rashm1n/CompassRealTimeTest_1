package com.fyp.ble.compassrealtimetest_1.ML;

public class Model {
    public static double score(double[] input) {
        return ((1.4544577163457983) + ((input[0]) * (-1.966701528872492))) + ((input[1]) * (0.06821109291977182));
    }
}
