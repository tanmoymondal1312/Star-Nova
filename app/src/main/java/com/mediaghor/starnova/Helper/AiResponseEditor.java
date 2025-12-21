package com.mediaghor.starnova.Helper;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiResponseEditor {

    // Extracts question wrapped with (*  *)
    public static String QuestionExtractor(String response) {

        if (response == null || response.isEmpty()) {
            return null;
        }

        // Match (* question *)
        Pattern pattern = Pattern.compile("\\(\\*\\s*(.*?)\\s*\\*\\)");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // Return ONLY the question text
            return matcher.group(1).trim();
        }

        return null;
    }

    public static String RemoveQuestionCover(String response) {

        if (response == null || response.isEmpty()) {
            return response;
        }

        // Remove (* and *) but keep the question text
        return response.replaceAll("\\(\\*\\s*(.*?)\\s*\\*\\)", "$1");
    }


}
