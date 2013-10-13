package com.totalchange.lucidware.ui;

import java.io.*;
import java.awt.*;
import javax.swing.*;

import ice.pilots.html4.*;
import ice.storm.*;

import com.totalchange.lucidware.Globals;
import java.awt.event.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class Browser extends JFrame {
    StormBase base;
    Viewport currentViewport;

    public Browser() {
        super("Results...");

        this.setSize(640, 480);
        this.setVisible(true);

        base = new StormBase();
        base.setCallback(new ThisCallback());

        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Browser(File HTMLFile) throws FileNotFoundException {
        this();
        setHTMLFile(HTMLFile);
    }

    public static void main(String[] args) throws Exception {
        Browser browser = new Browser();
        browser.setHTMLFile(new File("index.html"));
    }

    public void setHTMLFile(File HTMLFile) throws FileNotFoundException {
        if (HTMLFile.exists()) {
            base.renderContent("file:///" + HTMLFile.getAbsolutePath(), null, "ContentWindow");
        }
        else {
            throw new FileNotFoundException("Couldn't find HTML file...");
        }
    }

    public void reload() {
        base.getHistoryManager().reload(currentViewport.getName());
    }

    private class ThisCallback implements StormCallback {
        private StormBase callBase;

        public ThisCallback() {
        }

        public void init(StormBase callBase) {
            this.callBase = callBase;
        }

        public Container createTopLevelContainer(Viewport viewport) {
            currentViewport = viewport;
            return getContentPane();
        }

        public void disposeTopLevelContainer(Viewport viewport) {}

        public String prompt(Viewport vp, String msg, String defVal) {
            return null;
        }

        public boolean confirm(Viewport vp, String msg) {
            return true;
        }

        public void message(Viewport vp, String msg) {}
    }

    private void jbInit() throws Exception {
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
    }

    void this_windowClosing(WindowEvent e) {
        base.dispose();

        this.setVisible(false);
        this.dispose();

        base = null;
        currentViewport = null;

        // Force garbage collect in case Ice browser has nicked loads of
        // resources.
        System.gc();
    }
}