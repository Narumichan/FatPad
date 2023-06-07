package com.narumi.Tools;

import com.narumi.FatPad;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

    private Font defaultFont = new Font("Consoles", Font.BOLD, 14);
    private final JPanel updatePanel = new JPanel();
    private final JLabel updateLabel = new JLabel();
    private final JButton updateButton = new JButton();
    private AutoUpdater updater = new AutoUpdater();

    public InfoPanel() {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(300, 55));
        setMinimumSize(new Dimension(300, 55));
        setMaximumSize(new Dimension(300, 55));

        updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.X_AXIS));
        updatePanel.setPreferredSize(new Dimension(300, 55));
        updatePanel.setMinimumSize(new Dimension(300, 55));
        updatePanel.setMaximumSize(new Dimension(300, 55));

        updateLabel.setText(Utils.getVersionString(FatPad.VERSION));
        updateLabel.setPreferredSize(new Dimension(60, 55));
        updateLabel.setMinimumSize(new Dimension(60, 55));
        updateLabel.setMaximumSize(new Dimension(60, 55));

        updateButton.setFocusable(false);
        updateButton.setPreferredSize(new Dimension(150, 55));
        updateButton.setMinimumSize(new Dimension(150, 55));
        updateButton.setMaximumSize(new Dimension(150, 55));
        updateButton.addActionListener(e -> updater.openUpdatePage());


        if(updater.needToUpdate())
        {
            updateButton.setText("Get " + Utils.getVersionString(updater.getlatestVersion()));

            updatePanel.add(Box.createHorizontalGlue());
            updatePanel.add(updateLabel);
            updatePanel.add(updateButton);
        }
        else
        {
            updatePanel.add(Box.createHorizontalGlue());
            updatePanel.add(updateLabel);
        }

        add(Box.createHorizontalGlue());
        add(updatePanel);
    }

    public void changeFont(Font x) {
        updateButton.setFont(defaultFont);
        updateLabel.setFont(defaultFont);

    }

    public void changeTextColor(Color x) {
        updateButton.setForeground(x);
        updateLabel.setForeground(x);

    }

}
