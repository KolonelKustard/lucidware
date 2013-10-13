package com.totalchange.lucidware.resources;

import java.io.*;

import com.totalchange.lucidware.Globals;

/**
 * Title:        Final Year Project
 * Description:  A collection of static methods for dealing with the cache...
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class CacheHandler {

    /**
     * Returns true if the cache directory and its contents were successfully
     * wiped out.
     */
    public static boolean deleteEntireCache() {
        File cacheDir = new File(Globals.CACHE_DIR);
        recurseDelete(cacheDir);

        if (cacheDir.list().length == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    private static void recurseDelete(File directory) {
        String[] thisStructure = directory.list();
        File thisFile;

        try {
            for(int num = 0; num < thisStructure.length; num++) {
                thisFile = new File(directory, thisStructure[num]);

                // If this is a directory, then recurse further down the tree...
                if (thisFile.isDirectory()) {
                    recurseDelete(thisFile);
                }

                // Now delete the file / directory...
                try {
                    thisFile.delete();
                }
                catch (Exception e) {}
            }
        }
        catch(Exception e) {
            // Exception could be thrown if there's an error with the
            // filesystem.  In this case, we'll just ignore it and carry on with
            // the rest...
        }
    }
}