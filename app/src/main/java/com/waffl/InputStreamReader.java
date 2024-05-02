package com.waffl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InputStreamReader implements Runnable {
    protected InputStream in;
    protected List<LineListener> listeners;

    public InputStreamReader(InputStream in) {
        this.in = in;
        this.listeners = new ArrayList<LineListener>();
    }

    public void addLineListener(LineListener l) {
        this.listeners.add(l);
    }

    public void run() {
        String lineBuffer = "";
        while (true) {
            try {
                int next = in.read();
                lineBuffer += (char) next;
                if (next == '\n') {
                    for (LineListener l : listeners) {
                        l.lineReceived(lineBuffer);
                    }
                    lineBuffer = "";
                }
            } catch (IOException e) {
            }
        }
    }
}
