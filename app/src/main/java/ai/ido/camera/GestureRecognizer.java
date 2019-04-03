package ai.ido.camera;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.ModelReadyListener;
import ai.fritz.core.utils.FritzModelManager;
import ai.fritz.customtflite.FritzTFLiteInterpreter;

import static android.content.ContentValues.TAG;

public class GestureRecognizer{
    Bitmap myImage,myBackground, myImageCropped;
    static final int TARGET_DIM = 60;
    static final int NUM_CLASSES = 3;
    FritzOnDeviceModel onDeviceModel = new FritzOnDeviceModel("file:///android_asset/GestureRecognition_output5.tflite", "<e52e219a2a004a7a9bede990bf219ccd>", 1);
    FritzTFLiteInterpreter interpreter = new FritzTFLiteInterpreter(onDeviceModel);
    FritzTFLiteInterpreter tflite;

    FritzManagedModel managedModel = new FritzManagedModel("e52e219a2a004a7a9bede990bf219ccd");
    FritzModelManager modelManager = new FritzModelManager(managedModel);

    public class Result {
        String label;
        float probability;
        public Result(String label, float probability) {
            this.label = label;
            this.probability = probability;
        }
        public float getProbability() { return this.probability; }
    }



    public String predict(Bitmap bitmap) {
        HashMap<Integer, Object> newOutput = new HashMap<Integer,Object>();
        modelManager.loadModel(new ModelReadyListener() {
            @Override
            public void onModelReady(FritzOnDeviceModel onDeviceModel) {
                tflite = new FritzTFLiteInterpreter(onDeviceModel);
                Log.d(TAG, "Interpreter is now ready to use");
            }
        }, true);
        ByteBuffer[] inputData = matrix2bytebuffer();
// Declare where the output will be saved
        float[][] output = new float[1][NUM_CLASSES];
        newOutput.put(0,output);
// Run the graph
        tflite.runForMultipleInputsOutputs(inputData,newOutput);

        return getLabels(output);
    }
    public static Comparator<Result> idComparator = new Comparator<Result>(){
        @Override
        public int compare(Result c1, Result c2) {
            return (int) (c1.getProbability()*10000 - c2.getProbability()*10000);
        }
    };
    public String getLabels(float[][] probabilities)
    {
        String[] labels = {"0","1","2"};
        Result res;
        String results = "";
        Log.w("lipman",""+probabilities[0][0]);
        Log.w("lipman",""+probabilities[0][1]);
        Log.w("lipman",""+probabilities[0][2]);

        Queue<Result> pq = new PriorityQueue<>(probabilities[0].length, idComparator);
        for (int i=0;i<probabilities[0].length;i++) {
            pq.add(new Result(labels[i],probabilities[0][i]));
        }

        for (int i=0;i<probabilities[0].length;i++) {
            res = pq.poll();
            results = res.label+","+results;
        }

        return results;
    }
    private ByteBuffer[] matrix2bytebuffer() {
// Input 1
        ByteBuffer[] data = new ByteBuffer[3];
        ByteBuffer data1 = ByteBuffer.allocateDirect(TARGET_DIM*TARGET_DIM*4);
        float[] floatValues = prepareInput1();

        data1.order(ByteOrder.nativeOrder());
        data1.rewind();
        for (int i=0;i<floatValues.length;i++)
        {
            data1.putFloat(floatValues[i]);
        }
        data[0] = data1;

// Input 2
        ByteBuffer data2 = ByteBuffer.allocateDirect(TARGET_DIM*4);
        float[] floatValues2 = prepareInput2();

        data2.order(ByteOrder.nativeOrder());
        data2.rewind();
        for (int i=0;i<floatValues2.length;i++)
        {
            data2.putFloat(floatValues2[i]);
        }
        data[1] = data2;

// Input 3
        ByteBuffer data3 = ByteBuffer.allocateDirect(TARGET_DIM*4);
        float[] floatValues3 = prepareInput3();

        data3.order(ByteOrder.nativeOrder());
        data3.rewind();
        for (int i=0;i<floatValues3.length;i++)
        {
            data3.putFloat(floatValues3[i]);
        }
        data[2] = data3;


        return data;
    }

    public float[] prepareInput1() {
        float[] output = new float[TARGET_DIM*TARGET_DIM];
        int pixel;
        for (int x=0;x<TARGET_DIM;x++)
        {
            for (int y=0;y<TARGET_DIM;y++)
            {
                pixel = myImageCropped.getPixel(x,y);
                output[x*TARGET_DIM+y] = Color.red(pixel)>0 ? 1 : 0;
            }
        }
        return output;
    }

    public float[] prepareInput2() {
        float[] output = new float[TARGET_DIM];
        int pixel,tmpVal;
        for (int x=0;x<TARGET_DIM;x++)
        {
            for (int y=0;y<TARGET_DIM;y++)
            {
                pixel = myImageCropped.getPixel(x,y);
                tmpVal = Color.red(pixel)>0 ? 1 : 0;
                output[x] += tmpVal;
            }
        }
        return output;
    }

    public float[] prepareInput3() {
        float[] output = new float[TARGET_DIM];
        int pixel,tmpVal;
        for (int x=0;x<TARGET_DIM;x++)
        {
            for (int y=0;y<TARGET_DIM;y++)
            {
                pixel = myImageCropped.getPixel(y,x);
                tmpVal = Color.red(pixel)>0 ? 1 : 0;
                output[x] += tmpVal;
            }
        }
        return output;
    }

}