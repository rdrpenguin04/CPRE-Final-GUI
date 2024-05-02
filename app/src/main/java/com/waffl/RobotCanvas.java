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
    private Point2D arcStart = null;
    private Point2D arcEnd = null;
    private double arcExtent;

    private static Point2D toScreenSpace(Point2D in) {
        return new Point2D.Double(in.getX() + WIDTH / 2, in.getY() + HEIGHT / 2);
    }

    public RobotCanvas(RobotContext ctx) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point mousePos = e.getPoint();
                if (mousePos.distanceSq(robotPos) > ROBOT_DIAMETER * ROBOT_DIAMETER / 4) {
                    // Arc
                    arcStart = arcEnd = new Point2D.Double(mousePos.x, mousePos.y);
                    repaint();
                } else {
                    // Move
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (arcStart != null) {
                    // Arc
                    arcExtent = Math.abs(arcExtent);
                    int numPolls = Integer.parseInt((String) JOptionPane.showInputDialog(RobotCanvas.this,
                            "How many polls should be taken?", "Arc scan",
                            JOptionPane.QUESTION_MESSAGE, null, null,
                            String.valueOf((int) arcExtent)));
                    arcStart = arcEnd = null;
                    repaint();
                } else {
                    // TODO
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
                } else
                    super.mouseMoved(e);
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
        // TODO: draw other UI elements

        if (arcStart != null) {
            double startAngle = Math.atan2(robotPos.getY() - arcStart.getY(), arcStart.getX() - robotPos.getX())
                    / Math.PI * 180;
            double endAngle = Math.atan2(robotPos.getY() - arcEnd.getY(), arcEnd.getX() - robotPos.getX())
                    / Math.PI * 180;
            arcExtent = endAngle - startAngle;
            double midAngle = (startAngle + endAngle) / 2;
            if (Math.abs(startAngle - endAngle) > 180) {
                midAngle += 180;
            }

            if (arcExtent < -180) {
                arcExtent += 360;
            }
            if (arcExtent > 180) {
                arcExtent -= 360;
            }
            double radius = arcEnd.distance(robotPos);
            g.setColor(new Color(0, 255, 255, 128));
            Arc2D arc = new Arc2D.Double(robotPos.getX() - radius, robotPos.getY() - radius,
                    radius * 2, radius * 2, startAngle, arcExtent, Arc2D.PIE);
            g.fill(arc);
            g.setStroke(new BasicStroke(1.0f));
            g.setColor(new Color(0, 255, 255));
            g.draw(arc);
            g.draw(new Line2D.Double(robotPos.getX(), robotPos.getY(),
                    robotPos.getX() + radius * Math.cos(midAngle / 180 * Math.PI),
                    robotPos.getY() - radius * Math.sin(midAngle / 180 * Math.PI)));
        }
    }
}
