package com.mediaghor.starnova.AiRelated;

import android.os.Build;
import android.speech.tts.Voice;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

public class TTSManager {

    private TextToSpeech textToSpeech;
    private boolean isReady = false;
    private TTSListener ttsListener;

    public TTSManager(Context context, TTSListener listener) {
        this.ttsListener = listener;

        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {

                int result = textToSpeech.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    if (ttsListener != null) {
                        ttsListener.onError("Language not supported");
                    }

                } else {
                    isReady = true;

                    // DEFAULT VOICE CONFIG
                    textToSpeech.setPitch(1.0f);
                    textToSpeech.setSpeechRate(1.0f);

                    if (ttsListener != null) {
                        ttsListener.onReady(); // 🔥 READY CALLBACK
                    }
                }

            } else {
                if (ttsListener != null) {
                    ttsListener.onError("TTS initialization failed");
                }
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (ttsListener != null) ttsListener.onStart();
            }

            @Override
            public void onDone(String utteranceId) {
                if (ttsListener != null) ttsListener.onDone();
            }

            @Override
            public void onError(String utteranceId) {
                if (ttsListener != null) ttsListener.onError("Speech error");
            }
        });
    }

    public void speak(String text) {
        if (!isReady || text == null || text.trim().isEmpty()) return;

        String utteranceId = "TTS_" + System.currentTimeMillis();

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

        textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                params,
                utteranceId
        );
    }

    /* ---------------- VOICE CONTROLS ---------------- */

    public void setPitch(float pitch) {
        if (textToSpeech != null) {
            textToSpeech.setPitch(pitch); // 0.5–2.0
        }
    }

    public void setSpeechRate(float rate) {
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(rate); // 0.5–2.0
        }
    }

    public void setVoiceByName(String voiceName) {
        if (textToSpeech == null) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null) return;

        for (Voice voice : voices) {
            if (voice.getName().equalsIgnoreCase(voiceName)) {
                textToSpeech.setVoice(voice);
                Log.d("TTS", "Voice set: " + voice.getName());
                return;
            }
        }

        Log.e("TTS", "Voice not found: " + voiceName);
    }


    public void logAvailableVoices() {
        if (textToSpeech == null) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.e("TTS", "Voice API not supported");
            return;
        }

        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null) {
            Log.e("TTS", "No voices available");
            return;
        }

        for (Voice voice : voices) {
            Log.d("TTS_VOICE",
                    "Name: " + voice.getName()
                            + " | Locale: " + voice.getLocale()
                            + " | Quality: " + voice.getQuality()
                            + " | Network: " + voice.isNetworkConnectionRequired()
            );
        }
    }


    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    public void stop() {
        if (textToSpeech != null) textToSpeech.stop();
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
