package com.waffl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RobotCanvas extends JPanel {
    private static final double ROBOT_DIAMETER = 34;
    private static final double ROBOT_INNER_DIAMETER = 30;
    private static final double ROBOT_BASEBOARD_WIDTH = 20;
    private static final double ROBOT_BASEBOARD_HEIGHT = 10;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1920;

    private Point2D robotPos = new Point2D.Double(0.0, 0.0);
    private double robotAngle = -90.0;

    private Point2D robotPosMark;
    private double robotAngleMark;

    private Point2D arcStart = null;
    private Point2D arcEnd = null;
    private double arcExtent;
    private double arcMidAngle;
    private double arcRadius;

    private Point2D lineEnd = null;
    private double moveAngle;
    private double moveLen;

    private List<Point2D> scanPoints = new ArrayList<>();
    private List<Boolean> scanPointIsPing = new ArrayList<>();

    private static Point2D toScreenSpace(Point2D in) {
        return new Point2D.Double(in.getX() + WIDTH / 2, in.getY() + HEIGHT / 2);
    }

    public RobotCanvas(RobotContext ctx) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point mousePos = e.getPoint();
                Point2D mousePos2D = new Point2D.Double(mousePos.x, mousePos.y);
                if (mousePos.distanceSq(toScreenSpace(robotPos)) > ROBOT_DIAMETER * ROBOT_DIAMETER
                        / 3.8) { // don't question the numbers; they are what they are
                    // Arc
                    arcStart = arcEnd = mousePos2D;
                    repaint();
                } else {
                    lineEnd = mousePos2D;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (arcStart != null) {
                    // Arc
                    arcExtent = Math.abs(arcExtent);
                    String pollsText = (String) JOptionPane.showInputDialog(RobotCanvas.this,
                            "How many polls should be taken?", "Arc scan",
                            JOptionPane.QUESTION_MESSAGE, null, null,
                            String.valueOf((int) arcExtent));
                    if (pollsText == null)
                        return; // Cancel
                    ctx.turn((int) (-arcMidAngle - robotAngle));
                    if (pollsText.equals("ping")) {
                        ctx.tx("x p 90");
                    } else if (pollsText.equals("clear")) {
                        scanPoints.clear();
                        scanPointIsPing.clear();
                    } else {
                        int numPolls = Integer.parseInt(pollsText);
                        ctx.startScan(arcRadius, (int) (90 - arcExtent / 2),
                                (int) (90 + arcExtent / 2), numPolls);
                    }
                    arcStart = arcEnd = null;
                    repaint();
                } else {
                    ctx.turn((int) (moveAngle / Math.PI * 180 - robotAngle));
                    ctx.move((int) moveLen * 10);
                    lineEnd = null;
                    repaint();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (arcStart != null) {
                    Point mousePos = e.getPoint();
                    arcEnd = new Point2D.Double(mousePos.x, mousePos.y);
                    repaint();
                } else if (lineEnd != null) {
                    Point mousePos = e.getPoint();
                    lineEnd = new Point2D.Double(mousePos.x, mousePos.y);
                    repaint();
                } else
                    super.mouseMoved(e);
            }
        });
        ctx.addRobotMovementListener(new RobotMovementListener() {
            @Override
            public void robotStartedMovement() {
                robotPosMark = (Point2D) robotPos.clone();
                robotAngleMark = robotAngle;
            }

            @Override
            public void robotMoved(double dist) {
                dist /= 10;
                robotPos.setLocation(robotPosMark.getX() + dist * Math.cos(robotAngle * Math.PI / 180), robotPosMark.getY() + dist * Math.sin(robotAngle * Math.PI / 180));
                repaint();
            }

            @Override
            public void robotTurned(double angle) {
                robotAngle = robotAngleMark + angle;
                repaint();
            }
        });
        ctx.addRobotScanListener(new RobotScanListener() {
            @Override
            public void ir(double angle, double dist) {
                scanPointIsPing.add(false);
                scanPoints.add(new Point2D.Double(robotPos.getX() + dist * Math.sin((robotAngle + angle) * Math.PI / 180), robotPos.getY() + dist * -Math.cos((robotAngle + angle) * Math.PI / 180)));
                repaint();
            }

            @Override
            public void ping(double angle, double dist) {
                scanPointIsPing.add(true);
                scanPoints.add(new Point2D.Double(robotPos.getX() + dist * Math.sin((robotAngle + angle) * Math.PI / 180), robotPos.getY() + dist * -Math.cos((robotAngle + angle) * Math.PI / 180)));
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics _g) {
        var g = (Graphics2D) _g.create();
        g.setBackground(Color.DARK_GRAY);
        g.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw CyBot / Waff-L
        var robotPos = toScreenSpace(this.robotPos);
        g.translate(robotPos.getX(), robotPos.getY());
        g.rotate((robotAngle + 90) * Math.PI / 180);
        g.setColor(Color.GRAY);
        g.fill(new Ellipse2D.Double(-ROBOT_DIAMETER / 2, -ROBOT_DIAMETER / 2, ROBOT_DIAMETER,
                ROBOT_DIAMETER));
        g.setColor(Color.GREEN);
        g.fill(new Ellipse2D.Double(-ROBOT_INNER_DIAMETER / 2, -ROBOT_INNER_DIAMETER / 2,
                ROBOT_INNER_DIAMETER, ROBOT_INNER_DIAMETER));
        g.setColor(Color.RED);
        g.fill(new Rectangle2D.Double(-ROBOT_BASEBOARD_WIDTH / 2, 0, ROBOT_BASEBOARD_WIDTH,
                ROBOT_BASEBOARD_HEIGHT));
        g.dispose();

        g = (Graphics2D) _g.create();

        for (int i = 0; i < scanPoints.size(); i++) {
            g.setColor(scanPointIsPing.get(i) ? Color.GREEN : Color.RED);
            double size = scanPointIsPing.get(i) ? 10 : 2;
            Point2D point = toScreenSpace(scanPoints.get(i));
            g.fill(new Ellipse2D.Double(point.getX() - size / 2, point.getY() - size / 2, size, size));
        }

        if (arcStart != null) {
            double startAngle =
                    Math.atan2(robotPos.getY() - arcStart.getY(), arcStart.getX() - robotPos.getX())
                            / Math.PI * 180;
            double endAngle =
                    Math.atan2(robotPos.getY() - arcEnd.getY(), arcEnd.getX() - robotPos.getX())
                            / Math.PI * 180;
            arcExtent = endAngle - startAngle;
            arcMidAngle = (startAngle + endAngle) / 2;
            if (Math.abs(startAngle - endAngle) > 180) {
                arcMidAngle += 180;
            }

            if (arcExtent < -180) {
                arcExtent += 360;
            }
            if (arcExtent > 180) {
                arcExtent -= 360;
            }
            arcRadius = arcEnd.distance(robotPos);
            g.setColor(new Color(0, 255, 255, 128));
            Arc2D arc = new Arc2D.Double(robotPos.getX() - arcRadius, robotPos.getY() - arcRadius,
                    arcRadius * 2, arcRadius * 2, startAngle, arcExtent, Arc2D.PIE);
            g.fill(arc);
            g.setStroke(new BasicStroke(1.0f));
            g.setColor(new Color(0, 255, 255));
            g.draw(arc);
            g.draw(new Line2D.Double(robotPos.getX(), robotPos.getY(),
                    robotPos.getX() + arcRadius * Math.cos(arcMidAngle / 180 * Math.PI),
                    robotPos.getY() - arcRadius * Math.sin(arcMidAngle / 180 * Math.PI)));
        } else if (lineEnd != null) {
            moveAngle =
                    Math.atan2(lineEnd.getY() - robotPos.getY(), lineEnd.getX() - robotPos.getX());
            moveLen = Math.sqrt(Math.pow(robotPos.getY() - lineEnd.getY(), 2)
                    + Math.pow(lineEnd.getX() - robotPos.getX(), 2));
            double arrowSize = Math.min(moveLen, 20);
            Point2D arrowBase = new Point2D.Double(
                    robotPos.getX() + (moveLen - arrowSize) * Math.cos(moveAngle),
                    robotPos.getY() + (moveLen - arrowSize) * Math.sin(moveAngle));
            Point2D arrowLeft =
                    new Point2D.Double(arrowBase.getX() + arrowSize * Math.sin(moveAngle),
                            arrowBase.getY() - arrowSize * Math.cos(moveAngle));
            Point2D arrowRight =
                    new Point2D.Double(arrowBase.getX() - arrowSize * Math.sin(moveAngle),
                            arrowBase.getY() + arrowSize * Math.cos(moveAngle));
            g.setStroke(new BasicStroke(1.0f));
            g.setColor(new Color(0, 255, 255));
            g.draw(new Line2D.Double(robotPos, lineEnd));
            g.draw(new Line2D.Double(lineEnd, arrowLeft));
            g.draw(new Line2D.Double(lineEnd, arrowRight));
            g.draw(new Line2D.Double(arrowLeft, arrowRight));
        }
    }
}

