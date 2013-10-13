package com.totalchange.lucidware;

import java.io.*;
import java.util.*;

import java.awt.Dimension;
import javax.swing.*;

import com.totalchange.lucidware.net.*;
import com.totalchange.lucidware.ui.*;

/**
 * Title:        Final Year Project
 * Description:  This thread is responsible for finding all the keywords of a
 *               search and providing results...
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class KeywordSearch extends Thread {
    private static long staticSearchNum = 1;

    private String[] keywords;
    private long searchNum;

    private ArrayList friends;
    private ArrayList remoteFiles;
    private ArrayList downloadedFiles;
    private ArrayList downloadingFiles;
    private int numDownloadingFiles = 0;

    private boolean stillGoing;

    /**
     * This ArrayList is used to store the ID's of friends who have already been
     * asked for their list of files...  Means they won't get asked again.
     */
    private ArrayList alreadyTriedFriends;

    private SearchProgress progressWindow;
    private JFileChooser chooser;
    private Browser browserWindow;
    private File outputFile;
    private HTMLDocument document;

    boolean windowOpened = false;

    /**
     * Constructs and starts a thread to find files that relate to the specified
     * keyword...
     */
    public KeywordSearch(String searchText, ArrayList friends) throws Exception {
        this.friends = friends;
        remoteFiles = new ArrayList();
        downloadedFiles = new ArrayList();
        downloadingFiles = new ArrayList();

        ArrayList keywordsList = new ArrayList();
        String s = "";

        searchNum = staticSearchNum++;

        searchText.trim();
        searchText.toLowerCase();

        if (!searchText.equals("")) {
            for(int num = 0; num < searchText.length(); num++) {
                if (searchText.charAt(num) != ' ') {
                    s += searchText.charAt(num);
                }
                else {
                    keywordsList.add(s);
                    s = "";
                }
            }
            keywordsList.add(s);
        }

        keywords = new String[keywordsList.size()];
        for(int num = 0; num < keywordsList.size(); num++) {
            keywords[num] = (String)keywordsList.get(num);
        }

        if (keywords.length < 1) {
            throw new Exception("No keywords found.");
        }
        else {
            alreadyTriedFriends = new ArrayList();

            // Open file save to dialog...
            chooser = new JFileChooser();
            chooser.setFileFilter(new SaveAsFileFilter());
            chooser.setDialogTitle("Save...");
            chooser.setApproveButtonText("Save...");

            // Position file save to dialog in middle of screen...
            Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            int xPos = (screenSize.width / 2) - (chooser.getSize().width / 2);
            int yPos = (screenSize.height / 2) - (chooser.getSize().height / 2);
            chooser.setLocation(xPos, yPos);

            int returnVal = chooser.showSaveDialog(progressWindow);

            if (returnVal == chooser.APPROVE_OPTION) {
                outputFile = chooser.getSelectedFile();

                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                    outputFile = new File(outputFile, "index.html");
                    this.start();
                }
                else if (QuestionWindow.question("Saving to this location may overwrite previous files.  Is this OK?")) {
                    outputFile = new File(outputFile, "index.html");
                    this.start();
                }
            }
        }
    }

    // Does the business!!!  Goes off and finds a list of files and grabs them.
    public void run() {
        FindFilesThread[] finders;
        boolean waiter;
        boolean findersStillActive;

        Object[] tmpRemoteFiles;
        int[] tmpRandomIndexer;
        RemoteFile tmpRemoteFile;

        // Create, locate, and show results window...
        browserWindow = new Browser();

        // Create and show progress window...
        progressWindow = new SearchProgress(this);

        // Initialise output HTML Document...
        try {
            document = new HTMLDocument(outputFile, keywords);
        }
        catch(IOException e) {
            AlertWindow.alert("Trouble initialising output.  Results may contain errors.");
        }

        finders = new FindFilesThread[Globals.NUM_OF_SEARCH_THREADS];
        for(int num = 0; num < finders.length; num++) {
            finders[num] = new FindFilesThread(keywords, this);
        }

        // First while loop here deals with getting hold of the files needed.
        // When files are found, this loop ends, and we go to the next bit that
        // deals with constructing stuff out of retrieved files...
        stillGoing = true;
        while(stillGoing) {

            // Check to see if we're still looking for files...
            findersStillActive = false;
            for(int num = 0; num < finders.length; num++) {
                if (finders[num].isAlive()) findersStillActive = true;
            }

            // Now see if we need to quit...

            // If there are no remote files to report and the finders have
            // given up, then we need to quit this loop...
            if ((remoteFiles.size() < 1) && (!findersStillActive)) {
                stillGoing = false;
                System.out.println("No files matched");
            }

            // If the number of downloaded files has reached our limit, then
            // we quit this download cycle...
            if (downloadedFiles.size() >= Globals.NUM_OF_FILES_PER_SEARCH) {
                stillGoing = false;
                System.out.println("Reached max limit of downloads set in Globals class");
            }

            // If the number of downloaded files is equal to the number of
            // remote files and the finders have stopped, then we have all the
            // files we can have.
            if ((!findersStillActive) && (downloadedFiles.size() >= remoteFiles.size())) {
                stillGoing = false;
                System.out.println("All files exhausted...");
            }

            // If after all that we still haven't quit then we go on to check
            // if there's anything else we should do whilst we wait...
            if (stillGoing) {

                // Now check if there are already maximum number of downloads in
                // progress and also check that by starting a new download,
                // there won't be too many files if we start a new download.
                if ((numDownloadingFiles < Globals.NUM_OF_DOWNLOAD_THREADS) && ((downloadedFiles.size() + numDownloadingFiles) < Globals.NUM_OF_FILES_PER_SEARCH)) {
                    // Now we'll try and download a file...  We'll use good ol'
                    // random stuff to try and find a new file to grab...
                    tmpRemoteFiles = remoteFiles.toArray();
                    tmpRandomIndexer = getRandomSequence(tmpRemoteFiles.length);

                    for(int num = 0; num < tmpRemoteFiles.length; num++) {

                        // Check if the randomly fetched file is already
                        // downloaded or is being downloaded...
                        if ((!downloadedFiles.contains(tmpRemoteFiles[tmpRandomIndexer[num]])) &&
                            (!downloadingFiles.contains(tmpRemoteFiles[tmpRandomIndexer[num]]))) {

                            // Start downloading this file...
                            new FileDownloadingThread((RemoteFile)tmpRemoteFiles[tmpRandomIndexer[num]], this);

                            // Now break out the loop to prevent all files
                            // downloading at once...
                            break;
                        }
                    }
                }

                synchronized(this) {
                    try {
                        this.wait(20000);
                    }
                    catch(Exception e) {}
                }
            }
        }

        // Now we have all the files we are gonna get, so we have to close down
        // the HTML Document...
        try {
            document.closeIt();

            document = null;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        progressWindow.setVisible(false);
        progressWindow.dispose();
        progressWindow = null;

        if (downloadedFiles.size() <= 0) {
            AlertWindow.alert("Insufficient resources to create from...");
        }

        System.out.println("KeywordSearch thread dies...");

        // Clean up...
        finders = null;
        remoteFiles = null;
        downloadedFiles = null;
        downloadingFiles = null;
        tmpRemoteFiles = null;

        // Force a garbage collection to make sure all threads are destroyed...
        System.gc();
    }

    /**
     * Utility method to produce an easy-ish way to randomly seek through an
     * array without getting the same element twice...
     */
    private int[] getRandomSequence(int length) {
        int[] randomOrdering = new int[length];
        int randomNum;

        // Initialise random order array...
        for(int num = 0; num < randomOrdering.length; num++) {
            randomOrdering[num] = -1;
        }

        // Construct a random order to run through the array of friends...
        for(int num = 0; num < randomOrdering.length; num++) {
            do {
                randomNum = (int)(Math.random() * randomOrdering.length);
            } while (randomOrdering[randomNum] != -1);

            randomOrdering[randomNum] = num;
        }

        return randomOrdering;
    }

    /**
     * This method is used by FindFilesThreads to get the next random friend to
     * ask for files relevant to the keywords being searched for...
     */
    synchronized Friend nextFriend() {
        Object[] friendsArray;
        int[] randomOrdering;

        friendsArray = friends.toArray();
        randomOrdering = getRandomSequence(friendsArray.length);

        // Now grab next not already used Friend...
        for(int num = 0; num < friendsArray.length; num++) {
            if (!alreadyTriedFriends.contains(friendsArray[randomOrdering[num]])) {
                alreadyTriedFriends.add(friendsArray[randomOrdering[num]]);
                return (Friend)friendsArray[randomOrdering[num]];
            }
        }

        // If all friends have already been asked, then return null...
        return null;
    }

    synchronized void addNewFile(RemoteFile remoteFile) {
        remoteFiles.add(remoteFile);

        // Now notify the thread part to tell it something has changed...
        this.notify();
    }

    synchronized void checkOutFile(RemoteFile remoteFile) {
        downloadingFiles.add(remoteFile);
    }

    synchronized void checkInFile(RemoteFile remoteFile) {
        downloadingFiles.remove(remoteFile);
        downloadedFiles.add(remoteFile);

        // Re-parse the document and update the browser window...
        try {
            document.addRemoteFile(remoteFile);
            document.parseIt();

            if (!windowOpened) {
                browserWindow.setHTMLFile(outputFile);
                windowOpened = true;
            }
            else {
                browserWindow.reload();
            }
        }
        catch(Exception e) {}
    }

    synchronized void addDownloader() {
        numDownloadingFiles++;
    }

    synchronized void removeDownloader() {
        numDownloadingFiles--;
    }

    SearchProgress getProgressWindow() {
        return progressWindow;
    }

    public void shutdown() {
        stillGoing = false;
        progressWindow.setVisible(false);
        progressWindow.dispose();
    }
}



