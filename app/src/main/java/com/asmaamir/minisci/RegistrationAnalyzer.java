package com.asmaamir.minisci;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

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

    RegistrationAnalyzer(TextureView textureView, ImageView imageView, CameraX.LensFacing lens, Context context) {
        this.textureView = textureView;
        this.imageView = imageView;
        this.lens = lens;
        this.context = context;
        initDrawingUtils();
        initDetector();
        initRegisterButton();
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        long currentTimeStamp = System.currentTimeMillis();
        int rotation = degreesToFirebaseRotation(rotationDegrees);
        fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
        initBitmap();
        detectFaces();

    }

    private void initRegisterButton() {
        butRegister = (FloatingActionButton) ((RegistrationActivity) context).findViewById(R.id.button_register);
        butRegister.setOnClickListener(v ->
        {
            showAddFaceDialog();
        });

    }

    private void showAddFaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.face_edit_dialog, null);
        ImageView ivFace = dialogLayout.findViewById(R.id.dlg_image);
        TextView tvTitle = dialogLayout.findViewById(R.id.dlg_title);
        EditText etName = dialogLayout.findViewById(R.id.dlg_input);
        tvTitle.setText("Add Face");
        if (lastFace != null) {
            int cropHeight = (int) (lastFace.bottom - lastFace.top);
            int cropWidth = (int) (lastFace.right - lastFace.left);
            Bitmap crop = Bitmap.createBitmap(fbImage.getBitmap(),
                    lastFace.left,
                    lastFace.top,
                    cropWidth, cropHeight);
            ivFace.setImageBitmap(crop);
        }
        etName.setHint("Input name");
        builder.setPositiveButton("OK", (dlg, i) -> {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                return;
            }
            //detector.register(name, rec);
            dlg.dismiss();
        });
        builder.setView(dialogLayout);
        builder.show();
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
