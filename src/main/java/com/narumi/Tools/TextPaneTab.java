package com.narumi.Tools;

import com.narumi.FatPad;
import com.narumi.Tab;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TextPaneTab extends JScrollPane implements Tab {
    private final JTextArea lineNumberArea = new JTextArea();
    private FatPad owner;
    private String title;
    private File targetFile;
    private File targetColorFile;

    private boolean usingColors = false;

    private static final int FONTSIZEMIN = 11;
    private static final int FONTSIZEMAX = 70;
    private boolean saved;
    private JTextPane textPane;
    private final UndoManager undoManager = new UndoManager();
    public final Action undoText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canUndo()) {
                undoManager.undo();
                updateTitle();
            }
        }
    };
    public final Action redoText = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canRedo()) {
                undoManager.redo();
                updateTitle();
            }

        }
    };

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
    private int fontSize = 14;
    private static final int ZOOMSIZE = 5;

    public TextPaneTab(FatPad newOwner) {
        owner = newOwner;
        textPane = setupTextPane();
        updateTitle();
        textPane.setFont(owner.getDefaultFont());
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLineNumbers();
                updateTitle();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });

        lineNumberArea.setEditable(false);
        lineNumberArea.setFocusable(false);
        lineNumberArea.setFont(owner.getDefaultFont());

        setupScrollPane();
    }

    private void updateLineNumbers() {
        int totalLines = textPane.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLines; i++) {
            sb.append(i).append("\n");
        }
        lineNumberArea.setText(sb.toString());
    }

    public void setText(String text) {
        textPane.setText(text);
        updateLineNumbers();
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public void setTargetFile(File newTargetFile) {
        targetFile = newTargetFile;
        targetColorFile = new File("./config/styles/" + newTargetFile.getName().split("\\.")[0] + ".colors");

    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (owner.tabbedPane.indexOfComponent(this) != -1)
            owner.tabbedPane.setTitleAt(owner.tabbedPane.indexOfComponent(this), this.title);
    }

    private JTextPane setupTextPane() {
        textPane = new JTextPane();
        textPane.setFont(owner.getDefaultFont());
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
                    changeFontSize(ZOOMSIZE);
                } else //Mouse down
                {
                    changeFontSize(-ZOOMSIZE);
                }
                textPane.setFont(owner.getDefaultFont().deriveFont((float) fontSize));
                lineNumberArea.setFont(owner.getDefaultFont().deriveFont((float) fontSize));
            }
        });
        new DropTarget(textPane, new DragDropListener(this));
        //https://www.specialagentsqueaky.com/blog-post/mbu5p27a/2011-01-09-drag-and-dropping-files-to-java-desktop-application/
        textPane.getDocument().addUndoableEditListener(undoManager);

        return textPane;
    }

    public void updateTitle() {
        String newTitle = "New File";

        if (targetFile != null) newTitle = targetFile.getName();

        if (!saved) newTitle += '*';

        setTitle(newTitle);
    }

    public void changeFontSize(int modifier) {
        fontSize += modifier;
        if (fontSize < FONTSIZEMIN)
            fontSize = FONTSIZEMIN;
        if (fontSize > FONTSIZEMAX)
            fontSize = FONTSIZEMAX;
    }

    @Override
    public int closeTab() {
        if (isSaved()) {
            removeTab();
            return 0;
        }
        if (textPane.getText().equals("")) {
            removeTab();
            return 0;
        }

        String fileName = (targetFile == null) ? "this file" : targetFile.getName();
        int response = continueWithoutSavingDialog(fileName);

        switch (response) {
            case JOptionPane.YES_OPTION: {
                owner.saveFile();
                removeTab();
            }
            break;
            case JOptionPane.NO_OPTION: {
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
        if (textPane == null) return;
        if (targetFile == null) {
            saveFileAs();
            return;
        }
        //2 try blocks because even if the color writing fails, I'd rather try and write the text anyway because it's more important
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(targetFile))) {
            System.out.println(targetFile.getAbsolutePath());


            String temp = textPane.getText();
            fileWriter.write(temp);

            setSaved(true);
            updateTitle();

            System.out.println(targetColorFile.getParent());
            if (!Files.exists(Paths.get(targetColorFile.getParent())))
                Files.createDirectories(Paths.get(targetColorFile.getParent()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!usingColors) return;

        writeColorsFile();
    }

    private void writeColorsFile() {
        try (BufferedWriter colorsWriter = new BufferedWriter(new FileWriter(targetColorFile))) {
            System.out.println(targetColorFile.getAbsolutePath());
            StyledDocument doc = textPane.getStyledDocument();
            int length = doc.getLength();

            ArrayList<Color> usedColors = getUsedColors(doc);

            for (Color color : usedColors) {
                StringBuilder lineBuilder = new StringBuilder();
                lineBuilder.append(Utils.toHexString(color)).append(">");

                boolean inSegment = false;
                int segmentStart = 0;

                for (int i = 0; i < length; i++) {
                    Element element = doc.getCharacterElement(i);
                    AttributeSet attributes = element.getAttributes();
                    Color foreground = StyleConstants.getForeground(attributes);

                    if (foreground.equals(color)) {
                        if (!inSegment) {
                            segmentStart = i;
                            inSegment = true;
                        }
                    } else if (inSegment) {
                        lineBuilder.append(segmentStart).append(",").append(i - 1).append("|");
                        inSegment = false;
                    }
                }
                //if the last character was in a segment
                if (inSegment) {
                    lineBuilder.append(segmentStart).append(",").append(length - 1).append("|");
                }

                colorsWriter.write(lineBuilder.toString());
                colorsWriter.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ArrayList<Color> getUsedColors(StyledDocument doc) {
        int length = doc.getLength();
        ArrayList<Color> usedColors = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Element element = doc.getCharacterElement(i);
            AttributeSet attributes = element.getAttributes();
            Color foreground = StyleConstants.getForeground(attributes);
            if (!usedColors.contains(foreground)) usedColors.add(foreground);
        }
        return usedColors;
    }

    @Override
    public void saveFileAs() {
        if (textPane == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        if (!Pattern.matches(".*\\..+", selectedFile.getName()))
            selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
        try (FileWriter fileWriter = new FileWriter(selectedFile)) {
            String temp = textPane.getText();
            fileWriter.write(temp);

            setSaved(true);
            System.out.println(selectedFile.getAbsolutePath());

            setTargetFile(selectedFile);
            updateTitle();

            if (!Files.exists(Paths.get(targetColorFile.getParent())))
                Files.createDirectories(Paths.get(targetColorFile.getParent()));

        } catch (IOException e) {

            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
        }

        writeColorsFile();
    }

    @Override
    public int continueWithoutSavingDialog(String tabName) {
        Object[] options = {"Save", "Don't Save", "Cancel"};
        return JOptionPane.showOptionDialog
                (
                        this,
                        "Close " + tabName + " without saving?",
                        "",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        null
                );

    }

    private void setupScrollPane() {
        setViewportView(textPane);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        setFocusable(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "Create a New Tab");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), "Close the current tab");
        getActionMap().put("Create a New Tab", owner.createNewTab);
        getActionMap().put("Close the current tab", owner.closeCurrentTab);
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\u0013' || e.getKeyChar() == '\u001A' || e.getKeyChar() == '\u0019') //CTRL+S napise \u0013 character pa stavi da je unsaved i sve iako je upravo saved bilo 001A
                    return;
                setSaved(false);

                if (e.getKeyChar() == '\t') {
                    e.consume();
                }
                updateTitle();
            }
        });
    }

    public void setTextFont(Font font) {
        textPane.setFont(font);
    }

    public void setTextColor(Color color) {
        textPane.setForeground(color);
    }

    public FatPad getOwner() {
        return owner;
    }

    public boolean isUsingColors() {
        return usingColors;
    }

    public void setUsingColors(boolean usingColors) {
        this.usingColors = usingColors;
    }
}