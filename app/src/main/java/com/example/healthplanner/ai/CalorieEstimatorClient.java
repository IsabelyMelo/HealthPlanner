package com.example.healthplanner.ai;

import com.example.healthplanner.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalorieEstimatorClient {
    private static final String INSUFFICIENT_RESPONSE = "INSUFFICIENT";
    private static final Pattern CALORIE_PATTERN = Pattern.compile("\\b\\d{1,5}\\b");
    private static final int MAX_COMPLETION_TOKENS = 64;

    public EstimateResult estimateCalories(String mealDescription) throws IOException, JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", BuildConfig.AI_CALORIE_MODEL);
        requestBody.put("temperature", 0);
        requestBody.put("max_completion_tokens", MAX_COMPLETION_TOKENS);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "Voce estima calorias de refeicoes em portugues. Use somente a descricao enviada pelo usuario. Responda apenas com um numero inteiro de kcal, sem unidade. Estime quando houver alimento identificavel e alguma quantidade, peso, volume, unidade ou porcao comum, mesmo que faltem detalhes de preparo. Use valores medios quando necessario. Nunca responda INSUFFICIENT para descricoes como '100 gramas de arroz refogado', '1 banana media' ou '2 fatias de pao'. Responda exatamente INSUFFICIENT somente quando a descricao nao tiver alimento identificavel ou nao tiver quantidade/porcao minima."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", buildUserPrompt(mealDescription)));
        requestBody.put("messages", messages);

        String responseBody = postJson(requestBody.toString());
        JSONObject responseJson = new JSONObject(responseBody);
        String content = responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        if (content.isEmpty()) {
            throw new IOException("A IA nao retornou texto para a estimativa.");
        }

        return parseEstimate(content);
    }

    private String buildUserPrompt(String mealDescription) {
        String normalizedDescription = mealDescription == null ? "" : mealDescription.trim();
        return "Descricao da refeicao: " + normalizedDescription;
    }

    private String postJson(String body) throws IOException {
        URL url = new URL(BuildConfig.AI_CALORIE_API_BASE_URL);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.AI_CALORIE_API_KEY);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");

            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream responseStream = responseCode >= 200 && responseCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = readStream(responseStream);

            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException(buildHttpErrorMessage(responseCode, responseBody));
            }

            return responseBody;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String buildHttpErrorMessage(int responseCode, String responseBody) {
        String apiMessage = extractApiErrorMessage(responseBody);
        if (apiMessage.isEmpty()) {
            return "Erro na API de IA. Codigo HTTP: " + responseCode;
        }

        return "Erro na API de IA: " + apiMessage;
    }

    private String extractApiErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "";
        }

        try {
            JSONObject responseJson = new JSONObject(responseBody);
            JSONObject error = responseJson.optJSONObject("error");
            if (error == null) {
                return "";
            }

            return error.optString("message", "");
        } catch (JSONException e) {
            return "";
        }
    }

    private EstimateResult parseEstimate(String content) {
        String normalizedContent = content.trim();
        if (normalizedContent.equalsIgnoreCase(INSUFFICIENT_RESPONSE)
                || normalizedContent.toLowerCase(Locale.ROOT).contains("insufficient")) {
            return EstimateResult.insufficient();
        }

        Matcher matcher = CALORIE_PATTERN.matcher(normalizedContent);
        if (matcher.find()) {
            int calories = Integer.parseInt(matcher.group());
            if (calories > 0) {
                return EstimateResult.success(calories);
            }
        }

        return EstimateResult.insufficient();
    }

    public static class EstimateResult {
        private final Integer calories;

        private EstimateResult(Integer calories) {
            this.calories = calories;
        }

        public static EstimateResult success(int calories) {
            return new EstimateResult(calories);
        }

        public static EstimateResult insufficient() {
            return new EstimateResult(null);
        }

        public boolean hasCalories() {
            return calories != null;
        }

        public int getCalories() {
            return calories == null ? 0 : calories;
        }
    }
}
