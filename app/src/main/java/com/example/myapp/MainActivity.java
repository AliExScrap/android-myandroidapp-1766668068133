package com.example.myapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ConsoleRedirector.ConsoleListener {

    private TextView consoleOutput;
    private EditText consoleInput;
    private Button sendButton;
    private Button runButton;
    private Button clearButton;
    private ScrollView scrollView;
    private Handler mainHandler;
    private ExecutorService executor;
    private boolean isRunning = false;
    private boolean waitingForInput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        consoleOutput = findViewById(R.id.consoleOutput);
        consoleInput = findViewById(R.id.consoleInput);
        sendButton = findViewById(R.id.sendButton);
        runButton = findViewById(R.id.runButton);
        clearButton = findViewById(R.id.clearButton);
        scrollView = findViewById(R.id.scrollView);

        consoleOutput.setMovementMethod(new ScrollingMovementMethod());
        
        // Setup console redirector
        ConsoleRedirector.setup(this);

        sendButton.setOnClickListener(v -> sendInput());
        
        runButton.setOnClickListener(v -> {
            if (!isRunning) {
                runProgram();
            }
        });
        
        clearButton.setOnClickListener(v -> {
            consoleOutput.setText("");
        });

        consoleInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                sendInput();
                return true;
            }
            return false;
        });

        // Show welcome message
        appendOutput("╔════════════════════════════════════════╗\n", Color.CYAN);
        appendOutput("║     Java Console - Prêt à exécuter     ║\n", Color.CYAN);
        appendOutput("╚════════════════════════════════════════╝\n", Color.CYAN);
        appendOutput("\nAppuyez sur 'Exécuter' pour lancer le programme.\n\n", Color.GRAY);
    }

    private void sendInput() {
        String input = consoleInput.getText().toString();
        consoleInput.setText("");
        
        appendOutput("> " + input + "\n", Color.GREEN);
        ConsoleRedirector.writeInput(input);
        waitingForInput = false;
        updateInputState();
    }

    private void runProgram() {
        isRunning = true;
        runButton.setEnabled(false);
        runButton.setText("En cours...");
        
        appendOutput("\n--- Exécution du programme ---\n\n", Color.YELLOW);

        executor.execute(() -> {
            try {
                // Call the user's main method
                Main.main(new String[]{});
                
                mainHandler.post(() -> {
                    appendOutput("\n--- Programme terminé ---\n", Color.YELLOW);
                    isRunning = false;
                    runButton.setEnabled(true);
                    runButton.setText("Exécuter");
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    appendOutput("\n❌ Erreur: " + e.getMessage() + "\n", Color.RED);
                    e.printStackTrace();
                    isRunning = false;
                    runButton.setEnabled(true);
                    runButton.setText("Exécuter");
                });
            }
        });
    }

    private void appendOutput(String text, int color) {
        mainHandler.post(() -> {
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            consoleOutput.append(spannable);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void updateInputState() {
        mainHandler.post(() -> {
            consoleInput.setEnabled(waitingForInput || isRunning);
            sendButton.setEnabled(waitingForInput || isRunning);
            if (waitingForInput) {
                consoleInput.requestFocus();
            }
        });
    }

    @Override
    public void onOutput(String text) {
        appendOutput(text, Color.WHITE);
    }

    @Override
    public void onError(String text) {
        appendOutput(text, Color.RED);
    }

    @Override
    public void onInputRequested() {
        waitingForInput = true;
        updateInputState();
        mainHandler.post(() -> {
            appendOutput("📝 ", Color.YELLOW);
            consoleInput.requestFocus();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConsoleRedirector.restore();
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}