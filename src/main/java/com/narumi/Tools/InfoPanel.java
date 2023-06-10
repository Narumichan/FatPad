package com.narumi.Tools;

import com.narumi.FatPad;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InfoPanel extends JPanel {

    private final FatPad owner;
    private Font defaultFont = new Font("Consoles", Font.BOLD, 14);
    private final JPanel colorsPanel = new JPanel();
    private final JLabel colorLabel1 = new JLabel();
    private final JLabel colorLabel2 = new JLabel();
    private final JLabel colorLabel3 = new JLabel();
    private final JLabel colorLabel4 = new JLabel();

    private final JPanel updatePanel = new JPanel();
    public final JLabel updateLabel = new JLabel();
    private final JButton updateButton = new JButton();
    private AutoUpdater updater = new AutoUpdater();

    public InfoPanel(FatPad owner) {
        this.owner = owner;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(300, 55));
        setMinimumSize(new Dimension(300, 55));
        setMaximumSize(new Dimension(300, 55));

        setupColorsPanel();
        setupUpdatePanel();

        add(colorsPanel);
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

        resetColorLabels();

    }

    private void setupUpdatePanel() {
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


        if (updater.needToUpdate()) {
            updateButton.setText("Get " + Utils.getVersionString(updater.getlatestVersion()));

            updatePanel.add(Box.createHorizontalGlue());
            updatePanel.add(updateLabel);
            updatePanel.add(updateButton);
        } else {
            updatePanel.add(Box.createHorizontalGlue());
            updatePanel.add(updateLabel);
        }

    }

    private void setupColorsPanel() {

        colorsPanel.setLayout(new BoxLayout(colorsPanel, BoxLayout.X_AXIS));
        colorsPanel.setPreferredSize(new Dimension(300, 55));
        colorsPanel.setMinimumSize(new Dimension(300, 55));
        colorsPanel.setMaximumSize(new Dimension(300, 55));

        setupPrimaryColorLabel(colorLabel1);
        setupSecondaryColorLabel(colorLabel2, 1);
        setupSecondaryColorLabel(colorLabel3, 2);
        setupSecondaryColorLabel(colorLabel4, 3);

        colorsPanel.add(Box.createHorizontalStrut(5));
        colorsPanel.add(colorLabel1);
        colorsPanel.add(Box.createHorizontalStrut(5));
        colorsPanel.add(colorLabel2);
        colorsPanel.add(Box.createHorizontalStrut(5));
        colorsPanel.add(colorLabel3);
        colorsPanel.add(Box.createHorizontalStrut(5));
        colorsPanel.add(colorLabel4);

    }

    private void setupSecondaryColorLabel(JLabel label, int labelIndex) {
        setupColorLabel(label);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Color selectedColor = JColorChooser.showDialog(owner, "", new Color(255, 255, 255));
                    if (selectedColor == null) return;

                    label.setBackground(selectedColor);
                    owner.getSelectedColors()[labelIndex] = selectedColor;
                    colorLabelMouseReleased(label);
                } else if (e.getButton() == MouseEvent.BUTTON1) colorLabelMouseReleased(label);

            }
        });

    }

    private void colorSelectedText(JTextPane textPane, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        Style newStyle = textPane.addStyle("ColoredText", null);
        StyleConstants.setForeground(newStyle, color);

        try {
            int startIndex = textPane.getSelectionStart();
            int endIndex = textPane.getSelectionEnd();

            doc.setCharacterAttributes(startIndex, endIndex - startIndex, newStyle, true);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setupPrimaryColorLabel(JLabel label) {
        setupColorLabel(label);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    colorLabelMouseReleased(label);

            }
        });
        label.setForeground(owner.getTextColor());

    }

    private void colorLabelMouseReleased(JLabel label) {
        if (label.getBackground() == null)
            return;

        Component selectedComponent = owner.tabbedPane.getSelectedComponent();

        if (!(selectedComponent instanceof TextPaneTab))
            return;
        String selectedText = ((TextPaneTab) selectedComponent).getTextPane().getSelectedText();
        if (selectedText == null)
            return;

        ((TextPaneTab) selectedComponent).setUsingColors(true);
        colorSelectedText(((TextPaneTab) selectedComponent).getTextPane(), label.getBackground());
    }

    private void setupColorLabel(JLabel label) {
        Border colorLabelBorder = BorderFactory.createLineBorder(owner.getTextColor().darker(), 2);
        Dimension colorLabelDimension = new Dimension(50, 50);

        label.setFocusable(false);
        label.setBorder(colorLabelBorder);
        label.setOpaque(true);
        label.setPreferredSize(colorLabelDimension);
        label.setMinimumSize(colorLabelDimension);
        label.setMaximumSize(colorLabelDimension);
    }

    public void resetColorLabels() {
        colorLabel1.setBackground(owner.getTextColor());
        colorLabel2.setBackground(owner.getSelectedColors()[1]);
        colorLabel3.setBackground(owner.getSelectedColors()[2]);
        colorLabel4.setBackground(owner.getSelectedColors()[3]);
    }

    public int[] getSelectedColors() {
        return new int[]{
                colorLabel1.getBackground().getRGB(),
                colorLabel2.getBackground().getRGB(),
                colorLabel3.getBackground().getRGB(),
                colorLabel4.getBackground().getRGB()
        };
    }
}
