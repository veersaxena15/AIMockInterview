package com.techiguru.aiinterview;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Button;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.Locale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class QuestionActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {
    private static final String API_KEY = "YOUR_API_KEY";
    private String category;
    private EditText etAnswer;
    private Button btnEndInterview;
    private TextView txtCategory, txtQuestion, txtStatus;/* txtAnswer;*/
    private TextToSpeech textToSpeech;
    private Button btnNext;
    private OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
    private ArrayList<String> questions = new ArrayList<>();
    private ArrayList<String> userAnswers = new ArrayList<>();
    StringBuilder interviewData = new StringBuilder();
    private int currentIndex = 0;
    private String experience;
    private static final int REQUEST_SPEECH = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        textToSpeech = new TextToSpeech(this, this);

        txtCategory = findViewById(R.id.txtCategory);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtStatus = findViewById(R.id.txtStatus);
       /* txtAnswer = findViewById(R.id.txtAnswer);*/
        etAnswer = findViewById(R.id.etAnswer);

        btnNext = findViewById(R.id.btnNext);
        btnEndInterview = findViewById(R.id.btnEndInterview);

        Button btnSpeak = findViewById(R.id.btnSpeak);

        category = getIntent().getStringExtra("category");

        experience = getIntent().getStringExtra("experience");

        txtCategory.setText(category + " - " + experience);

        txtQuestion.setText("Click Next to generate AI questions");

        btnSpeak.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                    );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Answer the interview question"
            );

            try {

                startActivityForResult(
                        intent,
                        REQUEST_SPEECH
                );

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Speech recognition not supported",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnNext.setOnClickListener(v -> {

            if(questions.isEmpty()){

                txtStatus.setText("Generating First Question...");
                generateQuestions();

            }else{

                String answer =
                        etAnswer.getText().toString().trim();


                if(answer.isEmpty()){

                    txtStatus.setText("Please speak or type an answer first");

                    return;
                }

                interviewData.append(
                        "Question: "
                                + txtQuestion.getText().toString()
                                + "\n"
                );

                interviewData.append(
                        "Answer: "
                                + answer
                                + "\n\n"
                );

                userAnswers.add(answer);

                currentIndex++;

                if(currentIndex < questions.size()){

                    String nextQuestion =
                            questions.get(currentIndex);

                    txtQuestion.setText(nextQuestion);

                    speakQuestion(nextQuestion);

                   /*txtAnswer.setText("");*/
                    etAnswer.setText("");

                }else{

                    txtStatus.setText(
                            "Generating Next Question..."
                    );

                    generateQuestions();
                }
            }
        });

        btnEndInterview.setOnClickListener(v -> {

            interviewData.append(
                    "Question: "
                            + txtQuestion.getText().toString()
                            + "\n"
            );

            interviewData.append(
                    "Answer: "
                            + etAnswer.getText().toString()
                            + "\n\n"
            );

            evaluateInterview();
        });
    }
    private void generateQuestions() {
        new Thread(() -> {
            try {

                Log.d("CATEGORY", category);
                Log.d("EXPERIENCE", experience);

                String prompt =
                        "Generate ONE interview question for a " +
                                category +
                                " role at " +
                                experience +
                                " experience level. " +
                                "Return ONLY the question text. " +
                                "Do not add numbering, bullets, explanations, headings or markdown.";

                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);
                JSONArray parts = new JSONArray();
                parts.put(textPart);
                JSONObject content = new JSONObject();
                content.put("parts", parts);
                JSONArray contents = new JSONArray();
                contents.put(content);
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("contents", contents);

                RequestBody body = RequestBody.create(
                        bodyJson.toString(),
                        MediaType.parse("application/json")
                );

                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + API_KEY;

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);

                    if (!json.has("candidates")) {

                        runOnUiThread(() ->
                                txtStatus.setText(responseBody)
                        );

                        return;
                    }

                    JSONArray candidates = json.getJSONArray("candidates");
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject contentObj = candidate.getJSONObject("content");
                    JSONArray responseParts = contentObj.getJSONArray("parts");
                    String generatedText = responseParts.getJSONObject(0).getString("text");

                    questions.clear();

                    generatedText = generatedText.trim();

                    if (!generatedText.isEmpty()) {
                        questions.add(generatedText);
                    }

                    runOnUiThread(() -> {
                        txtStatus.setText(questions.size() + " Questions Generated");
                        currentIndex = 0;
                        if (!questions.isEmpty()) {
                            String firstQuestion = questions.get(0);

                            txtQuestion.setText(firstQuestion);

                            speakQuestion(firstQuestion);
                        }
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtStatus.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void evaluateInterview() {
        txtStatus.setText("Evaluating Interview...");
        new Thread(() -> {
            try {
                String prompt =
                        "Evaluate this interview and return ONLY valid JSON.\n\n" +

                                "{\n" +
                                "\"overall_score\":\"85%\",\n" +
                                "\"communication\":\"8/10\",\n" +
                                "\"technical_accuracy\":\"7/10\",\n" +
                                "\"confidence\":\"9/10\",\n" +
                                "\"key_strength\":\"...\",\n" +
                                "\"growth_area\":\"...\",\n" +
                                "\"recommendation\":\"...\"\n" +
                                "}\n\n" +

                                "Also evaluate communication skills, technical knowledge and confidence.\n\n" +

                                interviewData;

                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);
                JSONArray parts = new JSONArray();
                parts.put(textPart);
                JSONObject content = new JSONObject();
                content.put("parts", parts);
                JSONArray contents = new JSONArray();
                contents.put(content);
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("contents", contents);

                RequestBody body = RequestBody.create(
                        bodyJson.toString(),
                        MediaType.parse("application/json")
                );

                String url =
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                + API_KEY;

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);

                    if (json.has("candidates")) {
                        String rawReport = json.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        final String report = cleanJsonResponse(rawReport);

                        runOnUiThread(() -> {
                            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                                    FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

                            DatabaseReference historyRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(uid)
                                    .child("interviews");

                            String interviewId = historyRef.push().getKey();
                            String score = "N/A";

                            try {
                                JSONObject reportJson = new JSONObject(report);
                                score = reportJson.getString("overall_score");
                            } catch (Exception e) {
                                android.util.Log.e("JSON_PARSE_ERROR", "Failed to parse: " + report);
                            }

                            if (interviewId != null) {
                                historyRef.child(interviewId).child("report").setValue(report);
                                historyRef.child(interviewId).child("category").setValue(category);
                                historyRef.child(interviewId).child("score").setValue(score);
                                String date = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                                        .format(new java.util.Date());
                                historyRef.child(interviewId).child("date").setValue(date);
                            }

                            Intent intent = new Intent(QuestionActivity.this, EvaluationActivity.class);

                            Log.d("REPORT_JSON", report);
                            intent.putExtra("report", report);

                            intent.putExtra("interviewData", interviewData.toString());

                            startActivity(intent);

                            finish();
                        });
                    } else {
                        throw new Exception("No candidates in response: " + responseBody);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();

                Log.e("NEXT_QUESTION_ERROR",
                        e.getMessage(),
                        e);

                runOnUiThread(() -> {
                    txtStatus.setText(
                            "Error: " + e.getMessage()
                    );
                });
            }
        }).start();
    }

    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == REQUEST_SPEECH
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> result =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (result != null
                    && !result.isEmpty()) {

                String spokenText =
                        result.get(0);

                etAnswer.setText(spokenText);
                etAnswer.setSelection(etAnswer.getText().length());
            }
        }
    }
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            textToSpeech.setLanguage(
                    Locale.US
            );
        }
    }
    private void speakQuestion(String question) {

        if (textToSpeech != null) {

            textToSpeech.speak(
                    question,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
            );
        }
    }
    @Override
    protected void onDestroy() {

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }
}