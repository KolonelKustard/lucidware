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
 * Contains static methods called be request threads when a friend wants a list
 * of friends.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class SendFriends {
    public static void sendFriends(Socket socket, ArrayList friends) {
        DataOutputStream out = null;
        Object[] friendsArray;
        Friend friend;
        byte[] byteAddr;

        // Now we'll send them the list of friends that we have...
        try {
            out = new DataOutputStream(socket.getOutputStream());

            NetworkManager.sendStandardHeader(out, Globals.RECEIVE_FRIENDS);

            synchronized(friends) {
                // Needs to be synchronized because other threads also add and
                // remove elements from this ArrayList...
                friendsArray = friends.toArray();
            }

            for(int num = 0; num < friendsArray.length; num++) {
                friend = (Friend)friendsArray[num];

                byteAddr = friend.getAddress().getAddress();
                out.write(byteAddr, 0, 4);

                out.writeInt(friend.getPort());
            }
        }
        catch(Exception e) {
            // In this case we will not handle an exception and will just end
            // the thread.  Another request will be received if the list needs
            // to be sent again...
        }

        // Clean up...
        try {
            out.close();
        }
        catch(Exception e) {}

        out = null;
        byteAddr = null;
        friendsArray = null;
        friend = null;
    }
}