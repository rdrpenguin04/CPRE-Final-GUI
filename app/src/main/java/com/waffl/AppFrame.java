package com.waffl;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class AppFrame extends JFrame {
    private RobotCanvas robotCanvas;
    private TerminalView terminalView;

    public AppFrame() {
        super("CPRE 288: Waff-L");

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        robotCanvas = new RobotCanvas();
        add(new JScrollPane(robotCanvas));

        terminalView = new TerminalView();
        add(terminalView);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
