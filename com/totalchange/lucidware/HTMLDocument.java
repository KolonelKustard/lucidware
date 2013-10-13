package com.totalchange.lucidware;

import java.io.*;
import java.util.*;

import com.totalchange.lucidware.net.*;

/**
 * Title:        Final Year Project
 * Description:  Constructs an HTML Document from the given text + images.
 *               Nothing is constructed and built until all the parseIt() method
 *               is called.  At this point the HTML is constructed and saved in
 *               the file specified by the constructor.
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class HTMLDocument {
    private static final String[] ARGH = {"<", ">", "  ", "\n"};
    private static final String[] FECK = {"&lt;", "&gt;", "&nbsp; ", "<br>\n"};

    private String[] keywords;

    private File thisDocFile;
    private File resourceDir;
    private boolean fileClosed = false;

    private ArrayList textFiles;
    private ArrayList imageFiles;

    /**
     * Pass the constructor the root for the output HTML file.  e.g.:
     * Globals.OUTPUT_DIR + "thisone/" + "index.html"
     */
    public HTMLDocument(File HTMLFile, String[] keywords) throws IOException {
        thisDocFile = HTMLFile;
        this.keywords = keywords;

        textFiles = new ArrayList();
        imageFiles = new ArrayList();

        // Try and create directory if it doesn't already exist...
        if (!thisDocFile.getParentFile().exists()) {
            thisDocFile.getParentFile().mkdirs();
        }

        // Try and create file if it isn't already there...
        if (!thisDocFile.exists()) {
            thisDocFile.createNewFile();
        }

        // Add resources sub-directory
        resourceDir = new File(thisDocFile.getParent() + "/" + Globals.OUTPUT_RESOURCE_DIR);
        resourceDir.mkdirs();
    }

    /**
     * This method determines the remote file type and copies it from cache to
     * the correct location, then uses it in creating this HTML Document.  In
     * the case of a text file, the text will remain in its cache.  An image
     * will however be copied from cache to the detination location.
     */
    public void addRemoteFile(RemoteFile remoteFile) {
        File fileToAdd;

        if (remoteFile.isCached()) {
            switch(remoteFile.getFileType()) {
                case(Globals.FILE_TYPE_TEXT) :
                    addTextFile(remoteFile.getFile());
                    break;

                case(Globals.FILE_TYPE_IMAGE) :
                    try {
                        fileToAdd = remoteFile.copyOutOfCache(resourceDir);
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }

                    addImageFile(remoteFile.getFile());
                    break;
            }
        }
    }

    /**
     * Adds plain text as paragraphs to the HTML document...
     *
     * @param
     */
    public void addTextFile(File file) {
        textFiles.add(file);
    }

    /**
     * Adds an image to the list of image files useable.
     */
    public void addImageFile(File file) {
        imageFiles.add(file);
    }

    /**
     * Adds a movie.  Not yet implemented...
     */
    public void addMovieFile(File file) {
    }

    /**
     * Constructs and saves the HTML.
     */
    public void parseIt() throws IOException {
        StringBuffer html = new StringBuffer();
        StringBuffer thisTextFileBuffer;
        String thisTextFile;

        String imageTag;
        boolean imageLeft = false;

        BufferedInputStream in;
        DataOutputStream out;
        byte[] output;
        long fileLength;

        boolean moreElements;
        int incrementor = 0;

        html.append("<html><head>\n");
        html.append("<title>" + keywordsToString() + "</title>\n");
        html.append("</head>\n");
        html.append("<body bgcolor=\"#FFFFFF\">\n");
        html.append("<h1>" + keywordsToString() + "</h1>\n");
        html.append("<div align=\"center\">\n");

        do {
            // Must be proven to have no more elements...
            moreElements = false;

            html.append("<table width=\"90%\" cellspacing=\"0\" cellpadding=\"10\">\n");
            html.append("<tr><td>");

            if (imageFiles.size() > incrementor) {
                // Add an image...
                // Construct image tag...
                imageTag = "";
                imageTag += "<img src=\"";
                imageTag += Globals.OUTPUT_RESOURCE_DIR + ((File)imageFiles.get(incrementor)).getName();
                imageTag += "\" border=\"0\" align=\"";
                if (imageLeft) {
                    imageTag += "left";
                    imageLeft = !imageLeft;
                }
                else {
                    imageTag += "right";
                    imageLeft = !imageLeft;
                }
                imageTag += "\">";

                html.append(imageTag);

                moreElements = true;
            }

            if (textFiles.size() > incrementor) {
                // Add some text...
                thisTextFile = "";
                in = new BufferedInputStream(new FileInputStream((File)textFiles.get(incrementor)));

                fileLength = ((File)textFiles.get(incrementor)).length();
                thisTextFileBuffer = new StringBuffer();

                for (long num2 = 0; num2 < fileLength; num2++) {
                    thisTextFileBuffer.append((char)in.read());
                }

                thisTextFile = thisTextFileBuffer.toString();
                thisTextFile = parseText(thisTextFile);

                html.append("<p>" + thisTextFile + "</p>\n");

                moreElements = true;
            }

            // Polish off table...
            html.append("</td></tr></table>\n");

            incrementor++;
        } while(moreElements);

        // Now finish off the HTML...
        html.append("</div>\n</body>\n</html>\n");

        // Now output to file...
        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(thisDocFile)));

        output = html.toString().getBytes();

        for (int num = 0; num < output.length; num++) {
            out.write(output[num]);
        }

        out.flush();
        out.close();
    }

    /**
     * Not useful now using the IceStorm browser...  Was only applicable when
     * using native browser and couldn't force reload.
     */
    public void closeIt() throws IOException {
        fileClosed = true;
        //parseIt();
    }

    private String keywordsToString() {
        String keywordsString = "";

        for (int num = 0; num < keywords.length; num++) {
            keywordsString += keywords[num] + " ";
        }

        return keywordsString;
    }

    private static String parseText(String s) {
        StringBuffer messyS = new StringBuffer(s);
        boolean carryOn;
        int thisCurrentPos;
        int thisStartPos;

        for(int num = 0; num < ARGH.length; num++) {
            carryOn = true;
            thisCurrentPos = 0;

            while(carryOn) {
                thisStartPos = messyS.toString().indexOf(ARGH[num], thisCurrentPos);

                if (thisStartPos >= 0) {
                    messyS.replace(thisStartPos, thisStartPos + ARGH[num].length(), FECK[num]);
                    thisCurrentPos = thisStartPos + FECK[num].length();
                }
                else {
                    carryOn = false;
                }
            }
        }

        return messyS.toString();
    }

    public static void main(String[] args) throws Exception {
        String s = "Hello my chum.\n  I loves ya you know,  you hunky chunky<great> big bear you...";
        System.out.println(HTMLDocument.parseText(s));
    }
}