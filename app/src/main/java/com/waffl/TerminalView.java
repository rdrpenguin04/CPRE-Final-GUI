package com.waffl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TerminalView extends JPanel {
    private JTextArea rx;
    private JTextField tx;

    public TerminalView(RobotContext robotContext) {
        setLayout(new BorderLayout());

        rx = new JTextArea();
        tx = new JTextField();

        rx.setEditable(false);
        rx.setBackground(Color.BLACK);
        rx.setForeground(Color.WHITE);

        tx.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("TX: " + tx.getText());
                tx.setText("");
            }
        });

        add(new JScrollPane(rx), BorderLayout.CENTER);
        add(tx, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(640, 360));
    }
}
