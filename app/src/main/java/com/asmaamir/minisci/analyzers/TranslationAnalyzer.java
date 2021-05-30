package com.asmaamir.minisci.analyzers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
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
import java.util.Random;

public class TranslationAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "DetectionAnalyzer";

    private final TextureView textureView;
    private final ImageView imageView;
    private Context context;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint linePaint;
    private YoloV4TinyClassifier yolov4;
    private final static float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private static final int TF_OD_API_INPUT_SIZE = 416;
    private List<YoloV4TinyClassifier.Recognition> lastResult;

    public TranslationAnalyzer(TextureView textureView, ImageView imageView, CameraX.LensFacing lens, Context context, SimilarityClassifier facenet) {
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
            //Log.i(TAG, "Running detection on image ");

            final long startTime = SystemClock.uptimeMillis();
            if (textureView.getBitmap() == null)
                return;
            Bitmap temp = textureView.getBitmap();
            Bitmap scaled = Bitmap.createScaledBitmap(temp, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, false);

            // TODO: Move probability filter to YoloV4 class
            final List<YoloV4TinyClassifier.Recognition> results = yolov4.recognizeImage(scaled);
            lastResult = results;
            Log.e(TAG, "results.size(): " + results.size());
            if (textureView.getBitmap() == null)
                return;
            float delta_w = textureView.getBitmap().getWidth() / (TF_OD_API_INPUT_SIZE * 1.0f);
            float delta_h = textureView.getBitmap().getHeight() / (TF_OD_API_INPUT_SIZE * 1.0f);
            for (final YoloV4TinyClassifier.Recognition result : results) {
                Random rnd = new Random();
                linePaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    Rect box = new Rect((int) ((result.getLocation().left) * delta_w),
                            (int) ((result.getLocation().top) * delta_h),
                            (int) ((result.getLocation().right) * delta_w),
                            (int) ((result.getLocation().bottom) * delta_h));
                    canvas.drawRect(box, linePaint);
                    String[] parts = result.getTitle().split("-");
                    canvas.drawText(parts[0],
                            (int) (result.getLocation().left) * delta_w,
                            (int) (result.getLocation().top) * delta_h,
                            linePaint);
                    canvas.drawText(parts[1],
                            (int) (result.getLocation().left) * delta_w,
                            (int) (result.getLocation().bottom) * delta_h,
                            linePaint);
                }
            }
            ((Activity) context).runOnUiThread(() -> {
                Log.i(TAG, "update");
                imageView.setImageBitmap(bitmap);
            });
        });
    }

    private float translateX(float x) {
        float delta_w = textureView.getBitmap().getWidth() / (TF_OD_API_INPUT_SIZE * 1.0f);
        return x * delta_w;
    }

    private float translateY(float y) {
        float delta_h = textureView.getBitmap().getHeight() / (TF_OD_API_INPUT_SIZE * 1.0f);
        return y * delta_h;
    }

    private void initDrawingUtils() {
        linePaint = new Paint();
        linePaint.setColor(Color.MAGENTA);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(8f);
        linePaint.setTextSize(90);
    }

    private void initBitmap() {
        bitmap = Bitmap.createBitmap(textureView.getWidth(), textureView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
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
