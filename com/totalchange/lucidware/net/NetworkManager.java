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
 * The network listener handles all requests for tasks from friends.  e.g. send
 * a list of known friends, search for a file, that kind of stuff...
 *
 * When a request is received then a thread is started to deal with that
 * request.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class NetworkManager extends Thread {
    private ServerSocket serverSocket;
    private FriendsArrayList friends;
    private boolean stillTakingRequests;

    public NetworkManager(FriendsArrayList friends) throws IOException {
        this.friends = friends;

        serverSocket = new ServerSocket(Globals.NETWORK_PORT);

        this.start();
    }

    /**
     * The main thread loop takes requests from friends and deciphers them.  It
     * then creates a thread to handle the request and waits for the next
     * request.
     */
    public void run() {
        Socket socket;
        int retrys = 0;

        stillTakingRequests = true;

        while(stillTakingRequests) {
            try {
                socket = serverSocket.accept();
                new DealWithRequest(socket);

                // If got this far, no exception has occured, so retrys reset.
                retrys = 0;
            }
            catch(IOException e) {
                // If an exception is caught, we'll retry the predetermined
                // number of times and then give up.
                retrys++;

                if (retrys > Globals.NETWORK_RETRIES) {
                    stillTakingRequests = false;
                }
                else {
                    try {
                        // Sleep for a second before retrying.  Just to check
                        // if it's a problem that will be fixed with time...
                        this.sleep(1000);
                    }
                    catch(Exception e2) {
                    }
                }
            }
        }

        // Any cleanup code will be put here...
        try {
            serverSocket.close();
        }
        catch(Exception e) {
        }

        socket = null;
        serverSocket = null;
    }

    public FriendsArrayList getFriends() {
        return friends;
    }

    public void shutdown() {
        stillTakingRequests = false;

        try {
            serverSocket.close();
        }
        catch(Exception e) {
        }
    }


    /**
     * Generates standard header using standard outgoing message header
     * structure of:
     *
     * 32-bit int Globals.PRIMARY_INT;
     * 8-bit byte representing what this stream is to do;
     * UTF8 format String representing this persons name Globals.NAME;
     * 32-bit int Globals.NETWORK_PORT;
     */
    public static void sendStandardHeader(DataOutputStream out, int streamType) throws IOException {
        out.writeInt(Globals.PRIMARY_INT);
        out.writeUTF(Globals.NAME);
        out.writeInt(Globals.NETWORK_PORT);
        out.write(streamType);
    }

    /**
     * This inner thread is called with every new request.  The thread then goes
     * off and deals with this request.
     */
    private class DealWithRequest extends Thread {
        private Socket socket;
        private Friend receivedFriend;

        public DealWithRequest(Socket socket) {
            this.socket = socket;
            this.start();
        }

        public void run() {
            DataInputStream in = null;
            int remotePort;
            String remoteName;

            try {
                socket.setSoTimeout(Globals.NETWORK_TIMEOUT_PERIOD_LONG);
                in = new DataInputStream(socket.getInputStream());

                // First 4 bytes of request must be int of particular value.
                // This is kind of identifying int thingy...  So to make sure
                // this network stuff isn't a request from a Half-Life server.
                // To see header protocol in full see:
                // NetworkManager.sendStandardHeader();
                if (in.readInt() == Globals.PRIMARY_INT) {

                    // Now we'll see if we already have this friend.  If not,
                    // we'll add them to the list.
                    remoteName = in.readUTF();
                    remotePort = in.readInt();
                    receivedFriend = new Friend(socket.getInetAddress(), remotePort, remoteName);
                    // Actually do the test after dealing with the request...

                    // Now decide what to do with it depending on request type
                    switch(in.readUnsignedByte()) {

                        case(Globals.REQUEST_FRIENDS) :
                            //System.out.println("Received request for friends");
                            SendFriends.sendFriends(socket, friends);
                            break;

                        case(Globals.DO_YOU_HAVE) :
                            System.out.println("Received request to find file");
                            FindFiles.findFiles(socket);
                            break;

                        case(Globals.PLEASE_SEND_ME) :
                            System.out.println("Received request to send file");
                            SendFile.sendFile(socket);
                            break;

                        case(Globals.PING) :
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                            sendStandardHeader(out, Globals.PONG);
                            out.close();
                            out = null;
                            break;

                        default :
                            break;
                    }

                    // Test to see if this querier is on the list...
                    if (!friends.contains(receivedFriend)) {
                        synchronized(friends) {
                            friends.add(receivedFriend);
                        }
                    }
                }
            }
            catch(InterruptedIOException e) {
                // In the case of a socket timeout, then this request is simply
                // ignored and this thread will end...
            }
            catch(SocketException e) {
                // Again, if the socket is invalid, this thread will end.
            }
            catch(IOException e) {
                // Any old errors will be ignored and thread will end
            }

            // Clean up...

            // Thread now ends and should be garbage collected...  But I'll help
            // by nullifying all the stuff...
            try {
                in.close();
            }
            catch(Exception e) {}

            try {
                socket.close();
            }
            catch(Exception e) {}

            in = null;
            socket = null;
            receivedFriend = null;
        }
    }
}