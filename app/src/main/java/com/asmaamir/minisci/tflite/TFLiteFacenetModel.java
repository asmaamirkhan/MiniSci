package com.asmaamir.minisci.tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;
import android.util.Pair;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class TFLiteFacenetModel
        implements SimilarityClassifier {
    private static final String TAG = "TFLiteFacenetModel";
    private static final int TF_OD_API_INPUT_SIZE = 160;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "facenet.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    //
    //private static final int OUTPUT_SIZE = 512;
    private static final int OUTPUT_SIZE = 128;

    // Only return this many results.
    private static final int NUM_DETECTIONS = 1;

    // Float model
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;

    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    private boolean isModelQuantized;
    // Config values.
    private int inputSize;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private float[][][] outputLocations;
    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private float[][] outputClasses;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private float[][] outputScores;
    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private float[] numDetections;

    private float[][] embeedings;

    private ByteBuffer imgData;

    private Interpreter tfLite;

    // Face Mask Detector Output
    private float[][] output;

    private HashMap<String, Recognition> registered = new HashMap<>();

    public void register(String name, Recognition rec) {
        registered.put(name, rec);
    }

    private TFLiteFacenetModel() {
    }

    /**
     * Memory-map the model file in Assets.
     */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     */
    public static SimilarityClassifier create(
            final AssetManager assetManager)
            throws IOException {

        final TFLiteFacenetModel d = new TFLiteFacenetModel();

        String actualFilename = TF_OD_API_LABELS_FILE.split("file:///android_asset/")[1];
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null) {
            Log.w(TAG, line);
            d.labels.add(line);
        }
        br.close();

        d.inputSize = TF_OD_API_INPUT_SIZE;

        try {
            d.tfLite = new Interpreter(loadModelFile(assetManager, TF_OD_API_MODEL_FILE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = TF_OD_API_IS_QUANTIZED;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (TF_OD_API_IS_QUANTIZED) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];

        d.tfLite.setNumThreads(NUM_THREADS);
        d.outputLocations = new float[1][NUM_DETECTIONS][4];
        d.outputClasses = new float[1][NUM_DETECTIONS];
        d.outputScores = new float[1][NUM_DETECTIONS];
        d.numDetections = new float[1];
        return d;
    }

    // looks for the nearest embeeding in the dataset (using L2 norm)
    // and retrurns the pair <id, distance>
    //private Pair<String, Float> findNearest(float[] emb) {
    public float findCosDistance(float[] knownEmb, float[] currentEmb) {

        Pair<String, Float> ret = null;
        //for (Map.Entry<String, Recognition> entry : registered.entrySet()) {
        //final String name = entry.getKey();
        //final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

        float distance = 0;
        /*for (int i = 0; i < emb.length; i++) {
              float diff = emb[i] - knownEmb[i];
              distance += diff*diff;
        }
        distance = (float) Math.sqrt(distance);*/
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < currentEmb.length; i++) {
            dotProduct += currentEmb[i] * knownEmb[i];
            normA += Math.pow(currentEmb[i], 2);
            normB += Math.pow(knownEmb[i], 2);
        }
        float f = dotProduct / ((float) Math.sqrt(normA) * (float) Math.sqrt(normB));
        /*if (ret == null || distance < ret.second) {
            ret = new Pair<>(name, distance);
        }*/
        //}

        return f;

    }


    @Override
    //public List<Recognition> recognizeImage(final Bitmap bitmap, boolean storeExtra) {
    public float[] recognizeImage(final Bitmap bitmap, boolean storeExtra) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        Trace.endSection(); // preprocessBitmap

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");


        Object[] inputArray = {imgData};

        Trace.endSection();

// Here outputMap is changed to fit the Face Mask detector
        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeedings);


        // Run the inference call.
        Trace.beginSection("run");
        //tfLite.runForMultipleInputsOutputs(inputArray, outputMapBack);
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        Trace.endSection();

//    String res = "[";
//    for (int i = 0; i < embeedings[0].length; i++) {
//      res += embeedings[0][i];
//      if (i < embeedings[0].length - 1) res += ", ";
//    }
//    res += "]";
        return embeedings[0];
        /*float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";

        if (registered.size() > 0) {
            //LOGGER.i("dataset SIZE: " + registered.size());
            final Pair<String, Float> nearest = findNearest(embeedings[0]);
            if (nearest != null) {

                final String name = nearest.first;
                label = name;
                distance = nearest.second;

                Log.i(TAG, "nearest: " + name + " - distance: " + distance);

            }
        }


        final int numDetectionsOutput = 1;
        final ArrayList<Recognition> recognitions = new ArrayList<>(numDetectionsOutput);
        Recognition rec = new Recognition(
                id,
                label,
                distance,
                new RectF());

        recognitions.add(rec);

        if (storeExtra) {
            rec.setExtra(embeedings);
        }

        Trace.endSection();
        return recognitions;*/
    }

    @Override
    public void enableStatLogging(final boolean logStats) {
    }

    @Override
    public String getStatString() {
        return "";
    }

    @Override
    public void close() {
    }

    public void setNumThreads(int num_threads) {
        if (tfLite != null) tfLite.setNumThreads(num_threads);
    }

    @Override
    public void setUseNNAPI(boolean isChecked) {
        if (tfLite != null) tfLite.setUseNNAPI(isChecked);
    }
}
