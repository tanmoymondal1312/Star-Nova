package com.mediaghor.starnova.AiRelated;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class CustomSpeechRecognizer implements RecognitionListener {

    private static final String TAG = "CustomSpeechRecognizer";
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private SpeechRecognitionListener listener;

    // The required 3-second pause for recognition to stop
    private static final long SPEECH_COMPLETE_SILENCE_MILLIS = 3000;

    public CustomSpeechRecognizer(Context context, SpeechRecognitionListener listener) {
        this.listener = listener;

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(this);
        } else {
            listener.onRecognitionError("Speech Recognition is not available on this device.");
            return;
        }

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());

        // 3-second pause requirement
        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                SPEECH_COMPLETE_SILENCE_MILLIS
        );
        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                SPEECH_COMPLETE_SILENCE_MILLIS
        );

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }

    public void startListening() {
        if (speechRecognizer != null) {
            try {
                speechRecognizer.startListening(recognizerIntent);
                Log.d(TAG, "Speech recognition started.");
            } catch (Exception e) {
                Log.e(TAG, "Error starting listening: " + e.getMessage());
                listener.onRecognitionError("Error starting: " + e.getMessage());
            }
        }
    }

    public void stop() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            Log.d(TAG, "Speech recognition stopped.");
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
            Log.d(TAG, "Speech recognizer destroyed.");
        }
    }

    // --- RecognitionListener Interface ---

    @Override
    public void onReadyForSpeech(Bundle params) {
        listener.onReadyForSpeech();
        Log.d(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        listener.onListening();
        Log.d(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // Optional: visual feedback
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // Not used
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech - processing...");
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String recognizedText = matches.get(0);
            listener.onRecognitionResult(recognizedText);
            Log.d(TAG, "Final Result: " + recognizedText);
        } else {
            // Send empty result instead of error for no-match
            listener.onRecognitionResult("");
        }
        // IMPORTANT: Fragment now controls the loop, so DON'T call restart here
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            Log.d(TAG, "Partial Result: " + matches.get(0));
        }
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        listener.onRecognitionError(errorMessage);
        Log.e(TAG, "ERROR: " + errorMessage);
        // IMPORTANT: Fragment now controls error restart
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // Not used
    }

    public static String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No recognition result matched";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER: return "Error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
            default: return "Unknown recognition error: " + errorCode;
        }
    }
}