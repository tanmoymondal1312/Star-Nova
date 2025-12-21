package com.mediaghor.starnova.Models;

public class Message {
    public String text;
    public boolean isUser;  // true = user, false = AI

    // New fields for translation
    public boolean isTranslated = false;  // default: not translated
    public String translatedText = null;  // default: null

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }
}
