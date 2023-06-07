package com.narumi;

/*
   font size slider

   tab lines

   line count

   row + char position

   word count
   char count

   better settings menu

   ctrl c + ctrl v

   background image
   opacity slider in the info panel

   better config files (save VERSION, append new data) (json?)

   error reading file

   better directory chooser (github)


 */

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme;
import com.narumi.Tools.InfoPanel;
import com.narumi.Tools.TextPaneTab;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;


public class FatPad extends JFrame {

    public static final int VERSION = 210;
    public final JTabbedPane tabbedPane = new JTabbedPane();
    private int currentTheme;
    private Color textColor = new Color(240, 240, 240);
    private boolean isUsingDefaultTextColor = true;
    private int fontSize = 15;
    private Font defaultFont = new Font("Consoles", Font.PLAIN, fontSize);
    public final Action closeCurrentTab = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            ((Tab) tabbedPane.getSelectedComponent()).closeTab();
        }
    };
    public final Action createNewTab = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            addNewTab();
        }
    };
    private Settings settingsPanel; //initialize after theme changes
    private final Random random = new Random();

    private InfoPanel infoPanel;

    public FatPad() {
        init();
    }

    public FatPad(String[] paths) {
        init();

        for (String i : paths) {
            openFile(new File(i));
        }

    }

    public static void main(String[] args) {
        if (args.length > 0) {
            EventQueue.invokeLater(() -> new FatPad(args));
        } else
            EventQueue.invokeLater(FatPad::new);

    }

    public void init() {
        setTitle("FatPad");
        setIconImage(new ImageIcon("./config/fatpad.png").getImage());
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, false);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_TITLE, false);

        setSize(1250, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(400, 300));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                settingsPanel.saveFile();
                closeWindow();
            }
        });

        setupTabbedPane();
        setJMenuBar(new Menu(this));


        settingsPanel = new Settings(this);
        infoPanel = new InfoPanel();

        add(tabbedPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);


        loadSettings();
        addNewTab();

        changeFont(getDefaultFont());


        setVisible(true);
    }

    private void setupTabbedPane() {
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex == -1) {
                        addNewTab();
                    } else {
                        closeTab(tabIndex);
                    }


                }
            }
        });
    }

    public void addNewTab() {
        TextPaneTab newTab = new TextPaneTab(this);
        newTab.setSaved(false);

        tabbedPane.addTab(newTab.getTitle(), newTab);
        tabbedPane.setSelectedComponent(newTab);
    }

    public void createSettingsTab() {
        for (Component i : tabbedPane.getComponents()) {
            if (i instanceof Settings) {
                tabbedPane.setSelectedComponent(i);
                return;
            }
        }
        settingsPanel = new Settings(this);
        tabbedPane.addTab(settingsPanel.getTitle(), settingsPanel);
        tabbedPane.setSelectedComponent(settingsPanel);

        settingsPanel.setSaved(true);
    }

    public void closeWindow() {
        for (Component i : tabbedPane.getComponents()) {
            if (!(i instanceof Tab)) continue;

            if (((Tab) i).closeTab() == -1) {
                return;
            }
        }
        dispose();
    }

    private void closeTab(int tabIndex) {
        ((Tab) tabbedPane.getComponentAt(tabIndex)).closeTab();
    }

    public void getRandomTheme() {
        int newNumber;
        do {
            newNumber = random.nextInt(Settings.NUMBEROFTHEMES) + 1;
        }
        while (currentTheme == newNumber);

        getTheme(newNumber);
        settingsPanel.resetSettings();
        settingsPanel.saveFile();
    }

    public void getTheme(int themeNumber) {
        System.out.println("Theme " + themeNumber);
        currentTheme = themeNumber;
        switch (themeNumber) {
            case 0: {
                getRandomTheme();
            }
            break;
            case 1: {
                FlatLightLaf.setup();
                FlatLaf.updateUI();
            }
            break;
            case 2: {
                FlatSolarizedLightIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 3: {
                FlatGitHubIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 5: {
                FlatArcDarkIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 6: {
                FlatCarbonIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 7: {
                FlatMoonlightIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 8: {
                FlatMaterialOceanicIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 9: {
                FlatAtomOneDarkIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 10: {
                FlatSolarizedDarkIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 11: {
                FlatGradiantoDarkFuchsiaIJTheme.setup();
                FlatLaf.updateUI();
            }
            break;
            case 4:
            default: {
                FlatDarkLaf.setup();
                FlatLaf.updateUI();
            }
        }
    }

    public void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            openFile(fileChooser.getSelectedFile());
        }
    }

    public void openFile(File file) {
        try (FileReader fileReader = new FileReader(file); BufferedReader reader = new BufferedReader(fileReader)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            if (tabbedPane.getComponentAt(0) instanceof TextPaneTab && (tabbedPane.getTabCount() == 1 && ((TextPaneTab) tabbedPane.getComponentAt(0)).getTextPane().getText().equals(""))) {
                    tabbedPane.removeAll();
            }

            TextPaneTab newTab = new TextPaneTab(this);
            tabbedPane.addTab(newTab.getTitle(), newTab);
            newTab.setSaved(true);
            newTab.setTitle(file.getName());
            newTab.setTargetFile(file);
            newTab.setText(content.toString());
            tabbedPane.setSelectedComponent(newTab);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while opening the file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveFile() {
        ((Tab) tabbedPane.getSelectedComponent()).saveFile();
    }

    public void saveFileAs() {
        ((Tab) tabbedPane.getSelectedComponent()).saveFileAs();
    }

    public void changeFont(Font x) {
        for (Component i : tabbedPane.getComponents()) {
            if (i instanceof TextPaneTab) {
                ((TextPaneTab) i).setTextFont(x.deriveFont((float) fontSize));
            }
        }
        infoPanel.changeFont(x);
    }

    public void changeTextColor(Color x) {
        for (Component i : tabbedPane.getComponents()) {
            if (i instanceof TextPaneTab) {
                ((TextPaneTab) i).setTextColor(x);
            }
        }

        infoPanel.changeTextColor(x);

    }

    public void loadSettings() {
        System.out.println("loading");
        try(
                BufferedReader br = new BufferedReader(new FileReader("./config/config.cfg"))){
            if (!Files.exists(Paths.get("./config/config.cfg"))) {
                getRandomTheme();
                return;
            }

            ArrayList<String> lines = new ArrayList<>();
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                lines.add(currentLine);
            }

            switch (lines.size()) {
                case 6: {
                    setUsingDefaultTextColor(Integer.parseInt(lines.get(5).split("=")[1]) == 1);
                }
                case 5: {
                    currentTheme = Integer.parseInt(lines.get(4).split("=")[1]);
                }
                case 4: {
                    textColor = new Color(Integer.parseInt(lines.get(3).split("=")[1]));
                }
                case 3: {
                    setDefaultFont(new Font(lines.get(0).split("=")[1],
                            Integer.parseInt(lines.get(1).split("=")[1]),
                            Integer.parseInt(lines.get(2).split("=")[1])));
                }
                default:
                {
                    System.out.println("Empty config file");
                }
            }

            getTheme(currentTheme);
            settingsPanel = new Settings(this); //so that the default color changes // fix this
            changeTextColor(isUsingDefaultTextColor() ? settingsPanel.getDefaultTextColor() : textColor);
            changeFont(getDefaultFont());

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int getCurrentTheme() {
        return currentTheme;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color newColor) {
        textColor = newColor;
    }

    public boolean isUsingDefaultTextColor() {
        return isUsingDefaultTextColor;
    }

    public void setUsingDefaultTextColor(boolean usingDefaultTextColor) {
        isUsingDefaultTextColor = usingDefaultTextColor;
    }

    public Font getDefaultFont() {
        return defaultFont;
    }

    public void setDefaultFont(Font defaultFont) {
        this.defaultFont = defaultFont;
    }
}