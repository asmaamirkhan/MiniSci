package com.asmaamir.minisci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.asmaamir.minisci.tflite.SimilarityClassifier;
import com.asmaamir.minisci.tflite.TFLiteFacenetModel;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private TextureView textureView;
    private ImageView imageView;
    private SimilarityClassifier facenet;
    public static final int REQUEST_CODE_PERMISSION = 101;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private static final String TAG = "LoginActivity";

    public static CameraX.LensFacing lens = CameraX.LensFacing.FRONT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textureView = findViewById(R.id.texture_view_login);
        imageView = findViewById(R.id.image_view_login);
        if (allPermissionsGranted()) {
            textureView.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
        textureView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> updateTransform());
        initFacenet();

    }

    private void initFacenet() {
        try {
            facenet =
                    TFLiteFacenetModel.create(getAssets());
            //cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void initCamera() {
        CameraX.unbindAll();
        PreviewConfig pc = new PreviewConfig
                .Builder()
                .setLensFacing(lens)
                .setTargetResolution(new Size(textureView.getWidth(), textureView.getHeight()))
                .build();

        Preview preview = new Preview(pc);
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup vg = (ViewGroup) textureView.getParent();
            vg.removeView(textureView);
            vg.addView(textureView, 0);
            textureView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });
        ImageAnalysisConfig iac = new ImageAnalysisConfig
                .Builder()
                .setLensFacing(lens)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(iac);
        LoginAnalyzer loginAnalyzer = new LoginAnalyzer(textureView, imageView, lens, this, facenet);
        loginAnalyzer.setFaceAnalyzerObserver(new LoginAnalyzer.FaceAnalyzerObserver() {
            @Override
            public void onRegisteredFaceFound() {
                redirectDash();
            }
        });
        imageAnalysis.setAnalyzer(Runnable::run, loginAnalyzer);
        CameraX.bindToLifecycle(this, preview, imageAnalysis);


    }

    private void redirectDash() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraX.unbindAll();
                finish();
                Intent loginRedirect = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(loginRedirect);
            }
        });

    }

    private void updateTransform() {
        Matrix mat = new Matrix();
        float centerX = textureView.getWidth() / 2.0f;
        float centerY = textureView.getHeight() / 2.0f;

        float rotationDegrees;
        switch (textureView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
            default:
                return;
        }
        mat.postRotate(rotationDegrees, centerX, centerY);
        textureView.setTransform(mat);

    }

    private void startCamera() {
        initCamera();
        ImageButton ibSwitch = findViewById(R.id.fab_switchcam_login);
        ibSwitch.setOnClickListener(v -> {
            if (lens == CameraX.LensFacing.FRONT)
                lens = CameraX.LensFacing.BACK;
            else
                lens = CameraX.LensFacing.FRONT;
            try {
                Log.i(TAG, "" + lens);
                CameraX.getCameraWithLensFacing(lens);
                initCamera();
            } catch (CameraInfoUnavailableException e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                textureView.post(this::startCamera);
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}