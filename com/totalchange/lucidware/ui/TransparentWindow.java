package com.totalchange.lucidware.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Title:        Final Year Project
 * Description:  A thing what makes transparent JWindow's.  Not at all portable
 *               outside of this application due to lots of hassles...  But feel
 *               free to try!!!
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class TransparentWindow extends JWindow {
    private static final int MOVE_UPDATE_PERIOD = 50;

    private static Robot robot;

    private Rectangle rect = new Rectangle();
    private BufferedImage image;
    private JLayeredPane layeredPane = new JLayeredPane();
    private JPanel newContentPanel = new JPanel();
    private ImageIcon imageIcon = new ImageIcon();
    private JLabel backImage = new JLabel();

    private int offsetX = 0, offsetY = 0;
    private long lastTime = System.currentTimeMillis();

    public TransparentWindow() {
        try {
            robot = new Robot();
        }
        catch(Exception e) {}

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        changeBackground();
    }

    public TransparentWindow(Window owner) {
        super(owner);

        try {
            robot = new Robot();
        }
        catch(Exception e) {}

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        changeBackground();
    }

    private void jbInit() throws Exception {
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                this_mouseDragged(e);
            }
        });
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                changeBackground();
            }
            public void mousePressed(MouseEvent e) {
                this_mousePressed(e);
            }
        });

        newContentPanel.setOpaque(false);

        layeredPane.add(backImage, new Integer(0));
        layeredPane.add(newContentPanel, new Integer(1));

        super.getContentPane().add(layeredPane);
    }

    private void changeBackground() {
        super.setVisible(false);

        // Has to sleep for a sec after hiding window and before doing screen
        // grab.  This is because I was finding it was grabbing remnants of the
        // window rather than what was behind it...
        try {
            Thread.currentThread().sleep(100);
        }
        catch(Exception e) {}

        try {
            rect.setSize(super.getSize());
            rect.setLocation(super.getLocation());
            image = robot.createScreenCapture(rect);

            imageIcon.setImage(image);
            backImage.setIcon(imageIcon);
            backImage.setSize(image.getWidth(), image.getHeight());
            newContentPanel.setSize(image.getWidth(), image.getHeight());
        }
        catch(Exception e) {}

        super.setVisible(true);

        // Added this toFront() because of losing focus when setting invisible
        // and visible again...
        super.toFront();
    }

    public void setSize(int x, int y) {
        super.setSize(x, y);
        changeBackground();
    }

    public void setLocation(int x, int y) {
        super.setLocation(x, y);
        try {
            changeBackground();
        }
        catch(Exception e) {}
    }

    public void setVisible(boolean b) {
        super.setVisible(b);

        if (b) {
            changeBackground();
        }
    }

    public Container getContentPane() {
        return newContentPanel;
    }

    /**
     * Gets the mouse offset before any dragging goes on.
     */
    void this_mousePressed(MouseEvent e) {
        offsetX = e.getX();
        offsetY = e.getY();
    }

    /**
     * Moves the window about when the mouse is dragging it...
     */
    void this_mouseDragged(MouseEvent e) {
        if (System.currentTimeMillis() >= (lastTime + MOVE_UPDATE_PERIOD)) {
           super.setLocation((int)super.getLocation().getX() + (e.getX() - offsetX), (int)super.getLocation().getY() + (e.getY() - offsetY));
           lastTime = System.currentTimeMillis();
        }
    }

    /**
     * A little test to make sure everything looks pretty.
     */
    public static void main(String[] args) {
        TransparentWindow window = new TransparentWindow();
        window.setSize(300, 300);
        window.setVisible(true);
    }
}