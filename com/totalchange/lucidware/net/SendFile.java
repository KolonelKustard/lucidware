package com.totalchange.lucidware.net;

import java.io.*;
import java.net.*;
import java.sql.*;

import com.aftexsw.util.bzip.CBZip2OutputStream;

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

public class SendFile {
    public static void sendFile(Socket socket) {
        DataInputStream in = null;
        DataOutputStream out = null;
        DataOutputStream outCompressed = null;

        DataInputStream fileIn = null;
        File thisFile = null;
        int thisFileType = Globals.FILE_TYPE_INVALID;
        int thisFileCodec = Globals.USING_CODEC;
        long thisFileLength;

        long requestedID;
        boolean found;

        PreparedStatement pstat;
        ResultSet result;

        try {
            // Set up streams..
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // Get the requested ID from the input stream...
            requestedID = in.readLong();

            // Now find the file and double check it's OK to send...
            pstat = SearchResources.getConn().prepareStatement("SELECT * FROM filesystem WHERE fileid = ?");
            pstat.setLong(1, requestedID);
            result = pstat.executeQuery();

            // Check if the file is found and valid...
            found = false;
            if (result.next()) {
                thisFile = new File(result.getString("filename"));
                thisFileType = result.getInt("filetype");
                if ((thisFile.isFile()) && (thisFile.canRead())) {
                    found = true;
                }
            }
            result.close();
            pstat.close();

            // Send standard header...
            NetworkManager.sendStandardHeader(out, Globals.I_SEND_YOU);

            if (found) {
                // Create stream for reading from file...
                fileIn = new DataInputStream(new BufferedInputStream(new FileInputStream(thisFile)));

                // File is found and valid, so we send it!
                out.writeLong(requestedID);
                out.writeInt(thisFileType);
                out.writeUTF(thisFile.getName());
                out.writeLong(thisFile.length());
                out.writeInt(thisFileCodec);
                out.flush();

                // Now create the compressed stream depending on selected codec.
                switch(Globals.USING_CODEC) {
                    case(Globals.CODEC_BZIP) :
                        outCompressed = new DataOutputStream(new CBZip2OutputStream(out));
                        thisFileCodec = Globals.CODEC_BZIP;
                        break;

                    default :
                        outCompressed = new DataOutputStream(out);
                        thisFileCodec = Globals.CODEC_NONE;
                        break;
                }

                // Now run through sending entire file...
                thisFileLength = thisFile.length();
                for (long num = 0; num < thisFileLength; num++) {
                    outCompressed.write(fileIn.read());
                }

                // Ensure entire file has been sent before continuing.
                outCompressed.flush();
            }
            else {
                // File is not found or is not valid, so we abort stream...
                out.writeLong(requestedID);
                out.writeInt(Globals.FILE_TYPE_INVALID);
            }
        }
        catch(Exception e) {
            // Just quit if exception occurs...
            e.printStackTrace();
        }

        // Clean up...
        try {
            in.close();
        }
        catch(Exception e) {}

        try {
            outCompressed.close();
        }
        catch(Exception e) {}

        try {
            out.close();
        }
        catch(Exception e) {}

        try {
            fileIn.close();
        }
        catch(Exception e) {}

        in = null;
        out = null;
        outCompressed = null;

        fileIn = null;
        thisFile = null;

        pstat = null;
        result = null;
    }
}