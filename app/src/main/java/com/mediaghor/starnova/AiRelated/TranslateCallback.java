package com.mediaghor.starnova.AiRelated;

public interface TranslateCallback {
    void onSuccess(String translatedText);
    void onError(String error);
}
