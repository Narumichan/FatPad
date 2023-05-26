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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme;
import com.narumi.Tools.DragDropListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

public class FatPad extends JFrame implements Colorize {

    public static int currentTheme;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final HashMap<JScrollPane, JTextPane> tabToTextPaneMap = new HashMap<>();
    private final HashMap<JScrollPane, File> tabToFileMap = new HashMap<>();
    private final HashMap<JScrollPane, Boolean> tabSavedMap = new HashMap<>();
    private final JPanel infoPanel = new JPanel();
    private final UndoManager undoManager = new UndoManager();
    private final int fontSizeMin = 11;
    private final int fontSizeMax = 100;
    private final int zoomSize = 5;
    private final Action save = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            saveFile();
        }
    };
    private final Action saveAs = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            saveFileAs();
        }
    };
    private final Action createNewTab = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            createNewTab();
        }
    };
    private final Action closeCurrentTab = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            closeTab(tabbedPane.getSelectedComponent());
        }
    };
    public Color textColor = new Color(240, 240, 240);

    public Action undoText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canUndo()) {
                undoManager.undo();
                updateTitle();
            }
        }
    };

    public Action redoText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canRedo()) {
                undoManager.redo();
                updateTitle();
            }

        }
    };
    private int fontSize = 15;
    public Font defaultFont = new Font("Consoles", Font.PLAIN, fontSize);
    private Settings settingsPanel; //initialize at the bottom of init() before any themes take effect

    public FatPad() {
        createNewTab();
        init();
        //colorize();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(FatPad::new);

    }

    public void init() {
        setTitle("FatPad");

        setSize(1250, 800);
        setLocationRelativeTo(null);
        //setBackground(Color.MAGENTA);
        setMinimumSize(new Dimension(400, 300));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                settingsPanel.saveOwnersSettings();
                closeWindow();
            }
        });

        setupInfoPanel();
        setupTabbedPane();
        setJMenuBar(new Menu(this));

        add(tabbedPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        loadSettings();
        settingsPanel = new Settings(this); //after the theme is loaded
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
        JTextPane textPane = setupTextPane();
        JScrollPane scrollPane = setupScrollPane(textPane);

        tabbedPane.addTab("New File*", scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);
        tabToTextPaneMap.put(scrollPane, textPane);
        tabToFileMap.put(scrollPane, null);
        tabSavedMap.put(scrollPane, false);
    }

    private void closeWindow() {
        for (Component i : tabbedPane.getComponents()) {
            if (closeTab(i) == -1) {
                return;
            }
        }
        dispose();
    }

    private void closeTab(int tabIndex) {
        closeTab(tabbedPane.getComponentAt(tabIndex));
    }

    private int closeTab(Component selectedTab) {
        if (tabToTextPaneMap.get(selectedTab).getText().equals("") || tabSavedMap.get(selectedTab)) {
            removeTab(selectedTab);
            return 0;
        }

        int response = continueWithoutSavingDialog((tabToFileMap.get(selectedTab) == null)? "this file": tabToFileMap.get(selectedTab).getName());

        switch (response) {
            case JOptionPane.YES_OPTION: {
                saveFile(selectedTab);
                removeTab(selectedTab);
            }
            break;
            case JOptionPane.NO_OPTION: {
                removeTab(selectedTab);
            }
            break;
            case JOptionPane.CANCEL_OPTION: {
                return -1;
            }
        }

        return 0;
    }

    private void removeTab(Component tabToRemove) {
        tabbedPane.remove(tabToRemove);
        tabToTextPaneMap.remove(tabToRemove);
        updateTitle();
        tabSavedMap.remove(tabToRemove);
        tabToFileMap.remove(tabToRemove);
        if (tabbedPane.getComponents().length == 0)
            closeWindow();
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

            if (tabbedPane.getTabCount() == 1 && tabToTextPaneMap.get(tabbedPane.getComponentAt(0)).getText().equals("")) {
                tabToTextPaneMap.clear();
                tabSavedMap.clear();
                tabToFileMap.clear();
                tabbedPane.removeAll();
            }

            JTextPane textPane = setupTextPane();
            textPane.setText(content.toString());
            JScrollPane scrollPane = setupScrollPane(textPane);
            tabbedPane.addTab(file.getName(), scrollPane);
            tabbedPane.setSelectedComponent(scrollPane);
            tabToTextPaneMap.put(scrollPane, textPane);
            tabSavedMap.put(scrollPane, true);
            tabToFileMap.put(scrollPane, file);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while opening the file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextPane setupTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setFont(defaultFont);
        textPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        textPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        textPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save");
        textPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "Save As");
        textPane.getActionMap().put("Undo", undoText);
        textPane.getActionMap().put("Redo", redoText);
        textPane.getActionMap().put("Save", save);
        textPane.getActionMap().put("Save As", saveAs);
        textPane.setFocusTraversalKeysEnabled(false);
        textPane.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!e.isControlDown()) {
                    textPane.getParent().dispatchEvent(e);
                    return;
                }

                if (e.getWheelRotation() < 0) //Mouse up (???)
                {
                    changeFontSize(zoomSize);
                    textPane.setFont(defaultFont.deriveFont((float) fontSize));
                } else //Mouse down
                {
                    changeFontSize(-zoomSize);
                    textPane.setFont(defaultFont.deriveFont((float) fontSize));
                }
            }
        });
        new DropTarget(textPane, new DragDropListener(this));
        //https://www.specialagentsqueaky.com/blog-post/mbu5p27a/2011-01-09-drag-and-dropping-files-to-java-desktop-application/
        textPane.getDocument().addUndoableEditListener(undoManager);

        return textPane;
    }

    private JScrollPane setupScrollPane(JTextPane textPane) {
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setFocusable(false);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        scrollPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "Create a New Tab");
        scrollPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "Close the current tab");
        scrollPane.getActionMap().put("Create a New Tab", createNewTab);
        scrollPane.getActionMap().put("Close the current tab", closeCurrentTab);
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\u0013' || e.getKeyChar() == '\u001A' || e.getKeyChar() == '\u0019') //CTRL+S napise \u0013 character pa stavi da je unsaved i sve iako je upravo saved bilo 001A
                    return;
                //System.out.println("typing");
                //colorize();
                tabSavedMap.put(scrollPane, false);

                if (e.getKeyChar() == '\t') {
                    e.consume();
                }
                updateTitle();
            }
        });

        return scrollPane;
    }

    public void changeFontSize(int modifier) {
        fontSize += modifier;
        if (fontSize < fontSizeMin)
            fontSize = fontSizeMin;
        if (fontSize > fontSizeMax)
            fontSize = fontSizeMax;
    }

    public void updateTitle() {
        //colorize();

        Component selectedTab = tabbedPane.getSelectedComponent();
        File file = tabToFileMap.get(selectedTab);
        String newTitle = "New File";

        if (file != null) {
            newTitle = file.getName();
        }

        if (newTitle.endsWith("*")) newTitle = newTitle.substring(0, newTitle.length() - 1);

        if (!(selectedTab instanceof JScrollPane)) {
            return;
        }
        tabbedPane.setTitleAt(tabbedPane.indexOfComponent(selectedTab), (tabSavedMap.get(selectedTab) ? newTitle : newTitle + '*'));

    }

    public void saveFile() {
        saveFile(tabbedPane.getSelectedComponent());
    }

    public void saveFile(Component selectedComponent) {

        JTextPane textPane = tabToTextPaneMap.get(selectedComponent);
        if (selectedComponent != null && textPane != null) {
            File selectedFile = tabToFileMap.get(selectedComponent);
            if (selectedFile != null) {
                try {
                    System.out.println(selectedFile.getName());
                    //tabbedPane.setTitleAt(tabbedPane.indexOfComponent(selectedComponent), selectedFile.getName());
                    FileWriter fileWriter = new FileWriter(selectedFile);
                    String temp = textPane.getText();
                    fileWriter.write(temp);
                    fileWriter.close();
                    tabSavedMap.put((JScrollPane) selectedComponent, true);
                    updateTitle();
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                saveFileAs(selectedComponent);
            }
        }
    }

    public void saveFileAs(Component selectedComponent) {
        JTextPane textPane = tabToTextPaneMap.get(selectedComponent);
        if (selectedComponent != null && textPane != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (!Pattern.matches(".*\\..+", selectedFile.getName()))
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                try {
                    FileWriter fileWriter = new FileWriter(selectedFile);
                    String temp = textPane.getText();
                    fileWriter.write(temp);
                    fileWriter.close();
                    tabSavedMap.put((JScrollPane) selectedComponent, true);
                    System.out.println(selectedFile.getName());
                    //tabbedPane.setTitleAt(tabbedPane.indexOfComponent(selectedComponent), selectedFile.getName());
                    tabToTextPaneMap.put((JScrollPane) selectedComponent, textPane);
                    tabToFileMap.put((JScrollPane) selectedComponent, selectedFile);
                    updateTitle();
                } catch (IOException e) {

                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void saveFileAs() {
        saveFileAs(tabbedPane.getSelectedComponent());
    }

    public int continueWithoutSavingDialog(String selectedTab) {
        Object[] options = {"Save", "Don't Save", "Cancel"};
        return JOptionPane.showOptionDialog
                (
                        this,
                        "Close " + selectedTab + " without saving?",
                        "",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        null
                );

    }

    public void settings() {
        JDialog settingsDialog = new JDialog(this, "", true);
        settingsPanel.parent = settingsDialog;
        settingsPanel.resetThemeList();
        settingsDialog.getContentPane().add(settingsPanel);
        settingsDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settingsDialog.setSize(Settings.dimension);
        settingsDialog.setResizable(false);
        settingsDialog.setLocationRelativeTo(null);
        settingsDialog.setVisible(true);
    }

    public void changeFont(Font x) {
        tabToTextPaneMap.get(tabbedPane.getSelectedComponent()).setFont(x.deriveFont((float) fontSize));
    }

    public void changeTextColor(Color x) {
        tabToTextPaneMap.get(tabbedPane.getSelectedComponent()).setForeground(x);
    }

    public void saveSettings(ArrayList<String> linesToSave) {
        try {
            Files.createDirectories(Paths.get("./config/"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("./config/config.cfg"));
            for (String line : linesToSave) {
                bw.write(line + "\n");
            }
            bw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void loadSettings() {
        System.out.println("loading");
        try {
            if (!Files.exists(Paths.get("./config/config.cfg"))) {
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

    @Override
    public void colorize() {
        Random random = new Random();
        Color[] colors = new Color[10];

        // Generate a random hue within a range
        int hueRange = 10; // Adjust this value to control the range of hues
        float baseHue = random.nextFloat() * 360;

        for (int i = 0; i < 10; i++) {
            float hue = (baseHue + i * hueRange) % 360;

            // Generate random saturation within a range
            float saturationMin = 0.4f;
            float saturationMax = 0.5f;
            float saturationRange = saturationMax - saturationMin;
            float saturation = saturationMin + (saturationRange * random.nextFloat());

            // Generate random brightness within a range
            float brightnessMin = 0.3f;
            float brightnessMax = 0.5f;
            float brightnessRange = brightnessMax - brightnessMin;
            float brightness = brightnessMin + (brightnessRange * random.nextFloat());

            // Create the color
            colors[i] = Color.getHSBColor(hue / 360, saturation, brightness);
        }

        getJMenuBar().setBackground(colors[0]);
        setBackground(colors[1]);
        for (Component i : getComponents()) {
            i.setBackground(colors[2]);
        }
        for (Component i : tabToTextPaneMap.values()) {
            i.setBackground(colors[3]);
        }
        tabbedPane.setBackground(colors[4]);
        infoPanel.setBackground(colors[5]);



    }

    private Color randomColor() {
        int red = new Random().nextInt(256);
        int green = new Random().nextInt(256);
        int blue = new Random().nextInt(256);
        return new Color(red, green, blue);
    }
}