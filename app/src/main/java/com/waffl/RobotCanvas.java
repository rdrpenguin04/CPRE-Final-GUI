package com.waffl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

public class RobotCanvas extends JPanel {
    private Point2D robotPos = new Point2D.Double(0.0, 0.0);
    private static final double ROBOT_DIAMETER = 34;
    private static final double ROBOT_INNER_DIAMETER = 30;
    private static final double ROBOT_BASEBOARD_WIDTH = 20;
    private static final double ROBOT_BASEBOARD_HEIGHT = 10;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1920;

    private static Point2D toScreenSpace(Point2D in) {
        return new Point2D.Double(in.getX() + WIDTH / 2, in.getY() + HEIGHT / 2);
    }

    public RobotCanvas() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
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
        g.fill(new Rectangle2D.Double(-ROBOT_BASEBOARD_WIDTH / 2, 0, ROBOT_BASEBOARD_WIDTH, ROBOT_BASEBOARD_HEIGHT));
        g.dispose();

        // g = (Graphics2D) _g.create();
        // TODO: draw other UI elements
    }
}
