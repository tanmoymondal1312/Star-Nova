package com.mediaghor.starnova.Helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.rejowan.cutetoast.CuteToast;

public class WorkWithDevice {


    public static void copyTextToClipboard(Context context, String text) {
        if (text.isEmpty()) {
            CuteToast.ct(context, "No text to copied", CuteToast.LENGTH_SHORT, CuteToast.CONFUSE, true).show();
            return;
        }

        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard == null) {
                CuteToast.ct(context, "Clipboard service not available", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show();
                return;
            }

            // Create clip with label
            ClipData clip = ClipData.newPlainText("Copied from App", text);
            clipboard.setPrimaryClip(clip);

            // Optional: For Android 12+, check if copy was successful
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                clipboard.addPrimaryClipChangedListener(() -> {
                    // Copy confirmation (optional)
                    Log.i("Clipboard", "Clipboard changed - copy successful");
                });
            }

            // Show success with copied text preview
            String preview = text.length() > 30 ?
                    text.substring(0, 30) + "..." : text;
            CuteToast.ct(context, "Copied", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show();

        } catch (SecurityException e) {
            // Handle permission issues (rare on modern Android)
            CuteToast.ct(context, "Permission denied to access clipboard", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show();
        } catch (Exception e) {
            e.printStackTrace();
            CuteToast.ct(context, "Failed to copy text", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show();
        }
    }


}
