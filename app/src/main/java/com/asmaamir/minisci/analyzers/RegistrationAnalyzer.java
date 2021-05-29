package com.asmaamir.minisci.analyzers;

import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;

import com.asmaamir.minisci.R;
import com.asmaamir.minisci.activities.RegistrationActivity;
import com.asmaamir.minisci.tflite.SimilarityClassifier;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RegistrationAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "RegistrationAnalyzer";
    private FirebaseVisionFaceDetector faceDetector;
    private final TextureView textureView;
    private final ImageView imageView;
    private FloatingActionButton butRegister;
    private Rect lastFace;
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

    public RegistrationAnalyzer(TextureView textureView, ImageView imageView, CameraX.LensFacing lens, Context context, SimilarityClassifier facenet) {
        this.textureView = textureView;
        this.imageView = imageView;
        this.lens = lens;
        this.context = context;
        this.facenet = facenet;
        initDrawingUtils();
        initDetector();
        initRegisterButton();
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (DETECTION_FLAG) {
            int rotation = degreesToFirebaseRotation(rotationDegrees);
            fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
            initBitmap();
            detectFaces();
        }
    }

    private void initRegisterButton() {
        butRegister = (FloatingActionButton) ((RegistrationActivity) context).findViewById(R.id.button_register);
        butRegister.setOnClickListener(v ->
        {
            Log.i(TAG, "clicked");
            DETECTION_FLAG = false;
            showAddFaceDialog();
        });

    }

    private void showAddFaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.face_edit_dialog, null);
        ImageView ivFace = dialogLayout.findViewById(R.id.dlg_image);
        EditText etName = dialogLayout.findViewById(R.id.dlg_input);

        if (lastFace != null) {
            Bitmap crop = Bitmap.createBitmap(fbImage.getBitmap(),
                    lastFace.left,
                    lastFace.top,
                    lastFace.right - lastFace.left,
                    lastFace.bottom - lastFace.top);
            Bitmap scaled = Bitmap.createScaledBitmap(crop, 160, 160, false);
            ivFace.setImageBitmap(scaled);


            //etName.setHint("Input name");
            builder.setPositiveButton("OK", (dlg, i) -> {
                String name = etName.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(context, "Enter ur name", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context)
                        .edit();
                prefEditor.putString("user_name", name);
                prefEditor.apply();

                float[] emb = facenet.recognizeImage(scaled, false);
                setPrefFloatArray("embedding", emb);

                dlg.dismiss();
            });
            builder.setView(dialogLayout);
            builder.show();
        }
    }

    public void setPrefFloatArray(String tag, float[] value) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();

        String s;
        try {
            JSONArray jsonArr = new JSONArray();
            for (float i : value)
                jsonArr.put(i);
            JSONObject json = new JSONObject();
            json.put(tag, jsonArr);
            s = json.toString();
        } catch (JSONException excp) {
            s = "";
        }

        prefEditor.putString(tag, s);
        prefEditor.apply();
    }

    private void initDrawingUtils() {
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, R.color.logo_blue));
        linePaint.setStrokeWidth(16f);
        linePaint.setStyle(Paint.Style.STROKE);
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
            lastFace = face.getBoundingBox();
        }
        imageView.setImageBitmap(bitmap);
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
}
