package com.techiguru.aiinterview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ExperienceActivity extends AppCompatActivity {

    private MaterialCardView cardFresher;
    private MaterialCardView cardIntermediate;
    private MaterialCardView cardExperienced;

    private Button btnGenerate;

    private String selectedExperience = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experience);

        cardFresher = findViewById(R.id.cardFresher);
        cardIntermediate = findViewById(R.id.cardIntermediate);
        cardExperienced = findViewById(R.id.cardExperienced);

        btnGenerate = findViewById(R.id.btnGenerate);

        String category =
                getIntent().getStringExtra("category");

        cardFresher.setOnClickListener(v -> {

            selectedExperience = "Fresher";

            resetCards();

            cardFresher.setStrokeWidth(6);
            cardFresher.setStrokeColor(
                    getResources().getColor(
                            android.R.color.holo_blue_dark
                    )
            );
        });

        cardIntermediate.setOnClickListener(v -> {

            selectedExperience = "Intermediate";

            resetCards();

            cardIntermediate.setStrokeWidth(6);
            cardIntermediate.setStrokeColor(
                    getResources().getColor(
                            android.R.color.holo_blue_dark
                    )
            );
        });

        cardExperienced.setOnClickListener(v -> {

            selectedExperience = "Experienced";

            resetCards();

            cardExperienced.setStrokeWidth(6);
            cardExperienced.setStrokeColor(
                    getResources().getColor(
                            android.R.color.holo_blue_dark
                    )
            );
        });

        btnGenerate.setOnClickListener(v -> {

            if (selectedExperience.isEmpty()) {

                Toast.makeText(
                        ExperienceActivity.this,
                        "Select Experience Level",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent intent =
                    new Intent(
                            ExperienceActivity.this,
                            QuestionActivity.class
                    );

            intent.putExtra(
                    "category",
                    category
            );

            intent.putExtra(
                    "experience",
                    selectedExperience
            );

            startActivity(intent);
        });
    }

    private void resetCards() {

        cardFresher.setStrokeWidth(2);
        cardIntermediate.setStrokeWidth(2);
        cardExperienced.setStrokeWidth(2);

        cardFresher.setStrokeColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        cardIntermediate.setStrokeColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );

        cardExperienced.setStrokeColor(
                getResources().getColor(
                        android.R.color.darker_gray
                )
        );
    }
}