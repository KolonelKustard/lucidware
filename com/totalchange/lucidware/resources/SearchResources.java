package com.totalchange.lucidware.resources;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.totalchange.lucidware.Globals;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 *
 * Search Resources is a thread that initiates at the start of the program and
 * creates a database table representing the contents of this users local disk.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class SearchResources extends Thread {
    private static Connection conn;
    private static long nextID = 0;

    private ResourceFilenameFilter fileFilter = new ResourceFilenameFilter();

    public SearchResources() throws ClassNotFoundException, SQLException {
        Class.forName(Globals.DB_INTERFACE);
        conn = DriverManager.getConnection(Globals.DB_URL, Globals.DB_USERNAME, Globals.DB_PASSWORD);

        this.setPriority(Globals.RESOURCE_COLLECTOR_PRIORITY);

        this.start();
    }

    public void run() {
        ArrayList fileList = new ArrayList();
        Statement stat = null;

        // First we'll get rid of any table that already exists...
        try {
            stat = conn.createStatement();
            stat.execute("DROP TABLE filesystem");
            stat.close();
        }
        catch(SQLException e) {}

        try {
            stat = conn.createStatement();
            stat.execute("DROP TABLE filekeywords");
            stat.close();
        }
        catch(SQLException e) {}

        // Then reconstruct the table as an empty table...
        try {
            stat = conn.createStatement();
            stat.execute("CREATE TABLE filesystem(fileid LONG PRIMARY KEY, filename VARCHAR(255), filetype INTEGER)");
            stat.execute("CREATE TABLE filekeywords(keyword VARCHAR(255), fileid LONG)");
            stat.close();
        }
        catch(SQLException e) {e.printStackTrace();}

        // Now we'll construct the filesystem table by parsing the current
        // filesystem and keeping track of files we like...
        recurseDirectories(new File(Globals.RESOURCE_ROOT), fileList);

        System.out.println("Found " + fileList.size() + " compatible files...  Parsing...");

        for(int num = 0; num < fileList.size(); num++) {
            fileFilter.addFileToDatabase((File)fileList.get(num));
        }

        System.out.println("All files parsed and added to database.");

        // Clean up...
        try {
            stat.close();
        }
        catch(Exception e) {}

        stat = null;
        System.gc();
    }

    private void recurseDirectories(File thisDir, ArrayList fileList) {
        File thisFile;

        String[] thisStructure = thisDir.list(fileFilter);

        try {
            for(int num = 0; num < thisStructure.length; num++) {
                thisFile = new File(thisDir, thisStructure[num]);

                // If this is a directory, then recurse further down the tree...
                if ((thisFile.isDirectory()) && (thisFile.canRead()) && (!thisFile.isHidden())) {
                    recurseDirectories(thisFile, fileList);
                }

                // If this is a valid file, then add it to the database according
                // to the routines held in ResourceFilenameFilter.
                if ((thisFile.isFile()) && (thisFile.canRead()) && (!thisFile.isHidden())) {
                    fileList.add(thisFile);
                }
            }
        }
        catch(Exception e) {
            // Exception could be thrown if there's an error with the
            // filesystem.  In this case, we'll just ignore it and carry on with
            // the rest...
        }
    }

    public static Connection getConn() {
        return conn;
    }

    public static long getNextID() {
        return nextID++;
    }

    public static void shutdown() {
        try {
            conn.close();
        }
        catch(Exception e) {}
    }
}