/**
* This thread is used by the KeywordSearch to request lists of files
* from various friends simultaneously.  The KeywordSearch waits for
* notification of found files from the FindFilesThreads, and then begins to
* download those files.
*/
class FindFilesThread extends Thread {
    private boolean stillFinding;

    private String[] keywords;
    private KeywordSearch parent;

    public FindFilesThread(String[] keywords, KeywordSearch parent) {
        this.keywords = keywords;
        this.parent = parent;

        this.start();
    }

    public void run() {
        Friend thisFriend;
        RemoteFile[] remoteFiles;

        stillFinding = true;
        while(stillFinding) {
            thisFriend = parent.nextFriend();

            // If we get a null back it means we've run out of friends to try...
            if (thisFriend == null) {
                stillFinding = false;
            }
            else {
                try {
                    // Ask this friend for all keywords...
                    for(int num = 0; num < keywords.length; num++) {
                        System.out.println("Asking: " + thisFriend.getAddress());
                        remoteFiles = thisFriend.getFilesFromKeyword(keywords[num]);

                        // Now add them to this here list used by the parent
                        // KeywordSearch...
                        for(int num2 = 0; num2 < remoteFiles.length; num2++) {
                            parent.addNewFile(remoteFiles[num2]);
                        }
                    }
                }
                catch(Exception e) {
                    // If Friend fails on request, we'll skip em.  Might add
                    // this as a todo to retry and then maybe remove from the
                    // list of friends...
                }
            }
        }

        // Notify parent thread that this has died...
        synchronized(parent) {
            try {
                parent.notify();
            }
            catch(Exception e) {}
        }

        // This thread now dies a peaceful death...
        System.out.println("FindFilesThread dies...");
    }

