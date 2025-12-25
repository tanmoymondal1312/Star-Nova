package com.mediaghor.starnova.Fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.mediaghor.starnova.AiRelated.AiResponseCallback;
import com.mediaghor.starnova.AiRelated.CustomSpeechRecognizer;
import com.mediaghor.starnova.AiRelated.GroqAIClint;
import com.mediaghor.starnova.AiRelated.SpeechRecognitionListener;
import com.mediaghor.starnova.AiRelated.TTSListener;
import com.mediaghor.starnova.AiRelated.TTSManager;
import com.mediaghor.starnova.AiRelated.TranslateCallback;
import com.mediaghor.starnova.AiRelated.TranslateClient;
import com.mediaghor.starnova.Helper.AiResponseEditor;
import com.mediaghor.starnova.Models.BottomNavViewModel;
import com.mediaghor.starnova.Models.Message;
import com.mediaghor.starnova.Permissions.MicPermissionHelper;
import com.mediaghor.starnova.R;
import com.mediaghor.starnova.Recycler.ChatAdapter;
import com.mediaghor.starnova.UI.UiAnimationManager;

import java.util.ArrayList;
import java.util.List;

public class AiFragment extends Fragment {

    // =========================================================================
    // CONSTANTS
    // =========================================================================
    private static final String TAG = "AiFragment";
    private static final int ERROR_RESTART_DELAY = 1000; // ms

    // =========================================================================
    // UI COMPONENTS
    // =========================================================================
    private RecyclerView recyclerView;
    private EditText inputBox;
    TextView question;
    private AppCompatImageButton sendButton;
    private View inputContainer;
    private CheckBox btnMicrophone;
    private LottieAnimationView avatar,avatar_bg,equalizer;

    // =========================================================================
    // MANAGERS & HELPERS
    // =========================================================================
    private ChatAdapter chatAdapter;
    private BottomNavViewModel bottomNavViewModel;
    private MicPermissionHelper micPermissionHelper;
    private CustomSpeechRecognizer speechRecognizer;
    private TTSManager ttsManager;
    private UiAnimationManager uiAnimationManager;

    // =========================================================================
    // DATA
    // =========================================================================
    private final List<Message> messageList = new ArrayList<>();

    // =========================================================================
    // STATE MANAGEMENT
    // =========================================================================
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int bottomNavHeight = 0;
    private boolean isProcessingAiResponse = false;

