package com.totalchange.lucidware.ui;

import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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

public class ExpandWindow extends TransparentWindow {
    BorderLayout borderLayout1 = new BorderLayout();
    JLayeredPane layers = new JLayeredPane();
    JLabel background = new JLabel();
    JButton exitButton = new JButton();
    JButton addButton = new JButton();
    JButton cancelButton = new JButton();
    JTextField textInput = new JTextField();

    ImageIcon iconBack = new ImageIcon(Globals.IMAGE_ROOT + "dialogexpand.png");
    ImageIcon iconExitOff = new ImageIcon(Globals.IMAGE_ROOT + "closeoff.png");
    ImageIcon iconExitOver = new ImageIcon(Globals.IMAGE_ROOT + "closeover.png");
    ImageIcon iconExitOn = new ImageIcon(Globals.IMAGE_ROOT + "closeon.png");
    ImageIcon iconAddOff = new ImageIcon(Globals.IMAGE_ROOT + "addoff.png");
    ImageIcon iconAddOver = new ImageIcon(Globals.IMAGE_ROOT + "addover.png");
    ImageIcon iconAddOn = new ImageIcon(Globals.IMAGE_ROOT + "addon.png");
    ImageIcon iconCancelOff = new ImageIcon(Globals.IMAGE_ROOT + "canceloff.png");
    ImageIcon iconCancelOver = new ImageIcon(Globals.IMAGE_ROOT + "cancelover.png");
    ImageIcon iconCancelOn = new ImageIcon(Globals.IMAGE_ROOT + "cancelon.png");

    private FriendsArrayList friends;

    public ExpandWindow(FriendsArrayList friends, Window owner) {
        super(owner);
        this.friends = friends;

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        super.setSize(400, 100);
    }

    public ExpandWindow(FriendsArrayList friends) {
        this.friends = friends;

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        super.setSize(400, 100);
    }

    public ExpandWindow() {
        this(null, new JWindow());
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

        addButton.setBorder(null);
        addButton.setOpaque(false);
        addButton.setIcon(iconAddOff);
        addButton.setRolloverIcon(iconAddOver);
        addButton.setPressedIcon(iconAddOn);
        addButton.setContentAreaFilled(false);
        addButton.setSize(90, 25);
        addButton.setLocation(297, 19);
        addButton.addActionListener(new java.awt.event.ActionListener() {
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
        cancelButton.setLocation(297, 56);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });

        textInput.setOpaque(false);
        textInput.setBorder(null);
        textInput.setForeground(new Color(0, 128, 217));
        textInput.setFont(new Font("SansSerif", Font.BOLD, 12));
        textInput.setHorizontalAlignment(textInput.LEFT);
        textInput.setText("e.g. 195.100.216.67");
        textInput.setSize(176, 21);
        textInput.setLocation(97, 39);
        textInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        textInput.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(FocusEvent e) {
                textInput.setText("");
            }
        });

        layers.add(background, new Integer(0));
        layers.add(exitButton, new Integer(1));
        layers.add(addButton, new Integer(1));
        layers.add(cancelButton, new Integer(1));
        layers.add(textInput, new Integer(2));

        this.getContentPane().add(layers, BorderLayout.CENTER);
    }

    void okPressed() {
        Friend friend;
        String s;
        InetAddress address;

        s = textInput.getText();
        s.trim();

        try {
            address = InetAddress.getByName(s);
            friend = new Friend(address, Globals.NETWORK_PORT);

            // Check to see friend doesn't already exist...
            if (!friends.contains(friend)) {
                // Add this friend to the list...
                synchronized(friends) {
                    // If they can't be added, we say we couldn't find them...
                    if (!friends.add(friend)) {
                        AlertWindow.alert("Couldn't obtain a response from this address.");
                    }
                }
            }
        }
        catch(UnknownHostException error) {
            AlertWindow.alert("That address is invalid.  The address must be a valid IP.");
        }

        cancelPressed();
    }

    void cancelPressed() {
        super.setVisible(false);
        super.dispose();
    }

    public static void main(String[] args) {
        new ExpandWindow();
    }
}