package com.narumi;

import com.narumi.Tools.ComboboxToolTipRenderer;
import org.drjekyll.fontchooser.FontDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Settings extends JScrollPane implements Tab{
    private static final String[] themes = {
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
    public static int numberOfThemes = themes.length;
    private static final String[] themeTooltips = {
            "https://github.com/JFormDesigner/FlatLaf"
            , "https://github.com/4lex4/intellij-platform-solarized"
            , "https://github.com/mallowigi/material-theme-ui-lite"
            , "https://github.com/JFormDesigner/FlatLaf"
            , "https://gitlab.com/zlamalp/arc-theme-idea"
            , "https://github.com/luisfer0793/theme-carbon"
            , "https://github.com/mallowigi/material-theme-ui-lite"
            , "https://github.com/mallowigi/material-theme-ui-lite"
            , "https://github.com/mallowigi/material-theme-ui-lite"
            , "https://github.com/mallowigi/material-theme-ui-lite"
            , "https://github.com/thvardhan/Gradianto"
    };
    private final Settings thiss;
    private final JPanel settingsPanel = new JPanel();
    private final JButton applyButton = new JButton("Apply");
    private final JButton fontButton = new JButton("Change Font");
    private final JButton textColorButton = new JButton("Change Text Color");
    private final JButton textDefaultColorButton = new JButton("Default");
    private final JCheckBox enableDragCheckBox = new JCheckBox();
    private final JSlider tabSizeSlider = new JSlider(20, 80, 40);
    private final JLabel tabSizeLabel = new JLabel("Tab Size: " + tabSizeSlider.getValue());
    private final JPanel tabSizePanel = new JPanel();
    private final JLabel textFontLabel = new JLabel();
    private final JLabel textColorLabel = new JLabel();
    private final JLabel enableDragLabel = new JLabel("Allow text dragging");
    private final JLabel themeLabel = new JLabel("Theme: ");
    private final JComboBox themeComboBox = new JComboBox(themes);
    private final ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel enableDragPanel = new JPanel();
    private final Font settingsFont = new Font("Consoles", Font.PLAIN, 18);
    private final JPanel themePanel = new JPanel();
    private final JPanel textColorPanel = new JPanel();
    private final JPanel innerButtonsPanel = new JPanel();
    private final JPanel textFontPanel = new JPanel();

    private int newTheme = 0;

    private Font newFont;

    private Color newColor = null;

    private final FatPad owner;
    private final JTabbedPane tabbedPaneOwner;
    private final String title = "< Settings >";
    private boolean saved = true;

    public Settings(FatPad newOwner, JTabbedPane newTabbedPaneOwner) {
        owner = newOwner;
        tabbedPaneOwner = newTabbedPaneOwner;
        thiss = this;
        settingsPanel.setLayout(new GridLayout(9, 1, 5, 5));

        newFont = owner.defaultFont;

        textFontPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        textFontLabel.setText(owner.defaultFont.getFamily() + " " + owner.defaultFont.getSize() + "px");
        textFontLabel.setFont(settingsFont.deriveFont(22F));

        fontButton.setFocusable(false);
        fontButton.setFont(settingsFont);
        fontButton.setPreferredSize(new Dimension(150, 60));
        fontButton.addActionListener(e -> {
            FontDialog fontDialog = new FontDialog((JFrame) null, "", true);
            fontDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            fontDialog.setLocationRelativeTo(null);
            fontDialog.setVisible(true);
            if (!fontDialog.isCancelSelected()) {
                textFontLabel.setText(fontDialog.getSelectedFont().getFamily() + " " + fontDialog.getSelectedFont().getSize() + "px");
                newFont = fontDialog.getSelectedFont();
            }
            thiss.setSaved(false);
        });
        textFontPanel.add(fontButton);
        textFontPanel.add(textFontLabel);

        textColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        textColorLabel.setText("");
        textColorLabel.setPreferredSize(new Dimension(50, 50));
        textColorLabel.setOpaque(true);
        textColorLabel.setBorder(new LineBorder(Color.black, 1, false));
        textColorLabel.setBackground(owner.textColor);

        textColorButton.setFocusable(false);
        textColorButton.setFont(settingsFont);
        textColorButton.setPreferredSize(new Dimension(200, 60));
        textColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(thiss, "", new Color(255, 255, 255));
            if (selectedColor == null)
                return;

            newColor = selectedColor;
            textColorLabel.setBackground(newColor);
            thiss.setSaved(false);
        });

        textDefaultColorButton.setFocusable(false);
        textDefaultColorButton.setFont(settingsFont.deriveFont(12F));
        textDefaultColorButton.setPreferredSize(new Dimension(80, 40));
        textDefaultColorButton.addActionListener(e -> {

            newColor = new Color(255, 255, 255);
            textColorLabel.setBackground(newColor);
            thiss.setSaved(false);
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
        themeComboBox.setSelectedIndex(owner.currentTheme - 1);
        renderer.setTooltips(Arrays.asList(themeTooltips));
        themeComboBox.addActionListener(e -> {
            newTheme = themeComboBox.getSelectedIndex() + 1;
            thiss.setSaved(false);
        });

        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);

        enableDragPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        enableDragLabel.setFont(settingsFont);

        enableDragCheckBox.setPreferredSize(new Dimension(30, 30));
        enableDragCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        enableDragCheckBox.setVerticalAlignment(JCheckBox.CENTER);

        enableDragPanel.add(enableDragCheckBox);
        enableDragPanel.add(enableDragLabel);

        tabSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        tabSizeLabel.setFont(settingsFont);
        tabSizeLabel.setPreferredSize(new Dimension(125, 50));
        tabSizeLabel.setVerticalAlignment(JLabel.CENTER);

        tabSizeSlider.setPreferredSize(new Dimension(250, 50));
        tabSizeSlider.setMajorTickSpacing(20);
        tabSizeSlider.addChangeListener(e -> {
            tabSizeLabel.setText("Tab Size: " + tabSizeSlider.getValue());
            thiss.setSaved(false);
        });

        tabSizePanel.add(tabSizeLabel);
        tabSizePanel.add(tabSizeSlider);


        applyButton.setPreferredSize(new Dimension(150, 60));
        applyButton.setFocusable(false);
        applyButton.setFont(settingsFont);
        applyButton.addActionListener(e -> {

            applyOwnersSettings();
            saveFile();
            closeTab();
        });

        innerButtonsPanel.setLayout(new BorderLayout(5, 5));
        innerButtonsPanel.setMaximumSize(new Dimension(305, 44));
        innerButtonsPanel.setPreferredSize(new Dimension(305, 44));
        innerButtonsPanel.setMinimumSize(new Dimension(305, 44));
        innerButtonsPanel.add(applyButton, BorderLayout.WEST);
        //innerButtonsPanel.add(cancelButton, BorderLayout.EAST);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "Create a New Tab");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "Close the current tab");
        getActionMap().put("Create a New Tab", owner.createNewTab);
        getActionMap().put("Close the current tab", owner.closeCurrentTab);

        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(innerButtonsPanel);
        buttonsPanel.add(Box.createHorizontalGlue());

        settingsPanel.add(textFontPanel);
        settingsPanel.add(textColorPanel);
        settingsPanel.add(themePanel);
        settingsPanel.add(new JLabel(""));
        settingsPanel.add(new JLabel(""));
        settingsPanel.add(new JLabel(""));
        settingsPanel.add(buttonsPanel);

        getViewport().setView(settingsPanel);

        //setVisible(true);
    }

    public void applyOwnersSettings() {
        if (newFont != null) {
            owner.defaultFont = newFont;
            owner.changeFont(newFont);
        }
        if (newColor != null) {
            owner.textColor = newColor;
            owner.changeTextColor(newColor);
        }
        if (themeComboBox.getSelectedIndex() != 0) {
            owner.getTheme(themeComboBox.getSelectedIndex() + 1);
        } else {
            owner.getRandomTheme();
        }

        thiss.setSaved(true);

    }

    private ArrayList<String> getLinesToSave() {
        if (newFont == null) {
            newFont = owner.defaultFont;
        }
        if (newColor == null) {
            newColor = owner.textColor;
        }
        if (newTheme == 0) {
            newTheme = owner.currentTheme;
        }
        System.out.println("saving");

        ArrayList<String> linesToSave = new ArrayList<>();
        linesToSave.add("Font=" + newFont.getFamily());
        linesToSave.add("FontStyle=" + newFont.getStyle());
        linesToSave.add("FontSize=" + newFont.getSize());
        linesToSave.add("Color=" + newColor.getRGB());
        linesToSave.add("Theme=" + newTheme);
        return linesToSave;
    }

    public void resetSettings() {
        newFont = null;
        newTheme = 1;
        textFontLabel.setText(owner.defaultFont.getFontName() + " " + owner.defaultFont.getSize() + "px");
        newColor = new Color(255, 255, 255);
        textColorLabel.setBackground(newColor);
        tabSizeLabel.setText("Tab Size: " + tabSizeSlider.getValue());
        themeComboBox.setSelectedIndex(owner.currentTheme - 1);
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    @Override
    public boolean isSaved() {
        return saved;
    }

    @Override
    public int closeTab() {
        if (isSaved()) {
            removeTab();
            return 0;
        }

        int response = continueWithoutSavingDialog("the settings");

        switch (response) {
            case JOptionPane.YES_OPTION: {
                applyOwnersSettings();
                saveFile();
                //removeTab();
            }
            break;
            case JOptionPane.NO_OPTION: {
                resetSettings();
                removeTab();
            }
            break;
            case JOptionPane.CANCEL_OPTION: {
                return -1;
            }
        }

        return 0;
    }

    @Override
    public void removeTab() {
        owner.tabbedPane.remove(this);
        if (owner.tabbedPane.getComponents().length == 0) owner.closeWindow();
    }

    @Override
    public void saveFile() {
        try {
            Files.createDirectories(Paths.get("./config/"));
            ArrayList<String> linesToSave = getLinesToSave();
            BufferedWriter bw = new BufferedWriter(new FileWriter("./config/config.cfg"));
            for (String line : linesToSave) {
                bw.write(line + "\n");
            }
            bw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void saveFileAs() {
        saveFile();
    }

    @Override
    public int continueWithoutSavingDialog(String fileName) {
        Object[] options = {"Save", "Don't Save", "Cancel"};
        return JOptionPane.showOptionDialog
                (
                        this,
                        "Close " + fileName + " without saving?",
                        "",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        null
                );

    }
}
