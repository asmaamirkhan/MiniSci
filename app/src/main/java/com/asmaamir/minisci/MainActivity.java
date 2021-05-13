package com.asmaamir.minisci;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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
        setContentView(R.layout.activity_main);
        butSignin = (Button) findViewById(R.id.button_signin);
        butSignup = (Button) findViewById(R.id.button_signup);
        butSignup.setOnClickListener(v -> {
            /*double[] arr = {6.5, 5.6, 3.4};
            setPrefIntArray("vector", arr);*/
            Intent regRedirect = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(regRedirect);
        });
        butSignin.setOnClickListener(v -> {
            /*double[] arr = {0, 0, 0};
            double[] prefArr = getPrefIntArray("vector", arr);
            Log.i(TAG, "" + getPrefIntArray("vector", arr).length);
            for (int i = 0; i < prefArr.length; i++) {
                Log.i(TAG, "" + prefArr[i]);
            }*/
        });
    }

    public void setPrefIntArray(String tag, double[] value) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();

        String s;
        try {
            JSONArray jsonArr = new JSONArray();
            for (double i : value)
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

    public double[] getPrefIntArray(String tag, double[] defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String s = pref.getString(tag, "");
        try {
            JSONObject json = new JSONObject(new JSONTokener(s));
            JSONArray jsonArr = json.getJSONArray(tag);

            double[] result = new double[jsonArr.length()];

            for (int i = 0; i < jsonArr.length(); i++)
                result[i] = jsonArr.getDouble(i);

            return result;
        } catch (JSONException excp) {
            return defaultValue;
        }
    }
}