    public void shutdown() {
        stillFinding = false;
    }
}


/**
 * This thread attempts to download a remoteFile from a friend.  If it is
 * successful then the remoteFile is given back to the parent.
 */
class FileDownloadingThread extends Thread {
    /**
     * Used to own a progress meter on the progress window
     */
    private static int staticDownloadNum = 0;
    private int downloadNum = staticDownloadNum++;

    private RemoteFile remoteFile;
    private RemoteFileListener listener;
    private KeywordSearch parent;

    public FileDownloadingThread(RemoteFile remoteFile, KeywordSearch parent) {
        this.remoteFile = remoteFile;
        this.parent = parent;

        // Tell parent there's a new download thread...
        parent.addDownloader();

        // Check out the file that's been handed to this thread...
        parent.checkOutFile(remoteFile);

        // Set up listener to callback with...
        listener = new RemoteFileListener() {
            public void setPercentComplete(int percent) {
                setPercent(percent);
            }
        };

        System.out.println("File downloader started: " + remoteFile.getFileID());
        this.start();
    }

    public void run() {
        boolean retry = true;
        int numRetries = 0;

        synchronized(parent) {
            try {
                parent.notify();
            }
            catch(Exception e) {}
        }

        while(retry) {
            try {
                if (!remoteFile.isCached()) {
                    System.out.println("Downloading file...");
                    remoteFile.addListener(listener);
                    remoteFile.receiveFile();
                    remoteFile.removeListener(listener);
                }
                else {
                    System.out.println("File was already cached...");
                }

                // Now we know the file is held locally, we can check it back
                // in to the parent object.
                parent.checkInFile(remoteFile);
                retry = false;
            }
            catch(Exception e) {
                e.printStackTrace();
                if (numRetries >= Globals.NETWORK_RETRIES) {
                    retry = false;
                }
                else {
                    numRetries++;
                    try {
                        this.sleep(5000);
                    }
                    catch(Exception e2) {}
                }
            }
        }

        // Clean up...

        // Tell parent that finished...
        parent.removeDownloader();

        // Notify parent that it need to check its stuff again...
        synchronized(parent) {
            try {
                parent.notify();
            }
            catch(Exception e) {}
        }

        System.out.println("FileDownloadingThread dies...");
    }

    /**
     * A callback on the remote file to listen for how complete the file
     * download is...
     */
    public void setPercent(int percent) {
        parent.getProgressWindow().setDownload(downloadNum, percent);
    }
}

/**
 * An implementation of the FileFilter abstract class to be used when deciding
 * where to save this document to...
 */
class SaveAsFileFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File file) {
        if ((file.isDirectory()) && (!file.isHidden())) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getDescription() {
        return "Lucia Results";
    }
}