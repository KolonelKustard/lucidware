package com.totalchange.lucidware.net;

import java.io.*;
import java.net.*;
import java.util.*;

import com.aftexsw.util.bzip.CBZip2InputStream;

import com.totalchange.lucidware.Globals;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class RemoteFile {
    private static long staticCachedFileID = 100;

    private Friend hostFriend;
    private long fileID;
    private int fileType;
    private String filename;
    private long fileSize;

    private ArrayList listeners;

    /**
     * This is the locally 'cached' version of the file.  If this is null then
     * the file has not been fetched yet.  So then we must grab that file...
     */
    private File file = null;

    /**
     * This is the cached file id of this file.  This number is a unique number
     * given to this file if and only if it's downloaded completely to local
     * disk...
     */
    private long cachedFileID;

    /**
     * This is a store of keywords relevant to this file.  Not necessarily used.
     */
    private ArrayList keywords;

    /**
     * Constructs an instance of a remote file.  All these details are needed
     * as parameters to ensure the file being received is indeed the correct
     * file.  Should be a bit of protection against malicious use...
     */
    public RemoteFile(Friend hostFriend, long fileID, int fileType, String filename, long fileSize) {
        this.hostFriend = hostFriend;
        this.fileID = fileID;
        this.fileType = fileType;
        this.filename = filename;
        this.fileSize = fileSize;

        cachedFileID = staticCachedFileID++;
        keywords = new ArrayList();
        listeners = new ArrayList();
    }

    /**
     * Determines if the object passed to it has the same values as this object.
     * Does this by checking from the same host friend and that the remote file
     * ID's are the same...
     */
    public boolean equals(Object obj) {
        if (obj instanceof RemoteFile) {
            RemoteFile remoteFile = (RemoteFile)obj;
            if ((remoteFile.getHostFriend().equals(hostFriend)) && (remoteFile.getFileID() == fileID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method downloads the file from the remote friend.
     */
    public void receiveFile() throws IOException, InterruptedIOException {
        Socket socket = null;
        DataInputStream in = null;
        DataInputStream inCompressed = null;
        DataOutputStream out = null;

        File tmpFile;
        long tmpCachedFileID;
        DataOutputStream fileOut = null;

        int percentComplete = 0;
        int newPercent;

        boolean correctFile;

        // Connect to remote friend...
        socket = new Socket(hostFriend.getAddress(), hostFriend.getPort());
        socket.setSoTimeout(Globals.NETWORK_TIMEOUT_PERIOD_LONG);

        // Create standard streams...
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        // Send standard header...
        NetworkManager.sendStandardHeader(out, Globals.PLEASE_SEND_ME);
        out.writeLong(fileID);

        // Should now receive header from remote friend...
        if (hostFriend.receiveStandardHeader(in, Globals.I_SEND_YOU)) {
            // Now check file is indeed the one we think it is...
            correctFile = false;

            if (in.readLong() == fileID) {
                if (in.readInt() == fileType) {
                    if (in.readUTF().equals(filename)) {
                        if (in.readLong() == fileSize) {
                            correctFile = true;
                        }
                    }
                }
            }

            if (correctFile) {
                // If it's the correct file, we can now create it locally...
                // The way this works is by using the cachedFileID as a new
                // directory name with this file as its content...
                tmpFile = new File(Globals.CACHE_DIR + Globals.SUB_CACHED_DIR_BEGINS + cachedFileID);
                tmpFile.mkdirs();

                tmpFile = new File(Globals.CACHE_DIR + Globals.SUB_CACHED_DIR_BEGINS + cachedFileID + "/" + filename);
                if (tmpFile.createNewFile()) {
                    fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)));

                    // Now determine compression type used in sending this file
                    // and create inputstream for reading file data...
                    switch(in.readInt()) {
                        case(Globals.CODEC_BZIP) :
                            System.out.println("Input stream using bzip");
                            inCompressed = new DataInputStream(new CBZip2InputStream(in));
                            break;

                        default :
                            System.out.println("Input stream using no compression");
                            inCompressed = new DataInputStream(in);
                            break;
                    }

                    // Now read file to disk...  If reading fails, delete cached
                    // file.
                    try {
                        for(long num = 0; num < fileSize; num++) {
                            fileOut.write(inCompressed.read());

                            // Calculate percentage completed.  If percentage
                            // changes, notify listeners...
                            newPercent = (int)(((double)num / (double)fileSize) * 100);
                            if (percentComplete != newPercent) {
                                percentComplete = newPercent;
                                for (int num2 = 0; num2 < listeners.size(); num2++) {
                                    ((RemoteFileListener)listeners.get(num2)).setPercentComplete(percentComplete);
                                }
                            }
                        }
                        for (int num2 = 0; num2 < listeners.size(); num2++) {
                            ((RemoteFileListener)listeners.get(num2)).setPercentComplete(100);
                        }
                        fileOut.close();

                        // If we got this far, then the file is complete and in
                        // the cache.  Woohoo!!!!
                        this.file = tmpFile;
                    }
                    catch(Exception e) {
                        try {
                            socket.close();
                        }
                        catch(Exception e2) {}
                        socket = null;

                        tmpFile.delete();

                        throw new IOException("File download failed");
                    }
                }
                else {
                    try {
                        socket.close();
                    }
                    catch(Exception e) {}
                    socket = null;

                    throw new IOException("Cached filename already exists");
                }
            }
            else {
                try {
                    socket.close();
                }
                catch(Exception e) {}
                socket = null;

                throw new IOException("Remote file does not match");
            }
        }
        else {
            try {
                socket.close();
            }
            catch(Exception e) {}
            socket = null;

            throw new IOException("Remote thing made incorrect response");
        }

        // Clean up...
        try {
            in.close();
        }
        catch(Exception e) {}

        try {
            inCompressed.close();
        }
        catch(Exception e) {}

        try {
            out.close();
        }
        catch(Exception e) {}

        try {
            fileOut.close();
        }
        catch(Exception e) {}

        try {
            socket.close();
        }
        catch(Exception e) {}

        socket = null;
        in = null;
        inCompressed = null;
        out = null;
        fileOut = null;
    }

    /**
     * Adds a keyword to be associated with this file.
     */
    public void addKeyword(String s) {
        if (!keywords.contains(s)) {
           keywords.add(s);
        }
    }

    /**
     * Determines if this file has already been retrieved from the remote
     * location...
     *
     * @return Returns true if the file is already cached locally.
     */
    public boolean isCached() {
        if (file == null) {
            return false;
        }
        else {
            try {
                if ((file.canRead()) && (file.isFile())) {
                    return true;
                }
                else {
                    return false;
                }
            }
            catch(Exception e) {
                return false;
            }
        }
    }

    /**
     * Copies this cached file to another location on disk.  Will adjust
     * filename if necessary by adding numbers until an available name occurs.
     * Should check that this file is cached already or not before trying to
     * copy it from the cache...
     * @param destDir The destination directory to copy this file to.
     * @return New reference to file in its non-cached location.
     */
    public File copyOutOfCache(File destDir) throws IOException {
        File newFile;
        int fileExtension = 0;

        newFile = new File(destDir, file.getName());

        // Test if we can use this filename or not.  If not then add some
        // numbers to it.
        while(!newFile.createNewFile()) {
            // If gone over 9 tries at same filename, then summink else is up
            // with this...
            if (fileExtension >= 10) {
                throw new IOException("Could not create new file (filenames already existed)");
            }

            fileExtension++;
            newFile = new File(destDir, file.getName() + "_" + fileExtension);
        }

        // Now copy contents of file in cache to destination file...
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));

        fileSize = file.length();
        for (long num = 0; num < fileSize; num++) {
            out.write(in.read());
        }

        out.flush();
        in.close();
        out.close();

        return newFile;
    }

    /**
     * Returns the locally cached file.  This method should always follow a test
     * to see if the file is held locally already.  If the file is not held
     * locally then a null pointer is returned.
     */
    public File getFile() {
        return file;
    }

    public long getFileID() {
        return fileID;
    }

    public int getFileType() {
        return fileType;
    }

    public Friend getHostFriend() {
        return hostFriend;
    }

    public void addListener(RemoteFileListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RemoteFileListener listener) {
        listeners.remove(listener);
    }
}