    // =========================================================================
    // FRAGMENT LIFECYCLE
    // =========================================================================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);
        initializeViews(view);
        setupViewModel();
        setupRecyclerView();
        setupUiComponents();
        setupKeyboardInsets();
        initializeManagers();
        requestMicrophonePermission();






        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }

    @Override
    public void onDestroyView() {
        cleanupResources();
        super.onDestroyView();
    }

    // =========================================================================
    // INITIALIZATION METHODS
    // =========================================================================

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.chat_recycler);
        inputBox = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_message_button);
        btnMicrophone = view.findViewById(R.id.id_microphone_fa);
        inputContainer = view.findViewById(R.id.input_container);
        avatar = view.findViewById(R.id.id_avatar_animation_fa);
        avatar_bg = view.findViewById(R.id.id_avtar_bg_animation_fa);
        equalizer = view.findViewById(R.id.id_equalizer_fa);
        question = view.findViewById(R.id.id_question);


    }

    private void setupViewModel() {
        bottomNavViewModel = new ViewModelProvider(requireActivity())
                .get(BottomNavViewModel.class);

        bottomNavViewModel.getNavHeight().observe(getViewLifecycleOwner(), height -> {
            bottomNavHeight = height;
            ViewCompat.requestApplyInsets(inputContainer);
        });
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, requireContext(),recyclerView);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                Log.d(TAG, "Adapter: " + itemCount + " items inserted at " + positionStart);
            }
        });
    }

    private void setupUiComponents() {
        sendButton.setOnClickListener(v -> handleSendButtonClick());

        // Setup microphone checkbox listener
        btnMicrophone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startListening();
                } else {
                    stopListening();
                }
            }
        });
        initializeAnimations();
        animAiTalking(false);
        animEqualizer(false);



    }

    private void initializeManagers() {
        initializeSpeechRecognizer();
        initializeTTSManager();
        uiAnimationManager = new UiAnimationManager(requireContext());

    }

    // =========================================================================
    // SPEECH RECOGNITION MANAGEMENT
    // =========================================================================

    private void initializeSpeechRecognizer() {
        speechRecognizer = new CustomSpeechRecognizer(requireContext(),
                new SpeechRecognitionDelegate());
    }

    private void initializeTTSManager() {
        ttsManager = new TTSManager(requireContext(), new TTSManagerDelegate());
    }

    private void requestMicrophonePermission() {
        micPermissionHelper = new MicPermissionHelper(
                this,
                requireActivity(),
                new MicPermissionHelper.PermissionCallback() {
                    @Override
                    public void onGranted() {
                        Log.d(TAG, "Microphone permission granted");
                        // No auto-start - user will click microphone button
                    }

                    @Override
                    public void onDenied() {
                        Log.w(TAG, "Microphone permission denied");
//                        Toast.makeText(requireContext(),
//                                "Microphone permission is required for voice input",
//                                Toast.LENGTH_LONG).show();
                        // Disable microphone button if permission denied
                        btnMicrophone.setEnabled(false);
                    }
                }
        );
        micPermissionHelper.requestMicPermission();
    }

    // =========================================================================
    // LISTENING CONTROL
    // =========================================================================

    private void startListening() {
        if (speechRecognizer == null || isProcessingAiResponse) {
            return;
        }

        try {
            speechRecognizer.startListening();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Speech recognizer busy", e);
            btnMicrophone.setChecked(false);
            //Toast.makeText(requireContext(), "Speech recognizer not available", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.e(TAG, "Microphone permission not granted", e);
            btnMicrophone.setChecked(false);
            //Toast.makeText(requireContext(), "Microphone permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stop();
        }
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void stopListeningTemporarily() {
        if (speechRecognizer != null) {
            speechRecognizer.stop();
        }
    }

    // =========================================================================
    // MESSAGE HANDLING
    // =========================================================================

    private void handleSendButtonClick() {
        String text = inputBox.getText().toString().trim();
        if (!text.isEmpty()) {
            processUserMessage(text);
            inputBox.setText("");
        }
    }

    private void processUserMessage(String message) {
        stopListeningTemporarily();
        btnMicrophone.setChecked(false);
        addMessageToChat(message, true);
        sendToAi(message);
    }

    private void processVoiceInput(String recognizedText) {
        if (recognizedText != null && !recognizedText.trim().isEmpty()) {
            addMessageToChat(recognizedText, true);
            sendToAi(recognizedText);
        }
        // Uncheck microphone after processing
        btnMicrophone.setChecked(false);
    }

    private void addMessageToChat(String text, boolean isUserMessage) {
        messageList.add(new Message(text, isUserMessage));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    // =========================================================================
    // AI INTEGRATION
    // =========================================================================

    private void sendInitialGreeting() {
        runOnUiThreadIfAttached(() -> {
            sendToAi("Hi");
        });
    }

    private void sendToAi(String message) {
        if (!isAdded()) {
            Log.d(TAG, "Fragment detached, skipping AI request");
            return;
        }
        isProcessingAiResponse = true;


        GroqAIClint.sendMessage(message,"teacher", new AiResponseCallback() {
            @Override
            public void onSuccess(String aiMessage) {
                Log.d(TAG,"Direct Response From AI: "+aiMessage);
                runOnUiThreadIfAttached(() -> {
                    handleAiResponseSuccess(AiResponseEditor.RemoveQuestionCover(aiMessage));
                    uiAnimationManager.setTextAnimated(
                            AiResponseEditor.QuestionExtractor(aiMessage),
                            question,
                            200
                    );

                });

            }

            @Override
            public void onFailure(String error) {
                runOnUiThreadIfAttached(() -> {
                    handleAiResponseFailure(error);
                });
            }
        });



    }

    private void handleAiResponseSuccess(String aiMessage) {
        Log.d(TAG, "AI response received: " + aiMessage);
        addMessageToChat(aiMessage, false);
        isProcessingAiResponse = false;

        // Speak the response
        ttsManager.speak(aiMessage);
    }

    private void handleAiResponseFailure(String error) {
        Log.e(TAG, "AI request failed: " + error);
        addMessageToChat("Sorry, I encountered an error. Please try again.", false);
        isProcessingAiResponse = false;
    }

    // =========================================================================
    // UI UTILITIES
    // =========================================================================

    private void initializeAnimations() {
        // Set animations
        avatar.setAnimation("human_talking.json");
        avatar_bg.setAnimation("ai_talking.json");
        equalizer.setAnimation("equalizer.json");

        // Start and immediately pause to load animations
        avatar.playAnimation();
        avatar_bg.playAnimation();
        equalizer.playAnimation();

        // Set to desired frames after a small delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                avatar.setFrame(28);
                avatar_bg.setFrame(180);
                equalizer.setFrame(128);

                avatar.pauseAnimation();
                avatar_bg.pauseAnimation();
                equalizer.pauseAnimation();
            }
        }, 100);
    }

    // Simplified methods without complex listeners
    private void animAiTalking(boolean state) {
        if (state) {
            avatar.loop(true);
            avatar_bg.loop(true);
            avatar.playAnimation();
            avatar_bg.playAnimation();
        } else {
            avatar.setFrame(28);
            avatar_bg.setFrame(180);
            avatar.pauseAnimation();
            avatar_bg.pauseAnimation();
        }
    }

    private void animEqualizer(boolean state) {
        if (state) {
            equalizer.loop(true);
            equalizer.playAnimation();
        } else {
            equalizer.setFrame(128);
            equalizer.pauseAnimation();
        }
    }


    private void setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(inputContainer, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int targetMargin = Math.max(0, imeHeight - bottomNavHeight - 30);

            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) inputContainer.getLayoutParams();

            if (params.bottomMargin != targetMargin) {
                animateBottomMarginChange(params, targetMargin);
            }

            return insets;
        });
    }

    private void animateBottomMarginChange(ViewGroup.MarginLayoutParams params, int targetMargin) {
        ValueAnimator animator = ValueAnimator.ofInt(params.bottomMargin, targetMargin);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            params.bottomMargin = (int) animation.getAnimatedValue();
            inputContainer.setLayoutParams(params);
        });
        animator.start();
    }

    private void runOnUiThreadIfAttached(Runnable runnable) {
        if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
            Log.d(TAG, "Fragment not attached, skipping UI update");
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                runnable.run();
            }
        });
    }

    // =========================================================================
    // CLEANUP
    // =========================================================================

    private void cleanupResources() {
        stopListening();

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        if (ttsManager != null) {
            ttsManager.shutdown();
            ttsManager = null;
        }

        mainHandler.removeCallbacksAndMessages(null);
    }

    // =========================================================================
    // DELEGATE CLASSES FOR SEPARATION OF CONCERNS
    // =========================================================================

    private class SpeechRecognitionDelegate implements SpeechRecognitionListener {
        @Override
        public void onReadyForSpeech() {

            Log.d(TAG, "Speech recognizer ready");
        }

        @Override
        public void onListening() {
            // Already handled in startListening()
            requireActivity().runOnUiThread(() -> {
                boolean shouldShow = isAdded() && getActivity() != null && !getActivity().isFinishing();
                Log.d(TAG, "onStart UI Thread - shouldShow: " + shouldShow);
                if (shouldShow) {
                    animEqualizer(true);
                }
            });
        }

        @Override
        public void onRecognitionResult(String recognizedText) {
            Log.d(TAG, "Speech recognized: " + recognizedText);
            runOnUiThreadIfAttached(() -> {
                processVoiceInput(recognizedText);
                animEqualizer(false);
            });
        }

        @Override
        public void onRecognitionError(String errorMessage) {
            Log.e(TAG, "Speech recognition error: " + errorMessage);
            runOnUiThreadIfAttached(() -> {
                btnMicrophone.setChecked(false);
                animEqualizer(false);
//                Toast.makeText(requireContext(), "Speech recognition error: " + errorMessage,
//                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private class TTSManagerDelegate implements TTSListener {
        @Override
        public void onReady() {
            sendInitialGreeting();
        }

        @Override
        public void onStart() {
            Log.d(TAG, "TTS started speaking.................................");
            Log.d(TAG, "onStart - isAdded: " + isAdded() + ", Activity: " + getActivity());

            requireActivity().runOnUiThread(() -> {
                boolean shouldShow = isAdded() && getActivity() != null && !getActivity().isFinishing();
                Log.d(TAG, "onStart UI Thread - shouldShow: " + shouldShow);
                if (shouldShow) {
                    animAiTalking(true);
                    Log.d(TAG, "Toast shown for TTS start");
                }
            });
            stopListeningTemporarily();
        }

        @Override
        public void onDone() {
            Log.d(TAG, "TTS finished speaking>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.d(TAG, "onDone - isAdded: " + isAdded() + ", Activity: " + getActivity());

            requireActivity().runOnUiThread(() -> {
                boolean shouldShow = isAdded() && getActivity() != null && !getActivity().isFinishing();
                Log.d(TAG, "onDone UI Thread - shouldShow: " + shouldShow);
                if (shouldShow) {
                    animAiTalking(false);
                    Log.d(TAG, "Toast shown for TTS done");
                }
            });
        }

        @Override
        public void onError(String error) {
            Log.e(TAG, "TTS error: " + error);
            requireActivity().runOnUiThread(() -> {
                boolean shouldShow = isAdded() && getActivity() != null && !getActivity().isFinishing();
                Log.d(TAG, "onDone UI Thread - shouldShow: " + shouldShow);
                if (shouldShow) {
                    animAiTalking(false);
                    Log.d(TAG, "Toast shown for TTS done");
                }
            });

        }
    }
}