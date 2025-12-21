package com.mediaghor.starnova.AiRelated;


public interface SpeechRecognitionListener {
    /**
     * Called when the recognizer starts listening for speech.
     */
    void onReadyForSpeech();

    /**
     * Called continuously while speech is being detected.
     */
    void onListening();

    /**
     * Called when the speech input is finished and the final result is available.
     * @param recognizedText The final recognized text.
     */
    void onRecognitionResult(String recognizedText);

    /**
     * Called when a fatal error occurs.
     * @param errorMessage A user-friendly message describing the error.
     */
    void onRecognitionError(String errorMessage);
}