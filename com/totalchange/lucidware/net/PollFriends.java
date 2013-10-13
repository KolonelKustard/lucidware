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
 * This class contains an inner thread that is used to poll friends for new
 * lists of friends.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class PollFriends {
    private ArrayList friends;
    private int lastPolledFriend;
    private ArrayList pollThreads;

    public PollFriends(ArrayList friends) {
        this.friends = friends;
        pollThreads = new ArrayList();

        // Add the determined number of polling threads...
        for(int num = 0; num < Globals.NUM_OF_POLL_THREADS; num++) {
            pollThreads.add(new PollThread());
        }
    }

    public int getNumOfThreads() {
        return pollThreads.size();
    }

    /**
     * Change the number of polling threads to the number specified.
     */
    public synchronized void setNumOfThreads(int numOfThreads) {
        if (numOfThreads >= pollThreads.size()) {
            for(int num = 0; num < (numOfThreads - pollThreads.size()); num++) {
                pollThreads.add(new PollThread());
            }
        }
        else {
            for(int num = 0; num < (pollThreads.size() - numOfThreads); num++) {
                ((PollThread)pollThreads.get(0)).shutdown();
            }
        }
    }

    /**
     * This method requests a list of friends from an existing friend.  This
     * method should be called whenever a new friend is added to the list.  The
     * method will also automatically remove a friend that does not respond to
     * requests.
     */
    public void pollFriend(Friend friend) {
        Friend[] remoteFriends = null;

        boolean retryStill = true;
        int retrys = 0;

        // This block deals with request to the friend...
        synchronized(friend) {
            while(retryStill) {
                try {
                    remoteFriends = friend.getFriends();
                    break;
                }
                catch(IOException e) {
                    retrys++;

                    if(retrys > Globals.NETWORK_RETRIES) {
                        retryStill = false;
                    }
                }
            }
        }

        // This block deals with altering the list of friends...
        synchronized(friends) {

            // If the retryStill is true, then the attempt to get the
            // friends was successful.  If it wasn't, then we need to
            // remove this friend from the list...
            if (retryStill) {
                if (remoteFriends != null) {
                    for(int num = 0; num < remoteFriends.length; num++) {

                        // If we don't already have this friend, then it can
                        // be added to the list...
                        if (!friends.contains(remoteFriends[num])) {
                           friends.add(remoteFriends[num]);
                        }
                    }
                }
            }
            else {
                friends.remove(friend);
            }
        }
    }

    /**
     * This is the inner thread that repeatedly requests lists of new friends
     * from existing friends...
     */
    private class PollThread extends Thread {
        private boolean stillPoll;

        public PollThread() {
            this.start();
        }

        public void run() {
            Friend friend;
            int currentNum;

            stillPoll = true;
            while(stillPoll) {
                currentNum = lastPolledFriend++;

                if(currentNum >= friends.size()) {
                    currentNum = lastPolledFriend = 0;
                }

                try {
                    friend = (Friend)friends.get(currentNum);
                    pollFriend(friend);
                }
                catch(IndexOutOfBoundsException e) {
                    // If out of bounds, there's not much we can do other than
                    // keep trying until there are friends...
                }

                try {
                    this.sleep(Globals.POLL_THREAD_PAUSE);
                }
                catch(InterruptedException e) {}
            }

            // Clean up...
            friend = null;

            pollThreads.remove(this);
        }

        public void shutdown() {
            this.stillPoll = false;
        }
    }
}