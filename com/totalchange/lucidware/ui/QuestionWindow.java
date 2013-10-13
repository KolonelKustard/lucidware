package com.totalchange.lucidware.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.totalchange.lucidware.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class QuestionWindow extends TransparentWindow {
    BorderLayout borderLayout1 = new BorderLayout();
    JLayeredPane layers = new JLayeredPane();
    JLabel background = new JLabel();
    JButton exitButton = new JButton();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JLabel textLabel = new JLabel();

    ImageIcon iconBack = new ImageIcon(Globals.IMAGE_ROOT + "dialogquestion.png");
    ImageIcon iconExitOff = new ImageIcon(Globals.IMAGE_ROOT + "closeoff.png");
    ImageIcon iconExitOver = new ImageIcon(Globals.IMAGE_ROOT + "closeover.png");
    ImageIcon iconExitOn = new ImageIcon(Globals.IMAGE_ROOT + "closeon.png");
    ImageIcon iconOkOff = new ImageIcon(Globals.IMAGE_ROOT + "okoff.png");
    ImageIcon iconOkOver = new ImageIcon(Globals.IMAGE_ROOT + "okover.png");
    ImageIcon iconOkOn = new ImageIcon(Globals.IMAGE_ROOT + "okon.png");
    ImageIcon iconCancelOff = new ImageIcon(Globals.IMAGE_ROOT + "canceloff.png");
    ImageIcon iconCancelOver = new ImageIcon(Globals.IMAGE_ROOT + "cancelover.png");
    ImageIcon iconCancelOn = new ImageIcon(Globals.IMAGE_ROOT + "cancelon.png");

    private boolean result = false;

    public QuestionWindow() {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int xPos;
        int yPos;

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        super.setSize(400, 100);

        xPos = (screenSize.width / 2) - (super.getSize().width / 2);
        yPos = (screenSize.height / 2) - (super.getSize().height / 2);
        super.setLocation(xPos, yPos);

        super.setVisible(true);
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);

        background.setIcon(iconBack);
        background.setSize(400, 100);

        exitButton.setBorder(null);
        exitButton.setOpaque(false);
        exitButton.setIcon(iconExitOff);
        exitButton.setRolloverIcon(iconExitOver);
        exitButton.setPressedIcon(iconExitOn);
        exitButton.setContentAreaFilled(false);
        exitButton.setSize(10, 10);
        exitButton.setLocation(389, 1);
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });

        okButton.setBorder(null);
        okButton.setOpaque(false);
        okButton.setIcon(iconOkOff);
        okButton.setRolloverIcon(iconOkOver);
        okButton.setPressedIcon(iconOkOn);
        okButton.setContentAreaFilled(false);
        okButton.setSize(90, 25);
        okButton.setLocation(105, 54);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

        cancelButton.setBorder(null);
        cancelButton.setOpaque(false);
        cancelButton.setIcon(iconCancelOff);
        cancelButton.setRolloverIcon(iconCancelOver);
        cancelButton.setPressedIcon(iconCancelOn);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setSize(90, 25);
        cancelButton.setLocation(205, 54);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });

        textLabel.setOpaque(false);
        textLabel.setBorder(null);
        textLabel.setForeground(new Color(0, 128, 217));
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        textLabel.setHorizontalAlignment(textLabel.CENTER);
        textLabel.setSize(340, 21);
        textLabel.setLocation(30, 25);

        layers.add(background, new Integer(0));
        layers.add(exitButton, new Integer(1));
        layers.add(okButton, new Integer(1));
        layers.add(cancelButton, new Integer(1));
        layers.add(textLabel, new Integer(2));

        this.getContentPane().add(layers, BorderLayout.CENTER);
    }

    private void okPressed() {
        result = true;
        kill();
    }

    private void cancelPressed() {
        result = false;
        kill();
    }

    private void kill() {
        super.setVisible(false);
        super.dispose();

        synchronized(this) {
            try {
                this.notify();
            }
            catch(Exception e) {e.printStackTrace();}
        }
    }

    public void setText(String s) {
        textLabel.setText(s);
    }

    public boolean getResult() {
        return result;
    }

    public static boolean question(String questionText) {
        QuestionWindow questionWindow = new QuestionWindow();
        questionWindow.setText(questionText);
        boolean result;

        synchronized(questionWindow) {
            try {
                questionWindow.wait();
            }
            catch(Exception e) {e.printStackTrace();}
        }

        result = questionWindow.getResult();
        questionWindow = null;

        return result;
    }

    public static void main(String[] args) {
        System.out.println(QuestionWindow.question("This is a test question."));
        System.exit(0);
    }
}