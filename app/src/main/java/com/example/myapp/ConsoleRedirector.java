package com.example.myapp;

import java.io.*;

/**
 * Console Redirector - Redirects System.in/out to Android UI
 */
public class ConsoleRedirector {
    
    public interface ConsoleListener {
        void onOutput(String text);
        void onError(String text);
        void onInputRequested();
    }
    
    private static ConsoleListener listener;
    private static PipedInputStream inputPipe;
    private static PipedOutputStream inputWriter;
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    private static InputStream originalIn;
    
    public static void setup(ConsoleListener consoleListener) {
        listener = consoleListener;
        
        // Save originals
        originalOut = System.out;
        originalErr = System.err;
        originalIn = System.in;
        
        // Create custom PrintStream for output
        PrintStream customOut = new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();
            
            @Override
            public void write(int b) {
                char c = (char) b;
                if (c == '\n') {
                    final String line = buffer.toString();
                    buffer = new StringBuilder();
                    if (listener != null) {
                        listener.onOutput(line + "\n");
                    }
                } else {
                    buffer.append(c);
                }
            }
            
            @Override
            public void flush() {
                if (buffer.length() > 0) {
                    final String line = buffer.toString();
                    buffer = new StringBuilder();
                    if (listener != null) {
                        listener.onOutput(line);
                    }
                }
            }
        }, true);
        
        PrintStream customErr = new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();
            
            @Override
            public void write(int b) {
                char c = (char) b;
                if (c == '\n') {
                    final String line = buffer.toString();
                    buffer = new StringBuilder();
                    if (listener != null) {
                        listener.onError(line + "\n");
                    }
                } else {
                    buffer.append(c);
                }
            }
        }, true);
        
        // Setup input pipe
        try {
            inputPipe = new PipedInputStream();
            inputWriter = new PipedOutputStream(inputPipe);
            System.setIn(inputPipe);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.setOut(customOut);
        System.setErr(customErr);
    }
    
    public static void writeInput(String input) {
        if (inputWriter != null) {
            try {
                inputWriter.write((input + "\n").getBytes());
                inputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void requestInput() {
        if (listener != null) {
            listener.onInputRequested();
        }
    }
    
    public static void restore() {
        if (originalOut != null) System.setOut(originalOut);
        if (originalErr != null) System.setErr(originalErr);
        if (originalIn != null) System.setIn(originalIn);
        try {
            if (inputWriter != null) inputWriter.close();
            if (inputPipe != null) inputPipe.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}