package com.waffl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

public class RobotContext implements LineListener {
    private Socket socket;
    private DataOutputStream outputStream;
    private InputStreamReader reader;
    private Queue<String> txQueue;
    private boolean sending = false;
    private double curScanDist = 0;

    public RobotContext() throws UnknownHostException, IOException {
        socket = new Socket("192.168.1.1", 288);
        outputStream = new DataOutputStream(socket.getOutputStream());
        reader = new InputStreamReader(socket.getInputStream());
        new Thread(reader).start();
        addLineListener(this);
        txQueue = new ArrayDeque<>();
    }

    public void addLineListener(LineListener listener) {
        reader.addLineListener(listener);
    }

    public void tx(String tx) {
        // Send string to socket
        System.out.println("TX queue: " + tx);
        if (tx.equals("k")) {
            txNow("k");
            txQueue.clear();
        } else if (sending) {
            txQueue.add(tx);
        } else if (!sending) {
            txNow(tx);
        }
    }

    public void txNow(String tx) {
        // Actually send string to socket
        if (tx != null) {
            System.out.println("TX  send: " + tx);
            tx += '\r';
            try {
                sending = true;
                outputStream.writeBytes(tx);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            sending = false;
        }
    }

    public void startScan(double maxDist, int startAngle, int endAngle, int numPolls) {
        curScanDist = maxDist;
        tx("x i " + startAngle + " " + endAngle + " " + numPolls);
    }

    public void move(int dist) {
        tx("w " + dist);
    }

    public void turn(int angle) {
        while (angle < -180)
            angle += 360;
        while (angle > 180)
            angle -= 360;

        if (angle < 0) {
            tx("a " + (-angle));
        } else {
            tx("d " + angle);
        }
    }

    @Override
    public void lineReceived(String line) {
        if (line.startsWith("Ready")) {
            txNow(txQueue.poll());
        } else if (line.startsWith("IR")) {
            try (Scanner lineScanner = new Scanner(line)) {
                if (!lineScanner.next().equals("IR")) {
                    System.out.println("invalid format: IR missing");
                    return;
                }
                float angle = lineScanner.nextFloat();
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                }
                float dist = lineScanner.nextFloat();
                System.out.println("angle: " + angle + ", dist: " + dist);
            }
        }
    }
}
