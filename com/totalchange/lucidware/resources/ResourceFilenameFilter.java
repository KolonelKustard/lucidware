package com.totalchange.lucidware.resources;

import java.io.*;
import java.sql.*;

import com.totalchange.lucidware.Globals;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 *
 * This class decides what to do with files...  It has methods called by the
 * SearchResources thread that maintain the database of files.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class ResourceFilenameFilter implements FilenameFilter {
    private String[] acceptedTypes = Globals.ACCEPTED_FILE_TYPES;
    private int[] acceptedTypesFilters = Globals.ACCEPTED_FILE_TYPES_FILTERS;

    public ResourceFilenameFilter() {
    }

    /**
     * This is the only inherited method from FilenameFilter, and is used by
     * the SearchResources thread to determine if a file should be listed or
     * not.
     */
    public boolean accept(File dir, String file) {
        if (new File(dir, file).isDirectory()) {
            return true;
        }

        for(int num = 0; num < acceptedTypes.length; num++) {
            if (file.endsWith("." + acceptedTypes[num])) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method runs through the known file types and decides what to do with
     * the file passed to it.
     */
    public void addFileToDatabase(File file) {
        int fileType = -1;

        for(int num = 0; num < acceptedTypes.length; num++) {
            if (file.getName().endsWith("." + acceptedTypes[num])) {
                fileType = acceptedTypesFilters[num];
                break;
            }
        }

        // If it's a valid file type, we'll decide what to do with it through
        // particular routines...
        switch(fileType) {
            case(Globals.FILE_TYPE_TEXT) :
                addTextFile(file);
                break;

            case(Globals.FILE_TYPE_IMAGE) :
                addImageFile(file);
                break;
        }
    }

    /**
     * Returns a keyword using the filename passed to it.
     */
    private String getKeyword(File file) {
        String s = "";

        for(int num = 0; num < file.getName().length(); num++) {
            if (file.getName().charAt(num) != '.') {
                s += file.getName().charAt(num);
            }
            else {
                break;
            }
        }

        s.toLowerCase();
        return s;
    }

    /**
     * Parses a text type document and picks out keywords from it...
     */
    private void addTextFile(File file) {
        PreparedStatement pStat = null;
        String filenameKeyword;
        String[] innerKeywords;
        long thisFileID;

        filenameKeyword = getKeyword(file);
        thisFileID = SearchResources.getNextID();

        // Execute updates to the database...
        try {
            pStat = SearchResources.getConn().prepareStatement("INSERT INTO filesystem(fileid, filename, filetype) VALUES(?, ?, ?)");
            pStat.setLong(1, thisFileID);
            pStat.setString(2, file.getAbsolutePath());
            pStat.setInt(3, Globals.FILE_TYPE_TEXT);
            pStat.execute();
            pStat.close();

            pStat = SearchResources.getConn().prepareStatement("INSERT INTO filekeywords(keyword, fileid) VALUES(?, ?)");
            pStat.setString(1, filenameKeyword);
            pStat.setLong(2, thisFileID);
            pStat.execute();
            pStat.close();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        // Now calculate more keywords and add them too...
        innerKeywords = TextFileParser.getKeywords(file);

        for(int num = 0; num < innerKeywords.length; num++) {
            try {
                pStat = SearchResources.getConn().prepareStatement("INSERT INTO filekeywords(keyword, fileid) VALUES(?, ?)");
                pStat.setString(1, innerKeywords[num]);
                pStat.setLong(2, thisFileID);
                pStat.execute();
                pStat.close();
            }
            catch(SQLException e) {
                e.printStackTrace();
            }
        }

        // Clean up...
        filenameKeyword = null;
        pStat = null;
    }

    /**
     * Uses the image files filename as keyword(s)...
     */
    private void addImageFile(File file) {
        PreparedStatement pStat = null;
        String filenameKeyword;
        long thisFileID;

        filenameKeyword = getKeyword(file);
        thisFileID = SearchResources.getNextID();

        // Execute updates to the database...
        try {
            pStat = SearchResources.getConn().prepareStatement("INSERT INTO filesystem(fileid, filename, filetype) VALUES(?, ?, ?)");
            pStat.setLong(1, thisFileID);
            pStat.setString(2, file.getAbsolutePath());
            pStat.setInt(3, Globals.FILE_TYPE_IMAGE);
            pStat.execute();
            pStat.close();

            pStat = SearchResources.getConn().prepareStatement("INSERT INTO filekeywords(keyword, fileid) VALUES(?, ?)");
            pStat.setString(1, filenameKeyword);
            pStat.setLong(2, thisFileID);
            pStat.execute();
            pStat.close();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }

        // Clean up...
        filenameKeyword = null;
        pStat = null;
    }
}