package com.asmaamir.minisci.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.asmaamir.minisci.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends AppCompatActivity {
    private Button butSignin;
    private Button butSignup;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        butSignin = (Button) findViewById(R.id.button_signin);
        butSignup = (Button) findViewById(R.id.button_signup);
        butSignup.setOnClickListener(v -> {
            /*float[] arr = {6.5f, 5.6f, 3.4f};
            setPrefIntArray("vector", arr);*/
            Intent regRedirect = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(regRedirect);
        });
        butSignin.setOnClickListener(v -> {
            /*float[] arr = {1, 1, 1};
            float[] prefArr = getPrefFloatArray("embedding", arr);
            Log.i(TAG, "Size: " + getPrefFloatArray("embedding", arr).length);
            String vector = "";
            for (int i = 0; i < prefArr.length; i++) {
                vector += prefArr[i] + ", ";
            }
            Log.i(TAG, vector);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String s = pref.getString("user_name", "NONE");
            Log.i(TAG, s);*/
            Intent loginRedirect = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginRedirect);

        });
    }

    public void setPrefIntArray(String tag, float[] value) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this)
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

    public float[] getPrefFloatArray(String tag, float[] defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
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
}