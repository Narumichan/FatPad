package com.narumi;

/**
 *  tab lines
 *
 *  line count
 *
 *  row + char position
 *
 *  word count
 *  char count
 *
 *  better settings menu
 *
 *  ctrl c + ctrl v
 *
 *  background image
 *  opacity slider in the info panel
 *
 *  better config files (save version, append new data) (json?)
 *
 *  error reading file
 *
 *  current directory when opening file
 *  better directory chooser (github)
 *
 *
 */

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme;
import com.narumi.Tools.DragDropListener;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

public class FatPad extends JFrame {

    public static int currentTheme;
    private String textFromFile = "";
    public final JTextPane textPane1 = new JTextPane();
    private final JScrollPane scrollTextPane = new JScrollPane(textPane1);
    private final JPanel infoPanel = new JPanel();
    private UndoManager undoManager = new UndoManager();

    public boolean saved = false;

    public File targetFile = null;

    private int tabStopNumber = 0;
    public int tabSize = 20;
    private TabSet tabSet = new TabSet(new TabStop[]{});

    public boolean dragEnabled = false;

    private static int themeNumber;
    private int fontSize = 15;
    private int fontSizeMin = 11;
    private int zoomSize = 5;

    public Font defaultFont = new Font("Consoles", Font.PLAIN, fontSize);

    public Color textColor = new Color(240,240,240);

