package com.narumi.Tools;

import com.narumi.FatPad;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {
    private FatPad owner;

    private Font defaultFont = new Font("Consoles", Font.BOLD, 14);

    public InfoPanel(FatPad newOwner) {
        owner = newOwner;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(300, 55));
        setMinimumSize(new Dimension(300, 55));
        setMaximumSize(new Dimension(300, 55));

        add(Box.createHorizontalGlue());
    }

    public void changeFont(Font x) {

    }

    public void changeTextColor(Color x) {

    }

}
