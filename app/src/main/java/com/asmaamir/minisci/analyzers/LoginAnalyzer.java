package com.asmaamir.minisci.analyzers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.asmaamir.minisci.tflite.SimilarityClassifier;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LoginAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "LoginAnalyzer";
    private static final float SIMILARITY_THRESH = 0.6f;
    private FirebaseVisionFaceDetector faceDetector;
    private final TextureView textureView;
    private final ImageView imageView;
    private FirebaseVisionFace lastFace;
    private Context context;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint linePaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private FirebaseVisionImage fbImage;
    private CameraX.LensFacing lens;
    private SimilarityClassifier facenet;
    private boolean DETECTION_FLAG = true;
    private float[] registeredUser = {0.0f};
    private float[] currentUser = {0.0f};

    private FaceAnalyzerObserver observer;

    public LoginAnalyzer(TextureView textureView, ImageView imageView, CameraX.LensFacing lens, Context context, SimilarityClassifier facenet) {
        this.textureView = textureView;
        this.imageView = imageView;
        this.lens = lens;
        this.context = context;
        this.facenet = facenet;
        this.observer = null;
        initDrawingUtils();
        initDetector();
        initRecognitionTimer();
        getRegisteredUserEmb();
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        Log.i(TAG, "flag: " + DETECTION_FLAG);
        if (DETECTION_FLAG) {
            int rotation = degreesToFirebaseRotation(rotationDegrees);
            fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
            initBitmap();
            detectFaces();
        } else {

            /*CameraX.unbindAll();
            image.close();
            Intent loginRedirect = new Intent(context, DashboardActivity.class);
            context.startActivity(loginRedirect);
            ((Activity) context).finish();
            Log.i(TAG, "end");*/
        }
    }

    private void getRegisteredUserEmb() {
        registeredUser = getPrefFloatArray("embedding", registeredUser);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public float[] getPrefFloatArray(String tag, float[] defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String s = pref.getString(tag, "");
        try {
            JSONObject json = new JSONObject(new JSONTokener(s));
            JSONArray jsonArr = json.getJSONArray(tag);

            float[] result = new float[jsonArr.length()];

            for (int i = 0; i < jsonArr.length(); i++)
                result[i] = (float) jsonArr.getDouble(i);

            return result;
        } catch (JSONException excp) {
            return defaultValue;
        }
    }

    private void initDrawingUtils() {
        linePaint = new Paint();
        linePaint.setColor(Color.MAGENTA);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        linePaint.setTextSize(50);

    }

    private void initBitmap() {
        bitmap = Bitmap.createBitmap(textureView.getWidth(), textureView.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        widthScaleFactor = canvas.getWidth() / (fbImage.getBitmap().getWidth() * 1.0f);
        heightScaleFactor = canvas.getHeight() / (fbImage.getBitmap().getHeight() * 1.0f);
    }

    private void initDetector() {
        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();
        faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);
    }

    private void detectFaces() {
        faceDetector
                .detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    if (!firebaseVisionFaces.isEmpty()) {
                        processFaces(firebaseVisionFaces);
                    } else {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                    }
                }).addOnFailureListener(e -> Log.i(TAG, e.toString()));
    }

    private void processFaces(List<FirebaseVisionFace> faces) {
        for (FirebaseVisionFace face : faces) {
            Rect box = new Rect((int) translateX(face.getBoundingBox().left),
                    (int) translateY(face.getBoundingBox().top),
                    (int) translateX(face.getBoundingBox().right),
                    (int) translateY(face.getBoundingBox().bottom));
            canvas.drawRect(box, linePaint);
            lastFace = face;
            /*AsyncTask.execute(() -> {
                Bitmap crop = Bitmap.createBitmap(fbImage.getBitmap(),
                        face.getBoundingBox().left,
                        face.getBoundingBox().top,
                        face.getBoundingBox().right - face.getBoundingBox().left,
                        face.getBoundingBox().bottom - face.getBoundingBox().top);
                Bitmap scaled = Bitmap.createScaledBitmap(crop, 160, 160, false);
                currentUser = facenet.recognizeImage(scaled, false);
                float distance = facenet.findCosDistance(registeredUser, currentUser);
                Log.i(TAG, "distance: " + distance);
            });*/


        }
        imageView.setImageBitmap(bitmap);
    }

    private void initRecognitionTimer() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (lastFace != null && DETECTION_FLAG) {
                    int width = lastFace.getBoundingBox().right - lastFace.getBoundingBox().left;
                    int height = lastFace.getBoundingBox().bottom - lastFace.getBoundingBox().top;
                    Bitmap crop;
                    // process overflowing
                    if (lastFace.getBoundingBox().left + width < fbImage.getBitmap().getWidth() && lastFace.getBoundingBox().top + height < fbImage.getBitmap().getHeight()) {
                        crop = Bitmap.createBitmap(fbImage.getBitmap(),
                                Math.max(lastFace.getBoundingBox().left, 0),
                                Math.max(lastFace.getBoundingBox().top, 0),
                                width,
                                height);
                    } else {
                        width = fbImage.getBitmap().getWidth() - lastFace.getBoundingBox().left - 1;
                        height = fbImage.getBitmap().getHeight() - lastFace.getBoundingBox().top - 1;
                        crop = Bitmap.createBitmap(fbImage.getBitmap(),
                                Math.max(lastFace.getBoundingBox().left, 0),
                                Math.max(lastFace.getBoundingBox().top, 0),
                                width,
                                height);


                    }
                    Bitmap scaled = Bitmap.createScaledBitmap(crop, 160, 160, false);
                    currentUser = facenet.recognizeImage(scaled, false);
                    float distance = facenet.findCosDistance(registeredUser, currentUser);
                    Log.i(TAG, "distance: " + distance + " flag: " + DETECTION_FLAG);
                    if (distance > SIMILARITY_THRESH) {
                        DETECTION_FLAG = false;
                        observer.onRegisteredFaceFound(lastFace.getSmilingProbability(),
                                (lastFace.getLeftEyeOpenProbability() +
                                        lastFace.getRightEyeOpenProbability()) / 2);
                    }
                }
            }
        }, 0, 1000);
    }

    private float translateY(float y) {
        return y * heightScaleFactor;
    }

    private float translateX(float x) {
        float scaledX = x * widthScaleFactor;
        if (lens == CameraX.LensFacing.FRONT) {
            return canvas.getWidth() - scaledX;
        } else {
            return scaledX;
        }
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException("Rotation must be 0, 90, 180, or 270.");
        }
    }

    public void setFaceAnalyzerObserver(FaceAnalyzerObserver observer) {
        this.observer = observer;
    }

    public interface FaceAnalyzerObserver {
        public void onRegisteredFaceFound(float smileProb, float eyeOpenProbAvg);
    }
}
