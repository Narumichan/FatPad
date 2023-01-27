package com.narumi;

import com.narumi.Tools.ComboboxToolTipRenderer;
import org.drjekyll.fontchooser.FontDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class Settings extends JPanel
{
    public static Dimension dimension = new Dimension(440,640);
    public JDialog parent = null;

    private JButton applyButton = new JButton("Apply");
    private JButton cancelButton = new JButton("Cancel");
    private JButton fontButton = new JButton("Change Font");
    private JButton textColorButton = new JButton("Change Text Color");
    private JButton textDefaultColorButton = new JButton("Default");
    private JCheckBox enableDragCheckBox = new JCheckBox();
    private JSlider tabSizeSlider = new JSlider(20,80,40);
    private JLabel tabSizeLabel = new JLabel("Tab Size: " + tabSizeSlider.getValue());
    private JPanel tabSizePanel = new JPanel();
    private JLabel textFontLabel = new JLabel();
    private JLabel textColorLabel = new JLabel();
    private JLabel enableDragLabel = new JLabel("Allow text dragging");
    private JLabel themeLabel = new JLabel("Theme: ");
    private static String[] themes = {
            "FlatLaf Light",
            "Solarized Light",
            "Github",
            "FlatLaf Dark",
            "Arc Dark",
            "Carbon",
            "Moonlight",
            "Material Oceanic",
            "Atom One Dark",
            "Solarized Dark",
            "Gradianto Dark Fuchsia"
    };
    private static String[] themeTooltips = {
            "https://github.com/JFormDesigner/FlatLaf"
            ,"https://github.com/4lex4/intellij-platform-solarized"
            ,"https://github.com/mallowigi/material-theme-ui-lite"
            ,"https://github.com/JFormDesigner/FlatLaf"
            ,"https://gitlab.com/zlamalp/arc-theme-idea"
            ,"https://github.com/luisfer0793/theme-carbon"
            ,"https://github.com/mallowigi/material-theme-ui-lite"
            ,"https://github.com/mallowigi/material-theme-ui-lite"
            ,"https://github.com/mallowigi/material-theme-ui-lite"
            ,"https://github.com/mallowigi/material-theme-ui-lite"
            ,"https://github.com/thvardhan/Gradianto"
    };
    private JComboBox themeComboBox = new JComboBox(themes);
    public static int numberOfThemes = themes.length;

    private ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
    private JPanel buttonsPanel = new JPanel();
    private JPanel enableDragPanel = new JPanel();
    private Font settingsFont = new Font("Consoles", Font.PLAIN, 18);
    private JPanel themePanel = new JPanel();
    private JPanel textColorPanel = new JPanel();
    private JPanel innerButtonsPanel = new JPanel();
    private JPanel textFontPanel = new JPanel();

    private int newTheme = 0;

    private Font newFont = null;

    private Color newColor = null;
    private Settings thiss;

    private FatPad owner = null;

    public Settings(FatPad newOwner)
    {
        owner = newOwner;
        thiss = this;
        setLayout(new GridLayout(9,1,5,5));

        //textFontPanel.setBackground(Color.red);
        //buttonsPanel.setBackground(Color.green);
        //innerButtonsPanel.setBackground(Color.pink);

        newFont = owner.defaultFont;

        textFontPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        textFontLabel.setText(owner.defaultFont.getFamily() + " " + owner.defaultFont.getSize() + "px");
        textFontLabel.setFont(settingsFont.deriveFont(22F));

        fontButton.setFocusable(false);
        fontButton.setFont(settingsFont);
        fontButton.setPreferredSize(new Dimension(150,60));
        fontButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FontDialog fontDialog = new FontDialog((JFrame) null, "", true);
                fontDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                fontDialog.setLocationRelativeTo(null);
                fontDialog.setVisible(true);
                if (!fontDialog.isCancelSelected()) {
                    textFontLabel.setText(fontDialog.getSelectedFont().getFamily() + " " + fontDialog.getSelectedFont().getSize() + "px");
                    newFont = fontDialog.getSelectedFont();
                }
            }
        });
        textFontPanel.add(fontButton);
        textFontPanel.add(textFontLabel);

        textColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        textColorLabel.setText("");
        textColorLabel.setPreferredSize(new Dimension(50,50));
        textColorLabel.setOpaque(true);
        textColorLabel.setBorder(new LineBorder(Color.black,1,false));
        textColorLabel.setBackground(owner.textColor);

        textColorButton.setFocusable(false);
        textColorButton.setFont(settingsFont);
        textColorButton.setPreferredSize(new Dimension(200,60));
        textColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color selectedColor = JColorChooser.showDialog(thiss, "",new Color(255,255,255));
                if(selectedColor == null)
                    return;

                newColor = selectedColor;
                textColorLabel.setBackground(newColor);
            }
        });

        textDefaultColorButton.setFocusable(false);
        textDefaultColorButton.setFont(settingsFont.deriveFont(12F));
        textDefaultColorButton.setPreferredSize(new Dimension(80,40));
        textDefaultColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                newColor = new Color(255,255,255);
                textColorLabel.setBackground(newColor);
            }
        });

        textColorPanel.add(textColorButton);
        textColorPanel.add(textColorLabel);
        textColorPanel.add(textDefaultColorButton);

        themePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));


        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);

        themeLabel.setFont(settingsFont);

        themeComboBox.setRenderer(renderer);
        themeComboBox.setFont(settingsFont);
        renderer.setTooltips(Arrays.asList(themeTooltips));
        themeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newTheme = themeComboBox.getSelectedIndex() + 1;
            }
        });

        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);

        enableDragPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        enableDragLabel.setFont(settingsFont);

        enableDragCheckBox.setPreferredSize(new Dimension(30,30));
        enableDragCheckBox.setSelected(owner.dragEnabled);
        enableDragCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        enableDragCheckBox.setVerticalAlignment(JCheckBox.CENTER);

        enableDragPanel.add(enableDragCheckBox);
        enableDragPanel.add(enableDragLabel);

        tabSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5,0));

        tabSizeLabel.setFont(settingsFont);
        tabSizeLabel.setPreferredSize(new Dimension(125,50));
        tabSizeLabel.setVerticalAlignment(JLabel.CENTER);

        tabSizeSlider.setPreferredSize(new Dimension(250,50));
        tabSizeSlider.setMajorTickSpacing(20);
        tabSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tabSizeLabel.setText("Tab Size: " + tabSizeSlider.getValue());
            }
        });

        tabSizePanel.add(tabSizeLabel);
        tabSizePanel.add(tabSizeSlider);





        applyButton.setPreferredSize(new Dimension(150,60));
        applyButton.setFocusable(false);
        applyButton.setFont(settingsFont);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                applyOwnersSettings();

                parent.setVisible(false);
            }
        });

        cancelButton.setPreferredSize(new Dimension(150,60));
        cancelButton.setFocusable(false);
        cancelButton.setFont(settingsFont);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFont = null;
                newTheme = 0;
                textFontLabel.setText(owner.defaultFont.getFontName() + " " + owner.defaultFont.getSize() + "px");
                newColor = new Color(255,255,255);
                textColorLabel.setBackground(newColor);
                tabSizeSlider.setValue(owner.tabSize);
                tabSizeLabel.setText("Tab Size: " + tabSizeSlider.getValue());
                parent.setVisible(false);
            }
        });

        innerButtonsPanel.setLayout(new BorderLayout(5,5));
        innerButtonsPanel.setMaximumSize(new Dimension(305,44));
        innerButtonsPanel.setPreferredSize(new Dimension(305,44));
        innerButtonsPanel.setMinimumSize(new Dimension(305,44));
        innerButtonsPanel.add(applyButton, BorderLayout.WEST);
        innerButtonsPanel.add(cancelButton, BorderLayout.EAST);

        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(innerButtonsPanel);
        buttonsPanel.add(Box.createHorizontalGlue());

        add(textFontPanel);
        add(textColorPanel);
        add(themePanel);
        add(enableDragPanel);
        add(tabSizePanel);
        add(new JLabel(""));
        add(new JLabel(""));
        add(new JLabel(""));
        add(buttonsPanel);

        setVisible(true);
    }

    private void applyOwnersSettings() {
        if (newFont != null) {
            owner.defaultFont = newFont;
            owner.changeFont(newFont);
        }
        if (newColor != null) {
            owner.textColor = newColor;
            owner.changeTextColor(newColor);
        }
        if (newTheme != 0)
        {
            owner.getTheme(newTheme);
        }
        owner.dragEnabled = enableDragCheckBox.isSelected();
        owner.setDrag(owner.dragEnabled); // wtf again
        owner.setTabSize(tabSizeSlider.getValue());
    }

    public void saveOwnersSettings()
    {
        if(newFont == null)
        {
            newFont = owner.defaultFont;
        }
        if(newColor == null)
        {
            newColor = owner.textColor;
        }
        if(newTheme == 0)
        {
            newTheme = owner.currentTheme;
        }
        System.out.println("saving");

        ArrayList<String> linesToSave= new ArrayList<String>();
        linesToSave.add("Font=" + newFont.getFamily());
        linesToSave.add("FontStyle=" + newFont.getStyle());
        linesToSave.add("FontSize=" + newFont.getSize());
        linesToSave.add("Color=" + newColor.getRGB());
        linesToSave.add("Theme=" + newTheme);
        linesToSave.add("DragEnabled=" + owner.dragEnabled);
        owner.saveSettings(linesToSave);
    }

    public void resetThemeList()
    {
        themeComboBox.setSelectedIndex(owner.currentTheme-1);
    }
}
