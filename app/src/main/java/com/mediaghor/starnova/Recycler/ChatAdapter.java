package com.mediaghor.starnova.Recycler;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.mediaghor.starnova.AiRelated.GroqAIClint;
import com.mediaghor.starnova.AiRelated.AiResponseCallback;
import com.mediaghor.starnova.AiRelated.TranslateCallback;
import com.mediaghor.starnova.AiRelated.TranslateClient;
import com.mediaghor.starnova.Helper.WorkWithDevice;
import com.mediaghor.starnova.Models.Message;
import com.mediaghor.starnova.R;
import com.rejowan.cutetoast.CuteToast;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private Context context;
    private static final String TAG = "CHAT_ADAPTER";
    private RecyclerView recyclerView;


    public ChatAdapter(List<Message> messages, Context context,RecyclerView recyclerView) {
        this.messages = messages;
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ai, parent, false);
            return new AIVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = messages.get(position);

        if (holder instanceof UserVH) {
            ((UserVH) holder).msg.setText(m.text);

        } else {
            AIVH aiHolder = (AIVH) holder;
            aiHolder.translateProgressBar.setVisibility(View.GONE);
            // Display either original or translated text
            if (m.isTranslated && m.translatedText != null) {
                aiHolder.msg.setText(m.translatedText);
                aiHolder.BtnTranslate.setChecked(true);
                aiHolder.BtnTranslate.setVisibility(View.VISIBLE);
                aiHolder.BtnTranslate.setChecked(true);
            } else {
                aiHolder.msg.setText(m.text);
                aiHolder.BtnTranslate.setVisibility(View.VISIBLE);
                aiHolder.BtnTranslate.setChecked(false);
            }

            // Remove previous listener to avoid recycling issues
            aiHolder.BtnTranslate.setOnCheckedChangeListener(null);

            aiHolder.BtnTranslate.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                     //If already translated, show cached translation
//                    if (m.translatedText != null) {
//                        aiHolder.msg.setText(m.translatedText);
//                        m.isTranslated = true;
//                        return;
//                    }

                    // UX feedback
                    aiHolder.translateProgressBar.setVisibility(View.VISIBLE);
                    aiHolder.BtnTranslate.setVisibility(View.GONE);
                    aiHolder.translateProgressBar.setIndeterminate(true);

                    TranslateClient.translate(
                            context, // Context
                            m.text,
                            "en",
                            "bn",
                            new TranslateCallback() {
                                @Override
                                public void onSuccess(String translatedText) {
                                    m.translatedText = translatedText;
                                    m.isTranslated = true;

                                    // Update UI on main thread
                                    ((Activity) context).runOnUiThread(() -> {
                                        // Check if ViewHolder is still showing this message
                                        if (messages.get(position) == m) {
                                            aiHolder.msg.setText(translatedText);
                                            CuteToast.ct(context, "Translate Success", CuteToast.LENGTH_SHORT, CuteToast.SUCCESS, true).show();
                                            aiHolder.translateProgressBar.setVisibility(View.GONE);
                                            aiHolder.BtnTranslate.setVisibility(View.VISIBLE);
                                            aiHolder.BtnTranslate.setChecked(true);



                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    m.isTranslated = false;

                                    // Update UI on main thread
                                    ((Activity) context).runOnUiThread(() -> {
                                        // Check if ViewHolder is still showing this message
                                        if (messages.get(position) == m) {
                                            CuteToast.ct(context, "Failed to translate text", CuteToast.LENGTH_SHORT, CuteToast.ERROR, true).show();
                                            aiHolder.BtnTranslate.setChecked(false);

                                        }
                                    });
                                }
                            }
                    );

                } else {
                    // Back to English
                    aiHolder.msg.setText(m.text);
                    m.isTranslated = false;
                    aiHolder.translateProgressBar.setVisibility(View.GONE);
                    aiHolder.BtnTranslate.setVisibility(View.VISIBLE);
                    aiHolder.BtnTranslate.setChecked(false);

                }
            });

            aiHolder.copyText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WorkWithDevice.copyTextToClipboard(context,aiHolder.msg.getText().toString());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ================= ViewHolders =================

    static class UserVH extends RecyclerView.ViewHolder {
        TextView msg;
        UserVH(View v) {
            super(v);
            msg = v.findViewById(R.id.user_text);
        }
    }

    static class AIVH extends RecyclerView.ViewHolder {
        TextView msg;
        CheckBox BtnTranslate;
        ProgressBar translateProgressBar;

        AppCompatImageButton copyText;

        AIVH(View v) {
            super(v);
            msg = v.findViewById(R.id.ai_text);
            BtnTranslate = v.findViewById(R.id.id_translate_btn);
            translateProgressBar = v.findViewById(R.id.translating_ai_item_progressbar);
            copyText = v.findViewById(R.id.copy_text_ai_adapter);
        }
    }
}
