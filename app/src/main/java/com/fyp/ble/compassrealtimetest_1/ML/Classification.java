package com.fyp.ble.compassrealtimetest_1.ML;

public class Classification {
    float[] meanNormalize =  {9.005383f,-71.305214f};
    float[] stdevNormalize = {5.240373f,2.396530f};


    private float[] normalization(float std, float kalman){
        float normalSTD = (std - meanNormalize[0])/stdevNormalize[0];
        float normalkalman = (kalman - meanNormalize[1])/stdevNormalize[1];
        float[] result = {normalSTD,normalkalman};
        return result;
    }

    public int classify(float std, float kalman){
        float normalized[] = normalization(std,kalman);
        double[] normalizedDouble = {(double)normalized[0],(double)normalized[1]};
//        double[] result = ModelNew.score(normalizedDouble);
        double result = Model.score(normalizedDouble);

//        int i=0;
        int pos = 0;
        if (result>0){
            pos =1;
        }else {
            pos = 0;
        }
//        double MAX = -100000000000000000000000000.0;
//        for (double k:result){
//            if (k>MAX){
//                MAX =k;
//                pos = i;
//            }
//            i++;
//        }
        return pos;
    }

}