    public Action undoText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canUndo())
            {
                undoManager.undo();
                saved = false;
                updateTitle();
            }
        }
    };


    public Action tabText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            System.out.println("tabbed at index " + textPane1.getCaretPosition());
            try
            {
                textPane1.getDocument().insertString(textPane1.getCaretPosition(), "////", null);
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }

            //textPane1.setText(insertTab(textPane1.getCaretPosition()));
        }
    };

    public Action redoText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if(undoManager.canRedo())//da bude unsaved text samo ako se redo ili undo actually dese a ne kao i ctrl+s da salje uvek unicode char{
            {
                undoManager.redo();
                saved = false;
                updateTitle();
            }

        }
    };
    private Action save = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            saveFile();
        }
    };
    private Action saveAs = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            saveAsFile();
        }
    };
    private Settings settingsPanel = null; //initialize at the bottom of init() before any themes take effect
    private StyleContext sc = null;

    public static void main(String[] args) {
        getRandomTheme();

        if(args.length == 0)
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new FatPad();
                }
            });
        }
        else
        {
            for(int i=0;i<args.length;++i)
            {
                int xd = i; //workaround jer i ne moze u inner class
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new FatPad(args[xd], xd*200);
                    }
                });
            }

        }

    }
    public FatPad()
    {
        newFile();
        init();
        setLocationRelativeTo(null);
    }

    public FatPad(String path, int offset) // fix this
    {
        targetFile = new File(path);
        readFile();
        init();
        setLocation(Math.round(offset+1920/2-this.getWidth()/2), Math.round(1080/2-this.getHeight()/2));
    }

    public void init()
    {
        //setTitle("FatPad");
        if (new File("./config/fatpad.png").exists())
        {
            setIconImage(Toolkit.getDefaultToolkit().getImage("./config/fatpad.png"));
        } else {
            setIconImage(null);
        }
        setSize(1250, 800);
        //setLocationRelativeTo(null);
        //setBackground(Color.MAGENTA);
        setMinimumSize(new Dimension(400, 300));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(5,5));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                settingsPanel.saveOwnersSettings();
                closeWindow();
            }
        });


        textPane1.setFont(defaultFont);
        textPane1.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        textPane1.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        textPane1.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save");
        //textPane1.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("TAB"), "TabText");
        textPane1.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "Save As");
        textPane1.getActionMap().put("Undo", undoText);
        textPane1.getActionMap().put("Redo", redoText);
        textPane1.getActionMap().put("Save", save);
        textPane1.getActionMap().put("Save As", saveAs);
        //textPane1.getActionMap().put("TabText", tabText);
        textPane1.setFocusTraversalKeysEnabled(false);
        textPane1.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!e.isControlDown())
                {
                    textPane1.getParent().dispatchEvent(e);
                    return;
                }

                if (e.getWheelRotation() < 0) //Mouse up (???)
                {
                    changeFontSize(zoomSize);
                    textPane1.setFont(defaultFont.deriveFont((float) fontSize));
                }
                else //Mouse down
                {
                    changeFontSize(-zoomSize);
                    textPane1.setFont(defaultFont.deriveFont((float) fontSize));
                }
            }
        });
        textPane1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == '\u0013' || e.getKeyChar() == '\u001A' || e.getKeyChar() == '\u0019') //CTRL+S napise \u0013 character pa stavi da je unsaved i sve iako je upravo saved bilo 001A
                    return;
                saved = false;
                System.out.println("typing");
                if(e.getKeyChar() == '\t')
                {
                    e.consume();
                    addTabStop();
                }
                updateTitle();
            }
        });
        new DropTarget(textPane1, new DragDropListener(this));
        //https://www.specialagentsqueaky.com/blog-post/mbu5p27a/2011-01-09-drag-and-dropping-files-to-java-desktop-application/
        textPane1.getDocument().addUndoableEditListener(undoManager);


        scrollTextPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollTextPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollTextPane.setFocusable(false);
        scrollTextPane.setBorder(new EmptyBorder(0,0,0,0));

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setPreferredSize(new Dimension(300,30));
        infoPanel.setMinimumSize(new Dimension(300,30));
        infoPanel.setMaximumSize(new Dimension(300,30));
        //infoPanel.setBackground(new Color(255,0,0));





        addTabStop(); //add the first one

        add(scrollTextPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        setJMenuBar(new Menu(this));
        loadSettings();
        settingsPanel = new Settings(this); //after the theme is loaded
        setVisible(true);
    }

    private void addTabStop() //https://stackoverflow.com/questions/757692/how-do-you-set-the-tab-size-in-a-jeditorpane
    {
        tabStopNumber++;
        TabStop[] newTabStops = new TabStop[tabSet.getTabCount()+1];
        for(int i = 0; i < tabSet.getTabCount(); ++i)
        {
            newTabStops[i] = tabSet.getTab(i);
        }
        newTabStops[tabSet.getTabCount()] = new TabStop(tabStopNumber * tabSize);
        tabSet = new TabSet(newTabStops);
        applyTabStops();
    }
    private void resetTabStops()
    {
        TabStop[] newTabStops = new TabStop[tabSet.getTabCount()];
        for(int i = 0; i < tabSet.getTabCount(); ++i)
        {
            newTabStops[i] = new TabStop((i+1) * tabSize);
        }
        tabSet = new TabSet(newTabStops);
        applyTabStops();
    }

    // Need to keep track of the number of \r characters, because the getText() method counts them,
    // but getDocument() (which the caret method uses internally) only sees \n
    // Difference between getText().length and getDocument().getLength() will always be the number of \r characters
    private void applyTabStops()
    {
        sc = StyleContext.getDefaultStyleContext();
        AttributeSet paraSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);

        ArrayList<Integer> endOfLinePositions = new ArrayList<Integer>();
        int x = textPane1.getText().length();
        int carriageCounter = 0;
        for(int i = 0; i < textPane1.getText().length(); ++i)
        {
            char y = textPane1.getText().charAt(i);
            if(textPane1.getText().charAt(i) == '\r')
            {
                carriageCounter++;
                continue;
            }
            if(textPane1.getText().charAt(i) == '\n')
            {
                endOfLinePositions.add(i);
                textPane1.setCaretPosition(i-carriageCounter);
                textPane1.setParagraphAttributes(paraSet, false);
            }
        }

        /*for(int pos : endOfLinePositions)
        {
            int xx = textPane1.getDocument().getLength();
            textPane1.setCaretPosition(pos);
            textPane1.setParagraphAttributes(paraSet, false);
        }*/

    }

    private void closeWindow()
    {
        if(saved || textPane1.getText().trim() == "")
        {
            this.dispose();
        }
        else
        {
            int response = continueWithoutSavingDialog();

            if(response == JOptionPane.YES_OPTION)
            {
                saveFile();
                this.dispose();
            }
            else if(response == JOptionPane.NO_OPTION)
            {
                this.dispose();
            }
            else if(response == JOptionPane.CANCEL_OPTION)
            {
                saved = false;
            }
        }
    }

    public void changeFontSize(int modifier)
    {

        fontSize += modifier;
        if (fontSize < fontSizeMin)
            fontSize = fontSizeMin;
        //if (fontSize > fontSizeMax)
        //    fontSize = fontSizeMax;
    }

    public static void getRandomTheme() {
        int newNumber = new Random().nextInt(Settings.numberOfThemes);
        while (themeNumber == newNumber)
        {
            newNumber = new Random().nextInt(Settings.numberOfThemes);
        }
        themeNumber = newNumber;

        getTheme(themeNumber+1);

    }

    public static void getTheme(int themeNumber)
    {
        System.out.println("Theme " + themeNumber);
        currentTheme = themeNumber;
        switch(themeNumber)
        {
            case 1:
            {
                FlatLightLaf.setup();
                FlatLightLaf.updateUI();
            }break;
            case 2:
            {
                FlatSolarizedLightIJTheme.setup();
                FlatSolarizedLightIJTheme.updateUI();
            }break;
            case 3:
            {
                FlatGitHubIJTheme.setup();
                FlatGitHubIJTheme.updateUI();
            }break;
            case 4:
            {
                FlatDarkLaf.setup();
                FlatDarkLaf.updateUI();
            }break;
            case 5:
            {
                FlatArcDarkIJTheme.setup();
                FlatArcDarkIJTheme.updateUI();
            }break;
            case 6:
            {
                FlatCarbonIJTheme.setup();
                FlatCarbonIJTheme.updateUI();
            }break;
            case 7:
            {
                FlatMoonlightIJTheme.setup();
                FlatMoonlightIJTheme.updateUI();
            }break;
            case 8:
            {
                FlatMaterialOceanicIJTheme.setup();
                FlatMaterialOceanicIJTheme.updateUI();
            }break;
            case 9:
            {
                FlatAtomOneDarkIJTheme.setup();
                FlatAtomOneDarkIJTheme.updateUI();
            }break;
            case 10:
            {
                FlatSolarizedDarkIJTheme.setup();
                FlatSolarizedDarkIJTheme.updateUI();
            }break;
            case 11:
            {
                FlatGradiantoDarkFuchsiaIJTheme.setup();
                FlatGradiantoDarkFuchsiaIJTheme.updateUI();
            }break;
        }
    }
    public void clearTextPane()
    {
        if(saved)
        {
            newFile();
        }
        else
        {
            int response = continueWithoutSavingDialog();

            if(response == JOptionPane.YES_OPTION)
            {
                saveFile();
                newFile();
            }
            else if(response == JOptionPane.NO_OPTION)
            {
                newFile();
            }
        }
    }

    public void newFile()
    {
        textPane1.setText("");
        undoManager.discardAllEdits();
        targetFile = null;
        updateTitle();
    }

    public void openFile()
    {
        if(saved)
        {
            JFileChooser fc = new JFileChooser();
            int response = fc.showOpenDialog(this);

            if(response == JFileChooser.APPROVE_OPTION)
            {
                targetFile = fc.getSelectedFile();
                readFile();
            }
        }
        else
        {
            int response = continueWithoutSavingDialog();

            if(response == JOptionPane.YES_OPTION)
            {
                saveFile();
                openFile();
            }
            else if(response == JOptionPane.NO_OPTION)
            {
                saved = true;
                openFile();
            }
            else if(response == JOptionPane.CANCEL_OPTION)
            {
                saved = false;
            }
        }

    }

    /*public void readFile()
    {
        try
        {
            String input = "";
            String line;
            BufferedReader br = new BufferedReader(new FileReader(targetFile));
            while((line = br.readLine()) != null)
            {
                input += line + "\n";
            }
            br.close();
            saved = true;
            updateTitle();
            textPane1.setText(input.substring(0,input.length()-1));

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }*/
    public void readFile()
    {
        try
        {
           //String textFromFile = new FileLoadWorker(this,targetFile.getAbsolutePath()).execute();
           SwingWorker sw1 = new SwingWorker(){

               @Override
               protected Object doInBackground() throws Exception {
                   String line;
                   BufferedReader br = new BufferedReader(new FileReader(targetFile));
                   while((line = br.readLine()) != null)
                   {
                       textFromFile += line + "\n";
                   }
                   br.close();
                   textPane1.setText(textFromFile.substring(0,textFromFile.length()-1));
                   textFromFile = "";
                   return null;
               }
           };
           sw1.execute();
           saved = true;
           updateTitle();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }



    public void saveAsFile()
    {
        JFileChooser fc = new JFileChooser();
        int response = fc.showSaveDialog(this);

        if(response == JFileChooser.APPROVE_OPTION)
        {
            targetFile = fc.getSelectedFile();
        }

        writeToFile();

    }

    public void saveFile()
    {
        if(targetFile == null)
        {
            saveAsFile();
            return;
        }
        System.out.println("wirin to file");
        writeToFile();

    }

    private void writeToFile()
    {
        if(saved) return;
        if(targetFile == null) return;
        System.out.println("saving file!!!!");

        String filePath = "";
        try
        {
            if(FilenameUtils.getExtension(targetFile.getName()) == "")
            {
                filePath = targetFile.getAbsolutePath() + ".txt";
            }
            else
            {
                filePath = targetFile.getAbsolutePath();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(textPane1.getText());
            writer.close();
            System.out.println("Wrote to file: saved");
            saved = true;
            updateTitle();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void updateTitle()
    {
        //System.out.println("updating title");
        String newTitle = "";
        if(targetFile == null)
        {
            newTitle = "New File";
        }
        else
        {
            if (!saved)
            {
                newTitle += "*";
            }

            if(FilenameUtils.getExtension(targetFile.getName()) == "")
            {
                newTitle += targetFile.getName()+ ".txt";
            }
            else
            {
                newTitle += targetFile.getName();
            }

        }
        //System.out.println(newTitle);

        setTitle(newTitle);
    }

    public int continueWithoutSavingDialog()
    {
        Object[] options = {"Save", "Don't Save", "Cancel"};
        return JOptionPane.showOptionDialog
                (
            this,
            "Save before exiting?",
            "",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            null
                );

    }

    public void settings()
    {
        JDialog settingsDialog = new JDialog(this, "",true);
        settingsPanel.parent = settingsDialog;
        settingsPanel.resetThemeList();
        settingsDialog.getContentPane().add(settingsPanel);
        settingsDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        settingsDialog.setSize(Settings.dimension);
        settingsDialog.setResizable(false);
        settingsDialog.setLocationRelativeTo(null);
        settingsDialog.setVisible(true);
    }

    public void changeFont(Font x)
    {
        textPane1.setFont(x.deriveFont((float) fontSize));
    }

    public void changeTextColor(Color x)
    {
        textPane1.setForeground(x);
    }

    public void saveSettings(ArrayList<String> linesToSave)
    {
        try
        {
            Files.createDirectories(Paths.get("./config/"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("./config/config.cfg"));
            for(String line : linesToSave)
            {
                bw.write(line + "\n");
            }
            bw.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public void setDrag(boolean drag) {
        System.out.println("Drag is set to " + drag);
        textPane1.setDragEnabled(drag);
        textPane1.setTransferHandler(new TransferHandler("text"));
    }
    public void loadSettings()
    {
        System.out.println("loading");
        try
        {
            if(!Files.exists(Paths.get("./config/config.cfg")))
            {
                return;
            }

            ArrayList<String> lines = new ArrayList<String>();
            String currentLine;
            BufferedReader br = new BufferedReader(new FileReader("./config/config.cfg"));
            while((currentLine = br.readLine()) != null)
            {
                lines.add(currentLine);
            }
            br.close();

            defaultFont = new Font(lines.get(0).split("=")[1],
                    Integer.parseInt(lines.get(1).split("=")[1]),
                    Integer.parseInt(lines.get(2).split("=")[1]));

            textColor = new Color(Integer.parseInt(lines.get(3).split("=")[1]));
            currentTheme = Integer.parseInt(lines.get(4).split("=")[1]);
            dragEnabled = Boolean.parseBoolean(lines.get(5).split("=")[1]);

            getTheme(currentTheme);
            changeTextColor(textColor);
            changeFont(defaultFont);
            setDrag(dragEnabled);

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void setTabSize(int value)
    {
        tabSize = value;
        resetTabStops();
    }
}