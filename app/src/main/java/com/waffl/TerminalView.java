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
import javax.swing.text.DefaultCaret;

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
                robotContext.tx(tx.getText());
                tx.setText("");
            }
        });

        robotContext.addLineListener(new LineListener() {
            @Override
            public void lineReceived(String line) {
                System.out.print("RX: " + line);
                rx.setText(rx.getText() + line);
            }
        });

        add(new JScrollPane(rx), BorderLayout.CENTER);
        add(tx, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(640, 360));

        DefaultCaret caret = (DefaultCaret) rx.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }
}
