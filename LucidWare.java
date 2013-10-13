import java.util.*;
import java.awt.Dimension;

import com.totalchange.lucidware.*;
import com.totalchange.lucidware.ui.*;
import com.totalchange.lucidware.resources.*;
import com.totalchange.lucidware.net.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class LucidWare {
    private FriendsArrayList friends;

    private SearchResources resourceManager;
    private NetworkManager netManager;
    private PollFriends pollFriends;
    private MainWindow mainWindow;

    public LucidWare() throws Exception {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int xPos;
        int yPos;

        friends = new FriendsArrayList();
        mainWindow = new MainWindow(friends, this);

        xPos = (screenSize.width / 2) - (mainWindow.getSize().width / 2);
        yPos = (screenSize.height / 2) - (mainWindow.getSize().height / 2);
        mainWindow.setLocation(xPos, yPos);

        CacheHandler.deleteEntireCache();

        resourceManager = new SearchResources();
        netManager = new NetworkManager(friends);

        pollFriends = new PollFriends(friends);
    }

    public void shutdown() {
        netManager.shutdown();
        resourceManager.shutdown();
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        LucidWare lucidWare = new LucidWare();
    }
}