package com.narumi.Tools;

import com.narumi.FatPad;
import com.narumi.Tab;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public class TextPaneTab extends JScrollPane implements Tab {
    private final JTextArea lineNumberArea = new JTextArea();
    private FatPad owner;
    private String title;
    private File targetFile;

    private final int fontSizeMin = 11;
    private final int fontSizeMax = 40;
    private boolean saved;
    private final JTextPane textPane;
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
    private final int zoomSize = 5;

    public TextPaneTab(FatPad newOwner) {
        owner = newOwner;
        textPane = setupTextPane();
        updateTitle();
        textPane.setFont(owner.defaultFont);
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLineNumbers();
                updateTitle();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLineNumbers();
                updateTitle();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLineNumbers();
                updateTitle();
            }
        });

        lineNumberArea.setEditable(false);
        lineNumberArea.setFocusable(false);
        lineNumberArea.setFont(owner.defaultFont);

        setupScrollPane();
        //setRowHeaderView(lineNumberArea);
        getVerticalScrollBar().setValue(getVerticalScrollBar().getValue()-1);
    }

    private void updateLineNumbers() {
        int totalLines = textPane.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLines; i++) {
            sb.append(i).append("\n");
        }
        lineNumberArea.setText(sb.toString());
        //lineNumberArea.setMargin(new Insets(0, 0, 0, 10));
    }

    private int getLineHeight() {
        FontMetrics fontMetrics = textPane.getFontMetrics(textPane.getFont());
        return fontMetrics.getHeight();
    }

    public String getText() {
        return textPane.getText();
    }

    public void setText(String text) {
        textPane.setText(text);
        updateLineNumbers();
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public JTextArea getLineArea() {
        return lineNumberArea;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File newTargetFile) {
        targetFile = newTargetFile;
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
        if(owner.tabbedPane.indexOfComponent(this) != -1)
            owner.tabbedPane.setTitleAt(owner.tabbedPane.indexOfComponent(this), this.title);
    }

    private JTextPane setupTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setFont(owner.defaultFont);
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
                    textPane.setFont(owner.defaultFont.deriveFont((float) fontSize));
                    lineNumberArea.setFont(owner.defaultFont.deriveFont((float) fontSize));
                } else //Mouse down
                {
                    changeFontSize(-zoomSize);
                    textPane.setFont(owner.defaultFont.deriveFont((float) fontSize));
                    lineNumberArea.setFont(owner.defaultFont.deriveFont((float) fontSize));
                }
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
        if (fontSize < fontSizeMin)
            fontSize = fontSizeMin;
        if (fontSize > fontSizeMax)
            fontSize = fontSizeMax;
        System.out.println(fontSize);
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
        if (textPane == null) return;
        if (targetFile == null) {
            saveFileAs();
            return;
        }

        try {
            System.out.println(targetFile.getAbsolutePath());

            FileWriter fileWriter = new FileWriter(targetFile);
            String temp = textPane.getText();
            fileWriter.write(temp);
            fileWriter.close();

            setSaved(true);
            updateTitle();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void saveFileAs() {
        if (textPane == null) return;

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

                setSaved(true);
                System.out.println(selectedFile.getAbsolutePath());

                setTargetFile(selectedFile);
                updateTitle();
            } catch (IOException e) {

                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error while saving the file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
                //colorize();
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
}