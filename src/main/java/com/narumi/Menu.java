package com.narumi;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Menu extends JMenuBar {
    public FatPad owner = null;
    public Menu(FatPad newOwner)
    {
        owner = newOwner;
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu theme = new JMenu("Theme");

        setFont(new Font("Ariel", Font.PLAIN,24));

        JMenuItem newFile = new JMenuItem("New File");
        JMenuItem newWindow = new JMenuItem("New Window");
        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As");
        JMenuItem exit = new JMenuItem("Exit");
        JMenuItem settings = new JMenuItem("Settings");
        JMenuItem randomizeTheme = new JMenuItem("Randomize Theme");


        newFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.clearTextPane();
            }
        });

        newWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new FatPad();
                    }
                });
            }
        });

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.openFile();
            }
        });

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.saveFile();
            }
        });

        saveAs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.saveAsFile();
            }
        });


        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.dispose();
            }
        });


        JMenuItem undo = new JMenuItem("Undo");
        JMenuItem redo = new JMenuItem("Redo");

        undo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ////////////////////////////
            }
        });

        redo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ////////////////////////////
            }
        });


        settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.settings();
            }
        });

        randomizeTheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.getRandomTheme();
            }
        });

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

    }

}
