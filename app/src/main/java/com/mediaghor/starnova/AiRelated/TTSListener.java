package com.mediaghor.starnova.AiRelated;

public interface TTSListener {
    void onReady();
    void onStart();
    void onDone();
    void onError(String error);
}
