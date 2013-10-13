package com.totalchange.lucidware.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import java.util.*;

import com.totalchange.lucidware.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class SearchProgress extends TransparentWindow {
    private static final int HEADER_WIDTH = 200;
    private static final int HEADER_HEIGHT = 35;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 25;

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel progressPanel = new JPanel();

    JLayeredPane layers = new JLayeredPane();
    JLabel background = new JLabel();
    JButton cancelButton = new JButton();

    ImageIcon iconBack = new ImageIcon(Globals.IMAGE_ROOT + "progwindow.png");
    ImageIcon iconCancelOff = new ImageIcon(Globals.IMAGE_ROOT + "canceloff.png");
    ImageIcon iconCancelOver = new ImageIcon(Globals.IMAGE_ROOT + "cancelover.png");
    ImageIcon iconCancelOn = new ImageIcon(Globals.IMAGE_ROOT + "cancelon.png");

    private JProgressBar[] progressBars;
    GridLayout progressLayout = new GridLayout();

    private KeywordSearch parent;

    public SearchProgress(KeywordSearch parent) {
        this();
        this.parent = parent;
    }

    public SearchProgress() {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        progressBars = new JProgressBar[0];

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        super.setSize(WIDTH, HEADER_HEIGHT);
        super.setLocation((int)(screenSize.getWidth() - WIDTH), 0);
        super.setVisible(true);
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(borderLayout1);

        background.setIcon(iconBack);
        background.setSize(HEADER_WIDTH, HEADER_HEIGHT);

        cancelButton.setBorder(null);
        cancelButton.setOpaque(false);
        cancelButton.setIcon(iconCancelOff);
        cancelButton.setRolloverIcon(iconCancelOver);
        cancelButton.setPressedIcon(iconCancelOn);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setSize(90, 25);
        cancelButton.setLocation(55, 7);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelPressed();
            }
        });

        progressPanel.setOpaque(false);
        progressPanel.setLayout(progressLayout);
        progressLayout.setRows(1);
        progressPanel.setLocation(0, HEADER_HEIGHT);

        layers.add(background, new Integer(0));
        layers.add(cancelButton, new Integer(1));
        layers.add(progressPanel, new Integer(2));

        this.getContentPane().add(layers, BorderLayout.CENTER);
    }

    public void setDownload(int downloadNum, int percent) {
        // If download num is greater than length of array, increase size of
        // array...
        if ((downloadNum + 1) > progressBars.length) {
            JProgressBar[] tmpProgressBars = new JProgressBar[downloadNum + 1];
            System.arraycopy(progressBars, 0, tmpProgressBars, 0, progressBars.length);
            progressBars = tmpProgressBars;
        }

        // If this progress bar doesn't exist yet, then we need to add a new
        // progress bar...
        if (progressBars[downloadNum] == null) {
            progressBars[downloadNum] = getNewProgressBar();
            progressLayout.setRows(progressBars.length);
            progressPanel.add(progressBars[downloadNum], null);

            progressPanel.setSize(WIDTH, progressBars.length * HEIGHT);

            super.setSize(WIDTH, (progressBars.length * HEIGHT) + HEADER_HEIGHT);
        }

        // Now set the percentage of the progress bar to that we've just been
        // given.
        progressBars[downloadNum].setValue(percent);

        // If percentage is now 100%, set the string to "Complete", else set the
        // string to the percentage of completion...
        if (percent >= 100) {
            progressBars[downloadNum].setString("Completed");
        }
        else {
            progressBars[downloadNum].setString("" + percent + "%");
        }
    }

    private JProgressBar getNewProgressBar() {
        JProgressBar progressBar = new JProgressBar();

        progressBar.setOrientation(JProgressBar.HORIZONTAL);
        progressBar.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        progressBar.setBorder(null);
        progressBar.setOpaque(false);
        progressBar.setString("Please Wait...");
        progressBar.setForeground(new Color(255, 191, 128));
        progressBar.setBackground(new Color(0, 128, 217));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(true);

        return progressBar;
    }

    private void cancelPressed() {
        parent.shutdown();
    }
}