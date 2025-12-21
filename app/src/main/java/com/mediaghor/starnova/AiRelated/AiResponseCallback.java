package com.mediaghor.starnova.AiRelated;

public interface AiResponseCallback {
    void onSuccess(String aiMessage);
    void onFailure(String error);
}
