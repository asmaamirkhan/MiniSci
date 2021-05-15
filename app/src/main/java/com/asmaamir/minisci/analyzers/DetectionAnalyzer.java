package com.asmaamir.minisci.analyzers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.asmaamir.minisci.tflite.SimilarityClassifier;
import com.asmaamir.minisci.tflite.TFLiteYoloV4TinyModel;
import com.asmaamir.minisci.tflite.YoloV4TinyClassifier;

import java.io.IOException;
import java.util.List;

public class DetectionAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "DetectionAnalyzer";

    private final TextureView textureView;
    private final ImageView imageView;
    private Context context;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint linePaint;
    private YoloV4TinyClassifier yolov4;
    private final static float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final int TF_OD_API_INPUT_SIZE = 416;

    public DetectionAnalyzer(TextureView textureView, ImageView imageView, CameraX.LensFacing lens, Context context, SimilarityClassifier facenet) {
        this.textureView = textureView;
        this.imageView = imageView;
        this.context = context;
        initDrawingUtils();
        initDetector();
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        initBitmap();
        processImage();
    }


    private void processImage() {
        AsyncTask.execute(() -> {
            Log.i(TAG, "Running detection on image ");

            final long startTime = SystemClock.uptimeMillis();
            Bitmap temp = textureView.getBitmap();
            Bitmap scaled = Bitmap.createScaledBitmap(temp, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, false);

            final List<YoloV4TinyClassifier.Recognition> results = yolov4.recognizeImage(scaled);
            Log.e(TAG, "results.size(): " + results.size());


            for (final YoloV4TinyClassifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    Log.i(TAG, "res: " + result.toString());
                    drawResult(result);
                }
                ((Activity) context).runOnUiThread(() -> {
                    imageView.setImageBitmap(bitmap);
                });
            }
            Log.i(TAG, "det done");
        });
    }

    private void drawResult(YoloV4TinyClassifier.Recognition result) {
        float delta_w = textureView.getBitmap().getWidth() / (TF_OD_API_INPUT_SIZE * 1.0f);
        float delta_h = textureView.getBitmap().getHeight() / (TF_OD_API_INPUT_SIZE * 1.0f);
        RectF box = new RectF(result.getLocation().left * delta_w,
                result.getLocation().top * delta_h,
                result.getLocation().right * delta_w,
                result.getLocation().bottom * delta_h);
        canvas.drawRect(box, linePaint);
        float xCenter = (result.getLocation().left) * delta_w;
        float yCenter = (result.getLocation().top) * delta_h;
        canvas.drawText(result.getTitle(),
                xCenter,
                yCenter,
                linePaint);
        /*canvas.drawText("" + (result.getConfidence()),
                result.getLocation().right * delta_w,
                result.getLocation().bottom * delta_h,
                linePaint);*/
    }

    private void initDrawingUtils() {
        linePaint = new Paint();
        linePaint.setColor(Color.MAGENTA);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setTextSize(75);
    }

    private void initBitmap() {
        bitmap = Bitmap.createBitmap(textureView.getWidth(), textureView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    private void initDetector() {
        try {
            yolov4 = TFLiteYoloV4TinyModel.create(context.getAssets());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Exception initializing classifier!");
        }
    }


}
