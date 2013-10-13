package com.totalchange.lucidware.net;

import java.io.*;
import java.net.*;
import java.util.*;

import com.totalchange.lucidware.Globals;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 *
 * A class that represents a friend at a remote location.  Contains methods
 * that represent requests to the remote client.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class Friend {
    private InetAddress address;
    private int port;
    private String name;

    private long timeCreated;

    private ArrayList remoteFiles;

    public Friend(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.name = "Roger";

        timeCreated = System.currentTimeMillis();

        remoteFiles = new ArrayList();
    }

    public Friend(InetAddress address, int port, String name) {
        this(address, port);

        this.name = name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    /**
     * Determines if the object passed to it has the same values as this object.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Friend) {
            Friend friend = (Friend)obj;
            if((friend.getAddress().equals(address)) && (friend.getPort() == port)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Receives the standard stream header from this friend which is used to
     * generate this friends details.  Format can be seen in
     * NetworkManager.sendStandardHeader();
     *
     * If the header received matches the type specified by receiveType, then
     * this method return true.
     *
     * This method will advance the stream to the start of the relevant receive
     * type...
     */
    public boolean receiveStandardHeader(DataInputStream in, int receiveType) throws IOException {
        if (in.readInt() == Globals.PRIMARY_INT) {
            this.name = in.readUTF();
            this.port = in.readInt();

            if (in.read() == receiveType) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets list of friends held by this remote friend.
     *
     * @return Array of friends held remotely
     */
    public Friend[] getFriends() throws IOException {
        Friend[] friendsArray;
        boolean stillReceiving;

        Socket socket = new Socket(address, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        // Set socket to timeout on a read from an input stream...
        socket.setSoTimeout(Globals.NETWORK_TIMEOUT_PERIOD);

        Friend friend;
        ArrayList friends = new ArrayList();

        NetworkManager.sendStandardHeader(out, Globals.REQUEST_FRIENDS);
        out.flush();

        if (receiveStandardHeader(in, Globals.RECEIVE_FRIENDS)) {
            stillReceiving = true;
            while(stillReceiving) {
                try {
                    // This looks horrible, and should be cleaner...  But I
                    // done it this way, so too late now.  This will convert
                    // 4 bytes into an IP address, and 4 bytes into a port
                    // number, then make a new Friend out of it...
                    friend = new Friend(InetAddress.getByName("" +
                                                              in.readUnsignedByte() +
                                                              "." +
                                                              in.readUnsignedByte() +
                                                              "." +
                                                              in.readUnsignedByte() +
                                                              "." +
                                                              in.readUnsignedByte()),
                                        in.readInt());

                    friends.add(friend);
                }
                catch(EOFException e) {
                    // If this exception is thrown then the socket has been
                    // closed and therefore the list is complete...
                    stillReceiving = false;
                }
                catch(UnknownHostException e) {
                    // If this is thrown then we ignore it, as it means that
                    // the IP address was wrongly formed.  By ignoring this
                    // exception, the incorrect address will not be added.
                }
            }
        }
        else {
            throw new IOException("Invalid header in reply");
        }

        // Convert ArrayList to array...
        friendsArray = new Friend[friends.size()];

        for(int num = 0; num < friends.size(); num++) {
            friendsArray[num] = (Friend)friends.get(num);
        }

        // Cleanup...
        friends.clear();
        friends = null;

        try {
            in.close();
            out.close();
        }
        catch(Exception e) {}

        try {
            socket.close();
        }
        catch(Exception e) {}

        friend = null;
        in = null;
        out = null;
        socket = null;

        // Return the array of friends...
        return friendsArray;
    }

    /**
     * Requests a list of remote files that match the current keyword.  If no
     * remote files match the keyword, then the returned array is empty.
     */
    public RemoteFile[] getFilesFromKeyword(String keyword) throws IOException {
        RemoteFile[] remoteFilesArray;
        RemoteFile tmpRemoteFile;
        int tmp;
        int numRemoteFiles;

        long thisFileID;
        int thisFileType;
        String thisFilename;
        long thisFileSize;

        Socket socket = new Socket(address, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        socket.setSoTimeout(Globals.NETWORK_TIMEOUT_PERIOD_LONG);

        // Send request for files matching keyword...
        NetworkManager.sendStandardHeader(out, Globals.DO_YOU_HAVE);
        out.writeUTF(keyword);
        out.flush();

        if (receiveStandardHeader(in, Globals.DO_I_HAVE)) {
            numRemoteFiles = in.readInt();
            remoteFilesArray = new RemoteFile[numRemoteFiles];

            // Grab every occurence of remote file sequentially from stream
            for(int num = 0; num < remoteFilesArray.length; num++) {
                thisFileID = in.readLong();
                thisFileType = in.readInt();
                thisFilename = in.readUTF();
                thisFileSize = in.readLong();

                tmpRemoteFile = new RemoteFile(this, thisFileID, thisFileType, thisFilename, thisFileSize);

                // See if remote file is already in list known of for this
                // friend.  If it is use a reference to the existing remote file
                // otherwise make a new remote file reference...
                tmp = remoteFiles.indexOf(tmpRemoteFile);
                if (tmp > -1) {
                    remoteFilesArray[num] = (RemoteFile)remoteFiles.get(num);
                }
                else {
                    remoteFilesArray[num] = tmpRemoteFile;

                    // This addition needs synchronization because more than one
                    // keyword search may try to access this method...
                    synchronized(this) {
                        remoteFiles.add(tmpRemoteFile);
                    }
                }

                // Add the keyword used in this search to this file reference.
                remoteFilesArray[num].addKeyword(keyword);
            }
        }
        else {
            throw new IOException("Invalid header in reply.");
        }

        // Clean up...
        try {
            in.close();
            out.close();
        }
        catch(Exception e) {}

        try {
            socket.close();
        }
        catch(Exception e) {}

        tmpRemoteFile = null;
        in = null;
        out = null;
        socket = null;

        // Return the array of files...
        return remoteFilesArray;
    }

    /**
     * Determines if Friend exists or not.
     *
     * @return Returns true if Friend is found.
     */
    public boolean pingPong() {
        Socket socket = null;
        DataInputStream in = null;
        DataOutputStream out = null;

        boolean success;

        try {
            socket = new Socket(address, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            socket.setSoTimeout(Globals.NETWORK_TIMEOUT_PERIOD);

            NetworkManager.sendStandardHeader(out, Globals.PING);
            receiveStandardHeader(in, Globals.PONG);

            success = true;
        }
        catch(Exception e) {
            success = false;
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

        try {
            socket.close();
        }
        catch(Exception e) {}

        in = null;
        out = null;
        socket = null;

        return success;
    }
}