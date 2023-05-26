package com.narumi;

import com.narumi.Tools.TextPaneTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Menu extends JMenuBar {
    public FatPad owner;

    public Menu(FatPad newOwner) {
        owner = newOwner;
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu theme = new JMenu("Theme");

        setFont(owner.defaultFont.deriveFont(14F));

        JMenuItem newFile = new JMenuItem("New File");
        JMenuItem newWindow = new JMenuItem("New Window");
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");
        JMenuItem settings = new JMenuItem("Settings");
        JMenuItem randomizeTheme = new JMenuItem("Randomize Theme");

        newFile.addActionListener(e -> owner.createNewTab());

        newWindow.addActionListener(e -> EventQueue.invokeLater(FatPad::new));

        open.addActionListener(e -> owner.chooseFile());

        save.addActionListener(e -> owner.saveFile());

        saveAs.addActionListener(e -> owner.saveFileAs());


        exit.addActionListener(e -> owner.dispose());


        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");

        undo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!(owner.tabbedPane.getSelectedComponent() instanceof TextPaneTab)) return;
                ((TextPaneTab) owner.tabbedPane.getSelectedComponent()).undoText.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });

        redo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!(owner.tabbedPane.getSelectedComponent() instanceof TextPaneTab)) return;
                ((TextPaneTab) owner.tabbedPane.getSelectedComponent()).redoText.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });


        settings.addActionListener(e -> owner.createSettingsTab());

        randomizeTheme.addActionListener(e -> owner.getRandomTheme());

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
            i.setFont(owner.defaultFont);
            for (Component j : ((JMenuItem) i).getComponents()) {
                j.setFont(owner.defaultFont);
            }
        }

    }

}
