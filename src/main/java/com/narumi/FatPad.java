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

   better config files (save version, append new data) (json?)

   error reading file

   current directory when opening file
   better directory chooser (github)


 */

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme;
import com.narumi.Tools.TextPaneTab;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class FatPad extends JFrame {

    public final JTabbedPane tabbedPane = new JTabbedPane();
    private final JPanel infoPanel = new JPanel();

    public int currentTheme;
    public Color textColor = new Color(240, 240, 240);
    private int fontSize = 15;
    public Font defaultFont = new Font("Consoles", Font.PLAIN, fontSize);
    public final Action closeCurrentTab = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            ((Tab) tabbedPane.getSelectedComponent()).closeTab();
        }
    };
    public final Action createNewTab = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            createNewTab();
        }
    };
    private Settings settingsPanel; //initialize after theme changes

    public FatPad() {
        init();
        createNewTab();
        //colorize();
        settingsPanel = new Settings(this, tabbedPane);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(FatPad::new);

    }

    public void init() {
        setTitle("FatPad");
        setIconImage(new ImageIcon("./config/fatpad.png").getImage());
        getRootPane().putClientProperty( FlatClientProperties.TITLE_BAR_SHOW_ICON, false );
        getRootPane().putClientProperty( FlatClientProperties.TITLE_BAR_SHOW_TITLE, false );

        setSize(1250, 800);
        setLocationRelativeTo(null);
        //setBackground(Color.MAGENTA);
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

        setupInfoPanel();
        setupTabbedPane();
        setJMenuBar(new Menu(this));

        add(tabbedPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        loadSettings();
        setVisible(true);
    }

    private void setupTabbedPane() {
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex == -1) {
                        createNewTab();
                    } else {
                        closeTab(tabIndex);
                    }


                }
            }
        });
    }

    private void setupInfoPanel() {
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setPreferredSize(new Dimension(300, 30));
        infoPanel.setMinimumSize(new Dimension(300, 30));
        infoPanel.setMaximumSize(new Dimension(300, 30));
        //infoPanel.setBackground(new Color(255,0,0));
    }

    public void createNewTab() {
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
        settingsPanel = new Settings(this, tabbedPane);
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
            newNumber = new Random().nextInt(Settings.numberOfThemes) + 1;
        }
        while (currentTheme == newNumber);

        getTheme(newNumber);

    }

    public void getTheme(int themeNumber) {
        System.out.println("Theme " + themeNumber);
        currentTheme = themeNumber;
        switch (themeNumber) {
            case 1: {
                FlatLightLaf.setup();
                FlatLightLaf.updateUI();
            }
            break;
            case 2: {
                FlatSolarizedLightIJTheme.setup();
                FlatSolarizedLightIJTheme.updateUI();
            }
            break;
            case 3: {
                FlatGitHubIJTheme.setup();
                FlatGitHubIJTheme.updateUI();
            }
            break;
            case 4: {
                FlatDarkLaf.setup();
                FlatDarkLaf.updateUI();
            }
            break;
            case 5: {
                FlatArcDarkIJTheme.setup();
                FlatArcDarkIJTheme.updateUI();
            }
            break;
            case 6: {
                FlatCarbonIJTheme.setup();
                FlatCarbonIJTheme.updateUI();
            }
            break;
            case 7: {
                FlatMoonlightIJTheme.setup();
                FlatMoonlightIJTheme.updateUI();
            }
            break;
            case 8: {
                FlatMaterialOceanicIJTheme.setup();
                FlatMaterialOceanicIJTheme.updateUI();
            }
            break;
            case 9: {
                FlatAtomOneDarkIJTheme.setup();
                FlatAtomOneDarkIJTheme.updateUI();
            }
            break;
            case 10: {
                FlatSolarizedDarkIJTheme.setup();
                FlatSolarizedDarkIJTheme.updateUI();
            }
            break;
            case 11: {
                FlatGradiantoDarkFuchsiaIJTheme.setup();
                FlatGradiantoDarkFuchsiaIJTheme.updateUI();
            }
            break;
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
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            fileReader.close();

            if (tabbedPane.getComponentAt(0) instanceof TextPaneTab) {
                if (tabbedPane.getTabCount() == 1 && ((TextPaneTab) tabbedPane.getComponentAt(0)).getTextPane().getText().equals("")) {
                    tabbedPane.removeAll();
                }

            }

            TextPaneTab newTab = new TextPaneTab(this);
            newTab.setSaved(true);
            newTab.setTitle(file.getName());
            newTab.setTargetFile(file);
            newTab.setText(content.toString());
            tabbedPane.addTab(newTab.getTitle(), newTab);
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
    }

    public void changeTextColor(Color x) {
        for (Component i : tabbedPane.getComponents()) {
            if (i instanceof TextPaneTab) {
                ((TextPaneTab) i).setTextColor(x);
            }
        }
    }

    public void loadSettings() {
        System.out.println("loading");
        try {
            if (!Files.exists(Paths.get("./config/config.cfg"))) {
                getRandomTheme();
                return;
            }

            ArrayList<String> lines = new ArrayList<>();
            String currentLine;
            BufferedReader br = new BufferedReader(new FileReader("./config/config.cfg"));
            while ((currentLine = br.readLine()) != null) {
                lines.add(currentLine);
            }
            br.close();

            defaultFont = new Font(lines.get(0).split("=")[1],
                    Integer.parseInt(lines.get(1).split("=")[1]),
                    Integer.parseInt(lines.get(2).split("=")[1]));

            textColor = new Color(Integer.parseInt(lines.get(3).split("=")[1]));
            currentTheme = Integer.parseInt(lines.get(4).split("=")[1]);

            getTheme(currentTheme);
            changeTextColor(textColor);
            changeFont(defaultFont);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

}