package com.techiguru.aiinterview;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONObject;

public class EvaluationActivity extends AppCompatActivity {

    CircularProgressIndicator progressScore;

    TextView txtOverallScore, txtCommunication, txtTechnical, txtConfidence,
            txtStrength, txtGrowth, txtRecommendation,txtTranscript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        progressScore = findViewById(R.id.progressScore);

        txtOverallScore = findViewById(R.id.txtOverallScore);
        txtCommunication = findViewById(R.id.txtCommunication);
        txtTechnical = findViewById(R.id.txtTechnical);
        txtConfidence = findViewById(R.id.txtConfidence);
        txtStrength = findViewById(R.id.txtStrength);
        txtGrowth = findViewById(R.id.txtGrowth);
        txtRecommendation = findViewById(R.id.txtRecommendation);
        txtTranscript = findViewById(R.id.txtTranscript);

        Button btnHome = findViewById(R.id.btnHome);

        btnHome.setOnClickListener(v -> {

            Intent intent = new Intent(
                    EvaluationActivity.this,
                    HomeActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        String report =
                getIntent().getStringExtra("report");

        String interviewData =
                getIntent().getStringExtra("interviewData");

        try {

            JSONObject obj = new JSONObject(report);

            String overall =
                    obj.getString("overall_score");

            String communication =
                    obj.getString("communication");

            String technical =
                    obj.getString("technical_accuracy");

            String confidence =
                    obj.getString("confidence");

            String strength =
                    obj.getString("key_strength");

            String growth =
                    obj.getString("growth_area");

            String recommendation =
                    obj.getString("recommendation");

            txtOverallScore.setText(overall);

            txtCommunication.setText(
                    "Communication: " + communication
            );

            txtTechnical.setText(
                    "Technical Accuracy: " + technical
            );

            txtConfidence.setText(
                    "Confidence: " + confidence
            );

            txtStrength.setText(strength);

            txtGrowth.setText(growth);

            txtRecommendation.setText(recommendation);

            txtTranscript.setText(interviewData);

            // Convert score to percentage
            int percent = 0;

            if (overall.contains("%")) {

                percent = Integer.parseInt(
                        overall.replace("%", "")
                );

            } else if (overall.contains("/10")) {

                double score =
                        Double.parseDouble(
                                overall.replace("/10", "")
                        );

                percent = (int) (score * 10);
            }

            progressScore.setProgress(percent);

        } catch (Exception e) {

            txtTranscript.setText(
                    "Error parsing report:\n\n" +
                            e.getMessage()
            );

            e.printStackTrace();
        }
    }
}