package com.totalchange.lucidware.net;

import java.net.*;
import java.util.ArrayList;

import com.totalchange.lucidware.Globals;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class FriendsArrayList extends ArrayList {
    private ArrayList listeners;

    public FriendsArrayList() {
        super();
        listeners = new ArrayList();

        // Add ourselves to the list of friends...
        try {
            super.add(new Friend(InetAddress.getLocalHost(), Globals.NETWORK_PORT));
        }
        catch(Exception e) {}
    }

    public boolean add(Object o) {
        boolean result;
        boolean success;

        if (o instanceof Friend) {
            success = ((Friend)o).pingPong();

            if (success) {
                result = super.add(o);
            }
            else {
                result = false;
            }
        }
        else {
            result = false;
        }

        // Alert listeners of any change in size of ArrayList
        for(int num = 0; num < listeners.size(); num++) {
            ((FriendsArrayListListener)listeners.get(num)).arraySizeEvent(super.size());
        }

        return result;
    }

    public boolean remove(Object o) {
        boolean result;
        result = super.remove(o);

        for(int num = 0; num < listeners.size(); num++) {
            ((FriendsArrayListListener)listeners.get(num)).arraySizeEvent(super.size());
        }

        return result;
    }

    public void addListener(FriendsArrayListListener listener) {
        listeners.add(listener);
        listener.arraySizeEvent(super.size());
    }

    public void removeListener(FriendsArrayListListener listener) {
        listeners.remove(listener);
    }
}