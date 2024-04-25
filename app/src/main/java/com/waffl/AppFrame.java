package com.waffl;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class AppFrame extends JFrame {
    private RobotContext robotContext;
    private RobotCanvas robotCanvas;
    private TerminalView terminalView;

    public AppFrame(RobotContext ctx) {
        super("CPRE 288: Waff-L");
        robotContext = ctx;

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        robotCanvas = new RobotCanvas(robotContext);
        add(new JScrollPane(robotCanvas));

        terminalView = new TerminalView(robotContext);
        add(terminalView);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
