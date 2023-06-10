package com.narumi;

import com.narumi.Tools.TextPaneTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Menu extends JMenuBar {
    private FatPad owner;

    public Menu(FatPad newOwner) {
        setOwner(newOwner);
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu theme = new JMenu("Theme");

        setFont(getOwner().getDefaultFont().deriveFont(14F));

        JMenuItem newFile = new JMenuItem("New File");
        JMenuItem newWindow = new JMenuItem("New Window");
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");
        JMenuItem settings = new JMenuItem("Settings");
        JMenuItem randomizeTheme = new JMenuItem("Randomize Theme");

        newFile.addActionListener(e -> getOwner().addNewTab());

        newWindow.addActionListener(e -> EventQueue.invokeLater(FatPad::new));

        open.addActionListener(e -> getOwner().chooseFile());

        save.addActionListener(e -> getOwner().saveFile());

        saveAs.addActionListener(e -> getOwner().saveFileAs());


        exit.addActionListener(e -> getOwner().dispose());


        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");

        undo.addActionListener(e -> {
            if (!(getOwner().tabbedPane.getSelectedComponent() instanceof TextPaneTab)) return;
            ((TextPaneTab) getOwner().tabbedPane.getSelectedComponent()).undoText.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        });


        redo.addActionListener(e -> {
            if (!(getOwner().tabbedPane.getSelectedComponent() instanceof TextPaneTab)) return;
            ((TextPaneTab) getOwner().tabbedPane.getSelectedComponent()).redoText.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        });

        settings.addActionListener(e -> getOwner().createSettingsTab());

        randomizeTheme.addActionListener(e -> getOwner().getRandomTheme());

        file.add(newFile);
        file.add(open);
        file.add(newWindow);
        file.addSeparator();
        file.add(save);
        file.add(saveAs);
        file.addSeparator();
        file.add(exit);

        edit.add(undo);
        edit.add(redo);
        edit.addSeparator();
        edit.add(settings);

        theme.add(randomizeTheme);

        add(file);
        add(edit);
        add(theme);

        for (Component i : getComponents()) {
            i.setFont(getOwner().getDefaultFont());
            for (Component j : ((JMenuItem) i).getComponents()) {
                j.setFont(getOwner().getDefaultFont());
            }
        }

    }

    public FatPad getOwner() {
        return owner;
    }

    public void setOwner(FatPad owner) {
        this.owner = owner;
    }
}
