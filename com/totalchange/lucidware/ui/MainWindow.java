package com.totalchange.lucidware.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import LucidWare;
import com.totalchange.lucidware.*;
import com.totalchange.lucidware.net.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class MainWindow extends TransparentWindow  implements FriendsArrayListListener {
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel background = new JLabel();
    JTextField textInput = new JTextField();
    JButton createButton = new JButton();
    JButton expandButton = new JButton();

    JLayeredPane layers = new JLayeredPane();

    ImageIcon mainBack = new ImageIcon(Globals.IMAGE_ROOT + "mainwindow.png");
    ImageIcon createIconOff = new ImageIcon(Globals.IMAGE_ROOT + "createoff.png");
    ImageIcon createIconOver = new ImageIcon(Globals.IMAGE_ROOT + "createover.png");
    ImageIcon createIconOn = new ImageIcon(Globals.IMAGE_ROOT + "createon.png");
    ImageIcon expandIconOff = new ImageIcon(Globals.IMAGE_ROOT + "expandoff.png");
    ImageIcon expandIconOver = new ImageIcon(Globals.IMAGE_ROOT + "expandover.png");
    ImageIcon expandIconOn = new ImageIcon(Globals.IMAGE_ROOT + "expandon.png");
    JLabel sizeLabel = new JLabel();

    private FriendsArrayList friends;
    private LucidWare parent;
    private ExpandWindow expandWindow;

    public MainWindow(FriendsArrayList friends, LucidWare parent) {
        this.friends = friends;
        this.parent = parent;

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        friends.addListener(this);

        super.setSize(240, 240);
        super.setVisible(true);
    }

    private void jbInit() throws Exception {
        textInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(FocusEvent e) {
                selectAllText();
            }
        });
        textInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                keywordSearch();
            }
        });
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                keywordSearch();
            }
        });
        expandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expandNetwork();
            }
        });

        this.getContentPane().setLayout(borderLayout1);

        background.setSize(240, 240);
        background.setIcon(mainBack);

        textInput.setOpaque(false);
        textInput.setBorder(null);
        textInput.setForeground(new Color(0, 128, 217));
        textInput.setFont(new Font("SansSerif", Font.BOLD, 12));
        textInput.setHorizontalAlignment(textInput.CENTER);
        textInput.setText("");
        textInput.setSize(197, 21);
        textInput.setLocation(21, 109);

        sizeLabel.setOpaque(false);
        sizeLabel.setBorder(null);
        sizeLabel.setForeground(new Color(0, 128, 217));
        sizeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        sizeLabel.setHorizontalAlignment(sizeLabel.CENTER);
        sizeLabel.setText("1");
        sizeLabel.setSize(30, 20);
        sizeLabel.setLocation(80, 190);

        createButton.setOpaque(false);
        createButton.setBorder(null);
        createButton.setIcon(createIconOff);
        createButton.setRolloverIcon(createIconOver);
        createButton.setPressedIcon(createIconOn);
        createButton.setContentAreaFilled(false);
        createButton.setSize(90, 25);
        createButton.setLocation(75, 143);

        expandButton.setOpaque(false);
        expandButton.setBorder(null);
        expandButton.setIcon(expandIconOff);
        expandButton.setRolloverIcon(expandIconOver);
        expandButton.setPressedIcon(expandIconOn);
        expandButton.setContentAreaFilled(false);
        expandButton.setSize(40, 40);
        expandButton.setLocation(125, 180);
        expandButton.setToolTipText("Expand...");

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                this_keyPressed(e);
            }
        });
        layers.add(background, new Integer(0));
        layers.add(createButton, new Integer(1));
        layers.add(expandButton, new Integer(1));
        layers.add(textInput, new Integer(2));
        layers.add(sizeLabel, new Integer(2));

        this.getContentPane().add(layers, BorderLayout.CENTER);
    }

    private void keywordSearch() {
        try {
            new KeywordSearch(textInput.getText(), friends);
        }
        catch(Exception e) {
            AlertWindow.alert("Insufficient information to create from.");
        }
    }

    private void expandNetwork() {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int xPos;
        int yPos;

        expandWindow = new ExpandWindow(friends);

        xPos = (screenSize.width / 2) - (expandWindow.getSize().width / 2);
        yPos = (screenSize.height / 2) - (expandWindow.getSize().height / 2);

        expandWindow.setLocation(xPos, yPos);
    }

    private void shutdown() {
        parent.shutdown();
    }

    private void selectAllText() {
        textInput.selectAll();
    }

    public void arraySizeEvent(int i) {
        sizeLabel.setText("" + i);
    }

    void this_keyPressed(KeyEvent e) {
        if (e.getKeyCode() == e.VK_ESCAPE) {
            new QuitThread();
        }
    }

    private class QuitThread extends Thread {
        public QuitThread() {
            this.start();
        }

        public void run() {
            QuestionWindow question = new QuestionWindow();
            question.setText("Are you sure you would like to quit?");
            question.setVisible(true);

            synchronized(question) {
                try {
                    question.wait();
                }
                catch(Exception e) {}
            }

            if (question.getResult()) {
                shutdown();
            }
        }
    }
}