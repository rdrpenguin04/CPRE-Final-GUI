package com.waffl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class RobotContext implements LineListener {
    private Socket socket;
    private DataOutputStream outputStream;
    private InputStreamReader reader;
    private Queue<String> txQueue;
    private boolean sending = false;
    private double curScanDist = 0;
    private List<RobotMovementListener> robotMovementListeners;
    private List<RobotScanListener> robotScanListeners;

    public RobotContext() throws UnknownHostException, IOException {
        socket = new Socket("192.168.1.1", 288);
        outputStream = new DataOutputStream(socket.getOutputStream());
        reader = new InputStreamReader(socket.getInputStream());
        new Thread(reader).start();
        addLineListener(this);
        txQueue = new ArrayDeque<>();
        robotMovementListeners = new ArrayList<>();
        robotScanListeners = new ArrayList<>();
    }

    public void addLineListener(LineListener listener) {
        reader.addLineListener(listener);
    }

    public void addRobotMovementListener(RobotMovementListener listener) {
        robotMovementListeners.add(listener);
    }

    public void addRobotScanListener(RobotScanListener listener) {
        robotScanListeners.add(listener);
    }

    public void tx(String tx) {
        // Send string to socket
        System.out.println("TX queue: " + tx);
        if (tx.equals("k")) {
            txNow("k");
            txQueue.clear();
            sending = false;
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
            robotMovementListeners.forEach((listener) -> { listener.robotStartedMovement(); });
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

    char prevDir = ' ';

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
                double angle = lineScanner.nextDouble();
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                    return;
                }
                double dist = lineScanner.nextDouble();
                if (curScanDist != 0 && dist > curScanDist) {
                    dist = curScanDist;
                }
                double trueDist = dist;
                robotScanListeners.forEach((listener) -> {
                    listener.ir(angle, trueDist);
                });
            }
        } else if (line.startsWith("PING")) {
            try (Scanner lineScanner = new Scanner(line)) {
                if (!lineScanner.next().equals("PING")) {
                    System.out.println("invalid format: PING missing");
                    return;
                }
                double angle = lineScanner.nextDouble();
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                    return;
                }
                double dist = lineScanner.nextDouble();
                if (curScanDist != 0 && dist > curScanDist) {
                    dist = curScanDist;
                }
                double trueDist = dist;
                robotScanListeners.forEach((listener) -> {
                    listener.ping(angle, trueDist);
                });
            }
        } else if (line.startsWith("right")) {
            try (Scanner lineScanner = new Scanner(line)) {
                if (!lineScanner.next().equals("right")) {
                    System.out.println("invalid format: right missing");
                    return;
                }
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                    return;
                }
                double angle = lineScanner.nextDouble();
                robotMovementListeners.forEach((listener) -> {
                    if (prevDir != 'r') listener.robotStartedMovement();
                    listener.robotTurned(-angle);
                });
                prevDir = 'r';
            }
        } else if (line.startsWith("left")) {
            try (Scanner lineScanner = new Scanner(line)) {
                if (!lineScanner.next().equals("left")) {
                    System.out.println("invalid format: left missing");
                    return;
                }
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                    return;
                }
                double angle = lineScanner.nextDouble();
                robotMovementListeners.forEach((listener) -> {
                    if (prevDir != 'l') listener.robotStartedMovement();
                    listener.robotTurned(-angle);
                });
                prevDir = 'l';
            }
        } else if (line.startsWith("forward")) {
            try (Scanner lineScanner = new Scanner(line)) {
                if (!lineScanner.next().equals("forward")) {
                    System.out.println("invalid format: forward missing");
                    return;
                }
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                    return;
                }
                double dist = lineScanner.nextDouble();
                robotMovementListeners.forEach((listener) -> {
                    if (prevDir != 'f') listener.robotStartedMovement();
                    listener.robotMoved(dist);
                });
                prevDir = 'f';
            }
        } else if (line.startsWith("backward")) {
            try (Scanner lineScanner = new Scanner(line)) {
                if (!lineScanner.next().equals("backward")) {
                    System.out.println("invalid format: backward missing");
                    return;
                }
                if (!lineScanner.next().equals("=")) {
                    System.out.println("invalid format: = missing");
                    return;
                }
                double dist = lineScanner.nextDouble();
                robotMovementListeners.forEach((listener) -> {
                    if (prevDir != 'b') listener.robotStartedMovement();
                    listener.robotMoved(dist);
                });
                prevDir = 'b';
            }
        }
    }
}
