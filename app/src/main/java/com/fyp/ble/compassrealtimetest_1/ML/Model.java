package com.fyp.ble.compassrealtimetest_1.ML;

import umich.cse.yctung.androidlibsvm.LibSVM;

public class Model {
//    public static double score(double[] input) {
//        return ((1.4544577163457983) + ((input[0]) * (-1.966701528872492))) + ((input[1]) * (0.06821109291977182));
//    }

    LibSVM svm;

//    public void test(){
//        this.svm.
//    }







    public static double score(double[] input) {


        return ((1.380402134963997) + ((input[0]) * (-1.8132205815542553))) + ((input[1]) * (0.018523785450020428));
    }
}
