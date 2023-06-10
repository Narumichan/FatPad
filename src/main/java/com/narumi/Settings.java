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

public class Settings extends JScrollPane implements Tab {
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
    public static final int NUMBEROFTHEMES = themes.length;
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
    private final JCheckBox textDefaultCheckBox = new JCheckBox("Match Theme");
    private final JCheckBox enableDragCheckBox = new JCheckBox();
    private final JSlider tabSizeSlider = new JSlider(20, 80, 40);
    private final JLabel tabSizeLabel = new JLabel("Tab Size: " + tabSizeSlider.getValue());
    private final JPanel tabSizePanel = new JPanel();
    private final JLabel textFontLabel = new JLabel();
    private final JLabel textColorLabel = new JLabel();
    private final JLabel enableDragLabel = new JLabel("Allow text dragging");
    private final JLabel themeLabel = new JLabel("Theme: ");
    private final JComboBox<String> themeComboBox = new JComboBox<>(themes);
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

    private Color newColor;

    private final FatPad owner;
    private static final String TITLE = "< Settings >";
    private boolean saved = true;

    public Settings(FatPad newOwner) {
        owner = newOwner;
        thiss = this;
        settingsPanel.setLayout(new GridLayout(9, 1, 5, 5));

        newFont = owner.getDefaultFont();
        newColor = textFontLabel.getForeground();

        textFontPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        textFontLabel.setText(owner.getDefaultFont().getFamily() + " " + owner.getDefaultFont().getSize() + "px");
        textFontLabel.setFont(settingsFont.deriveFont(22F));

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
        textColorLabel.setBackground(owner.isUsingDefaultTextColor() ? newColor : owner.getTextColor());

        textColorButton.setFont(settingsFont);
        textColorButton.setPreferredSize(new Dimension(200, 60));
        textColorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(thiss, "", new Color(255, 255, 255));
            if (selectedColor == null)
                return;
            textDefaultCheckBox.setSelected(false);
            newColor = selectedColor;
            textColorLabel.setBackground(newColor);
            thiss.setSaved(false);
        });

        textDefaultCheckBox.setSelected(owner.isUsingDefaultTextColor());
        textDefaultCheckBox.addItemListener(e -> {
            if (e.getStateChange() == 1) //Selected
            {
                newColor = textFontLabel.getForeground();
                textColorLabel.setBackground(newColor);
                owner.setUsingDefaultTextColor(true);
                thiss.setSaved(false);
            } else {
                owner.setUsingDefaultTextColor(false);
                thiss.setSaved(false);

            }
        });

        textColorPanel.add(textColorButton);
        textColorPanel.add(textColorLabel);
        textColorPanel.add(textDefaultCheckBox);


        themePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));


        themePanel.add(themeLabel);
        themePanel.add(themeComboBox);

        themeLabel.setFont(settingsFont);

        themeComboBox.setRenderer(renderer);
        themeComboBox.setFont(settingsFont);
        themeComboBox.setSelectedIndex(owner.getCurrentTheme() - 1);
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
        enableDragCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        enableDragCheckBox.setVerticalAlignment(SwingConstants.CENTER);

        enableDragPanel.add(enableDragCheckBox);
        enableDragPanel.add(enableDragLabel);

        tabSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        tabSizeLabel.setFont(settingsFont);
        tabSizeLabel.setPreferredSize(new Dimension(125, 50));
        tabSizeLabel.setVerticalAlignment(SwingConstants.CENTER);

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
    }

    public void applyOwnersSettings() {
        if (newFont != null) {
            owner.setDefaultFont(newFont);
            owner.changeFont(newFont);
        }
        if (newColor != null) {
            newColor = textDefaultCheckBox.isSelected() ? textFontLabel.getForeground() : textColorLabel.getBackground();
            owner.setTextColor(newColor);
            owner.changeTextColor(newColor);
        }
        if (themeComboBox.getSelectedIndex() != 0) {
            owner.getTheme(themeComboBox.getSelectedIndex() + 1);
        } else {
            owner.getRandomTheme();
        }

        owner.setUsingDefaultTextColor(textDefaultCheckBox.isSelected());

        thiss.setSaved(true);

    }

    private ArrayList<String> getLinesToSave() {
        if (newFont == null) {
            newFont = owner.getDefaultFont();
        }
        if (newColor == null) {
            newColor = owner.getTextColor();
        }
        if (newTheme == 0) {
            newTheme = owner.getCurrentTheme();
        }

        owner.setUsingDefaultTextColor(textDefaultCheckBox.isSelected());

        System.out.println("Saving settings");

        int[] selectedColorRGBArray = owner.getInfoPanel().getSelectedColors();

        ArrayList<String> linesToSave = new ArrayList<>();
        linesToSave.add("Font=" + newFont.getFamily());
        linesToSave.add("FontStyle=" + newFont.getStyle());
        linesToSave.add("FontSize=" + newFont.getSize());
        linesToSave.add("Color=" + newColor.getRGB());
        linesToSave.add("Theme=" + newTheme);
        linesToSave.add("UseDefaultTextColor=" + (textDefaultCheckBox.isSelected() ? "1" : "0"));
        linesToSave.add("SelectedColors=" +
                selectedColorRGBArray[0] + "=" +
                selectedColorRGBArray[1] + "=" +
                selectedColorRGBArray[2] + "=" +
                selectedColorRGBArray[3]);

        return linesToSave;
    }

    public void resetSettings() {
        newFont = null;
        newTheme = owner.getCurrentTheme();
        textDefaultCheckBox.setSelected(owner.isUsingDefaultTextColor());
        textFontLabel.setText(owner.getDefaultFont().getFontName() + " " + owner.getDefaultFont().getSize() + "px");
        newColor = textDefaultCheckBox.isSelected() ? textFontLabel.getForeground() : owner.getTextColor();
        textColorLabel.setBackground(newColor);
        tabSizeLabel.setText("Tab Size: " + tabSizeSlider.getValue());
        themeComboBox.setSelectedIndex(owner.getCurrentTheme() - 1);
    }

    public String getTitle() {
        return TITLE;
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
            }
            break;
            case JOptionPane.NO_OPTION: {
                resetSettings();
                removeTab();
            }
            break;
            default: {
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("./config/config.cfg"))) {
            Files.createDirectories(Paths.get("./config/"));
            ArrayList<String> linesToSave = getLinesToSave();
            for (String line : linesToSave) {
                bw.write(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public Color getDefaultTextColor() {
        return textFontLabel.getForeground();
    }
}
