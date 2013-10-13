package com.totalchange.lucidware.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

import com.totalchange.lucidware.Globals;
import com.totalchange.lucidware.resources.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class FindFiles {
    public static void findFiles(Socket socket) {
        DataInputStream in = null;
        DataOutputStream out = null;
        ResultSet result;
        PreparedStatement pstat;

        String keyword;

        FileDetailed thisFile;
        ArrayList matchedFiles = new ArrayList();

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Get keyword to look for...
            keyword = in.readUTF();

            // Execute query for this keyword...
            pstat = SearchResources.getConn().prepareStatement("SELECT filesystem.* FROM filesystem INNER JOIN filekeywords ON filesystem.fileid = filekeywords.fileid WHERE filekeywords.keyword LIKE ?");
            pstat.setString(1, "%" + keyword + "%");
            result = pstat.executeQuery();

            // Now check all results still exist and are valid.  Then make a
            // list of files and release the database resources.
            while(result.next()) {
                thisFile = new FileDetailed(new File(result.getString("filesystem.filename")));
                thisFile.fileID = result.getLong("filesystem.fileid");
                thisFile.fileType = result.getInt("filesystem.filetype");

                if ((thisFile.file.isFile()) && (thisFile.file.canRead())) {
                    matchedFiles.add(thisFile);
                }
            }
            result.close();
            pstat.close();

            // Will send header before each individual files details...
            NetworkManager.sendStandardHeader(out, Globals.DO_I_HAVE);
            out.writeInt(matchedFiles.size());

            // Will now send each individual file's details...  See text file
            // detailing transfer methods to see structure details...
            for(int num = 0; num < matchedFiles.size(); num++) {
                thisFile = (FileDetailed)matchedFiles.get(num);
                out.writeLong(thisFile.fileID);
                out.writeInt(thisFile.fileType);
                out.writeUTF(thisFile.file.getName());
                out.writeLong(thisFile.file.length());
            }
        }
        catch(Exception e) {
            // No error catching at the mo.  Just do cleanup...
            e.printStackTrace();
        }

        // Clean up...

        try {
            in.close();
        }
        catch(Exception e) {}

        try {
            out.close();
        }
        catch(Exception e) {}

        matchedFiles.clear();
        matchedFiles = null;
        thisFile = null;
        result = null;
        pstat = null;
        out = null;
    }
}

/**
 * Utility struct to hold details about a file...
 */
class FileDetailed {
    public File file;
    public long fileID;
    public int fileType;

    public FileDetailed(File file) {
        this.file = file;
    }
}