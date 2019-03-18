package com.omeric.android.bloodtestchecker;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "gipsy:MainActivity";

    // JSON Node names
    private static final String TAG_THRESHOLDS = "bloodTestConfig";
    private static final String TAG_THRESHOLD_NAME = "name";
    private static final String TAG_THRESHOLD = "threshold";
    private static final String TAG_HDL = "HDL Cholesterol";
    private static final String TAG_LDL = "LDL Cholesterol";
    private static final String TAG_A1C = "A1C";

    private static final String JSON_CONFIG_URL = "https://raw.githubusercontent.com/GipsyBeggar/BloodTestChecker/master/Configs/bloodTestConfig.json";

    // default values in case JSON parsing fails
    private int mHdlThreshold = 60;
    private int mLdlThreshold = 70;
    private int mA1cThreshold = 6;

    private EditText mTestNameView;
    private EditText mTestResultView;
    private ImageView mTestEvaluationImageView;
    private TextView mTestEvaluationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "::onCreate: start");
        setContentView(R.layout.activity_main);
/*
        JSONObject lConfJsonObject = getJsonObjectFromRawResource();
        if (lConfJsonObject != null)
        {
            getThresholdsFromJson(lConfJsonObject);
        }
*/
        getThresholdsFromUrl();

        mTestNameView = findViewById(R.id.EdtTxt_main_activity_test_name);
        mTestResultView = findViewById(R.id.EdtTxt_main_activity_test_result);
        mTestEvaluationImageView = findViewById(R.id.imgVw_main_activity_evaluation_image);
        mTestEvaluationTextView = findViewById(R.id.TxtVw_main_activity_evaluation_text);


        Log.d(TAG, "::onCreate: end");
    }

    public void Reset(@SuppressWarnings("unused") View pView) {
        mTestNameView.setText(null);
        mTestResultView.setText(null);
        mTestNameView.setError(null);
        mTestResultView.setError(null);
        hideEvaluation();
    }

    private void hideEvaluation() {
        mTestEvaluationImageView.setVisibility(View.INVISIBLE);
        mTestEvaluationTextView.setVisibility(View.INVISIBLE);
    }

    private void showEvaluation() {
        mTestEvaluationImageView.setVisibility(View.VISIBLE);
        mTestEvaluationTextView.setVisibility(View.VISIBLE);
    }

    public void CheckResult(@SuppressWarnings("unused") View pView) {
        String lTestName = mTestNameView.getText().toString();
        int lTestResult = 0;

        mTestNameView.setError(null);
        mTestResultView.setError(null);

        boolean lIfCancel = false;
        View lFocusView = null;

        int lNumberOfResultNames = 0;

        hideEvaluation();

        // check inputs
        if (lTestName.equals("")) {
            mTestNameView.setError(getString(R.string.error_field_required));
            lFocusView = mTestNameView;
            lIfCancel = true;
        }
        try {
            lTestResult = Integer.parseInt(mTestResultView.getText().toString());
        } catch (NumberFormatException pNumberFormatException) {
            mTestResultView.setError(getString(R.string.error_must_be_a_number));
            lFocusView = mTestResultView;
            lIfCancel = true;
        }

        if (lIfCancel) {
            // There was a problem with the inputs
            lFocusView.requestFocus();
            return;
        }

        // if input is legal - continue:
        if (lTestName.toLowerCase().contains(getResources().getString(R.string.hdl_dict))) {
            if (lTestResult < mHdlThreshold) {
                setGoodEvaluationImage();
            } else {
                setBadEvaluationImage();
            }
            setEvaluationText(TAG_HDL);
            lNumberOfResultNames++;
        }

        if (lTestName.toLowerCase().contains(getResources().getString(R.string.ldl_dict))) {
            if (lTestResult < mLdlThreshold) {
                setGoodEvaluationImage();
            } else {
                setBadEvaluationImage();
            }
            setEvaluationText(TAG_LDL);
            lNumberOfResultNames++;
        }

        // find any combination of 'hmglbn', hm, 'a1c'
        // https://regex101.com/r/jhyFoJ/3
        String lRegExHemoglobin = "(\\bh[e|i]?[m|n][o]?gl[o|i]?b[e|i]?[m|n]\\b)|(\\bhm\\b)|(\\ba{1,2}1c{1,2}\\b)";

        // Create a Pattern object
        Pattern lPattern = Pattern.compile(lRegExHemoglobin, Pattern.CASE_INSENSITIVE);

        // Now create matcher object.
        Matcher lMatcher = lPattern.matcher(lTestName);
        if (lMatcher.find()) {
            if (lTestResult < mA1cThreshold) {
                setGoodEvaluationImage();
            } else {
                setBadEvaluationImage();
            }
            setEvaluationText(TAG_A1C);
            lNumberOfResultNames++;
        }

        // There was a problem with the inputs
        if (lNumberOfResultNames > 1) {
            mTestNameView.setError(getString(R.string.error_more_than_one));
            mTestNameView.requestFocus();
            hideEvaluation();
            return;
        }
        if (lNumberOfResultNames == 0) {
            setUnknownEvaluationImage();
            mTestEvaluationTextView.setText("");
            mTestEvaluationTextView.setVisibility(View.INVISIBLE);
        }

        showEvaluation();

        Log.d(TAG, "::CheckResult: end");
    }

    private void setEvaluationText(String pEvaluationType) {
        String lFinalString = getString(R.string.your_evaluation_for) + pEvaluationType;
        mTestEvaluationTextView.setText(lFinalString);
    }

    private void setGoodEvaluationImage() {
        mTestEvaluationImageView.setBackgroundResource(R.drawable.emoji_good);
    }

    private void setBadEvaluationImage() {
        mTestEvaluationImageView.setBackgroundResource(R.drawable.emoji_bad);
    }

    private void setUnknownEvaluationImage() {
        mTestEvaluationImageView.setBackgroundResource(R.drawable.emoji_unknown);
    }

    @SuppressLint("StaticFieldLeak")
    private void getThresholdsFromUrl() {
        final ProgressBar lProgressBar = findViewById(R.id.progressbar_main_activity);
        final LinearLayout lLinearLayout = findViewById(R.id.layout_main_activity);
        lProgressBar.setVisibility(View.VISIBLE);
        lLinearLayout.setVisibility(View.INVISIBLE);

        new AsyncTask<Void, Void, StringBuilder>() {
            @Override
            protected StringBuilder doInBackground(Void... params) {
                StringBuilder lStringBuilder = new StringBuilder();
                try {
                    URL lUrl = new URL(JSON_CONFIG_URL); //Config file location
                    //First open the connection
                    HttpURLConnection lHttpUrlConnection = (HttpURLConnection) lUrl.openConnection();
                    lHttpUrlConnection.setConnectTimeout(20000); // 20 seconds timeout

                    BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(lHttpUrlConnection.getInputStream()));

                    String lLineString;
                    while ((lLineString = lBufferedReader.readLine()) != null) {
                        lStringBuilder.append(lLineString);
                    }
                    lBufferedReader.close();
                } catch (Exception pE) {
//                    Log.d("MyTag",e.toString());
                    Log.e(TAG, "::getThresholdsFromUrl: error getting JSON from URL: ", pE);
                }

//                Log.d(TAG, "::getThresholdsFromUrl: " + lStringBuilder.toString());

                Log.d(TAG, "::getThresholdsFromJson: success getting JSON from URL");
                return lStringBuilder;
            }

            @Override
            protected void onPostExecute(StringBuilder lResult) {
                try {
                    JSONObject lConfJsonObject = new JSONObject(lResult.toString());
                    getThresholdsFromJson(lConfJsonObject);
                } catch (JSONException pE) {
                    Log.e(TAG, "::getThresholdsFromUrl: error parsing JSON: ", pE);
                } finally {
                    lProgressBar.setVisibility(View.INVISIBLE);
                    lLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private JSONObject getJsonObjectFromRawResource() {
        InputStream lInputStream;
        JSONObject lJsonObject;

        try {
            lInputStream = getResources().openRawResource(R.raw.blood_test_config);
        } catch (Resources.NotFoundException pNotFoundException) {
            Log.e(TAG, "::getJsonObjectFromRawResource: JSON resource not found:", pNotFoundException);
            return null;
        }

        // delimiter pattern \A means 'the beginning of the input'
        // .next() reads the next token
        String jsonString = new Scanner(lInputStream).useDelimiter("\\A").next();
        try {
            lJsonObject = new JSONObject(jsonString);
        } catch (JSONException pJSONException) {
            Log.e(TAG, "::getJsonObjectFromRawResource: Error parsing JSON:", pJSONException);
            return null;
        }

        return lJsonObject;
    }

    private void getThresholdsFromJson(JSONObject pJsonObject) {
        JSONArray lThresholds;
        try {
            // Getting Array of Thresholds
            lThresholds = pJsonObject.getJSONArray(TAG_THRESHOLDS);

            // looping through All Contacts
            for (int i = 0; i < lThresholds.length(); i++) {
                JSONObject lJsonInnerObject = lThresholds.getJSONObject(i);
                String lThresholdName = lJsonInnerObject.getString(TAG_THRESHOLD_NAME);

                if (lThresholdName.equals(TAG_HDL)) {
                    mHdlThreshold = lJsonInnerObject.getInt(TAG_THRESHOLD);
                } else if (lThresholdName.equals(TAG_LDL)) {
                    mLdlThreshold = lJsonInnerObject.getInt(TAG_THRESHOLD);
                } else if (lThresholdName.equals(TAG_A1C)) {
                    mA1cThreshold = lJsonInnerObject.getInt(TAG_THRESHOLD);
                } else {
                    Log.e(TAG,
                        "::getThresholdsFromJson: unknown threshold name: " + lThresholdName);
                }
            }
        } catch (JSONException pJSONException) {
            Log.e(TAG, "::getThresholdsFromJson: Error getting thresholds from JSON object:", pJSONException);
            return;
        }

        Log.d(TAG, "::getThresholdsFromJson: success parsing JSON");
    }
}
