//package com.fyp.ble.compassrealtimetest_1.ML;
//
//import android.content.res.AssetFileDescriptor;
//
//import org.tensorflow.lite.Interpreter;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//
//public class TensorflowLiteClassifier {
//
//    private Interpreter tflite;
//
//    public TensorflowLiteClassifier() {
//        try{
//            tflite = new Interpreter(loadModelFile());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private float[] meanNormalize =  {9.005383f,-71.305214f};
//    private float[] stdevNormalize = {5.240373f,2.396530f};
//
//
//    public int classify(float std, float rssi){
//        float result = doInterference(std,rssi);
//        if (result>0.5){
//            return 1;
//        }else{
//            return 0;
//        }
//    }
//
//    private float doInterference(float std, float rssi){
//        float[] normalized = normalization(std,rssi);
//
//        float[] inputVal = new float[2];
//        inputVal[0] = normalized[0];
//        inputVal[1] = normalized[1];
//
//        float[][] outputval = new  float[1][1];
//        tflite.run(inputVal,outputval);
//        return outputval[0][0];
//    }
//
//    private float[] normalization(float std, float kalman){
//        float normalSTD = (std - meanNormalize[0])/stdevNormalize[0];
//        float normalkalman = (kalman - meanNormalize[1])/stdevNormalize[1];
//        float[] result = {normalSTD,normalkalman};
//        return result;
//    }
//
//
//
//
//
//}
