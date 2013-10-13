package com.totalchange.lucidware;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 * @author Kolonel Kustard
 * @version 1.0
 */

public class Globals {
    /**
     * This is this users name...
     */
    public static String NAME = "Roger";

    /**
     * This is the port used to receive network requests.  When sending requests
     * the first available port is used (no defined port).
     */
    public static final int NETWORK_PORT = 50501;

    /**
     * This is the number of retries each of the network based threads will use
     * before giving up and dying.
     */
    public static final int NETWORK_RETRIES = 3;

    /**
     * This is the time in millis to wait before timing out in a request to
     * another friend...
     */
    public static final int NETWORK_TIMEOUT_PERIOD = 10000;

    /**
     * This is the time to timeout on when requesting items such as searches
     * that may take up a longer period of time...
     */
    public static final int NETWORK_TIMEOUT_PERIOD_LONG = 60000;

    /**
     * Used to determine the number of polling threads (request new lists of
     * friends from existing friends) to run at once...
     */
    public static final int NUM_OF_POLL_THREADS = 1;

    /**
     * The number of milliseconds a polling thread waits between polls
     */
    public static final int POLL_THREAD_PAUSE = 3000;

    /**
     * These are the identifying bytes used for communications that are used to
     * decipher what each request is...  Primary int is sent at the start of ALL
     * communications as a way of telling if this is a valid request.  The
     * following byte is the identifier of the type of request that is being
     * sent...
     */
    public static final int PRIMARY_INT = -1;

    public static final int REQUEST_FRIENDS = 0;
    public static final int RECEIVE_FRIENDS = 1;

    public static final int DO_YOU_HAVE = 2;
    public static final int DO_I_HAVE = 3;

    public static final int PLEASE_SEND_ME = 4;
    public static final int I_SEND_YOU = 5;

    public static final int PING = 6;
    public static final int PONG = 7;


    /**
     * The following value and constants are the types of compression used and
     * available for file transfers...  Currently only bzip is an option, with
     * other types & no compression in the todo list
     */
    public static final int CODEC_NONE = 0;
    //public static final int CODEC_GZIP = 1;
    public static final int CODEC_BZIP = 2;

    public static int USING_CODEC = CODEC_NONE;


    /**
     * The main resource finder will always begin searching for resources under
     * the following directory root:
     * ("/" indicates the root of the current disk.)
     */
    public static final String RESOURCE_ROOT = "/";

    public static final String IMAGE_ROOT = "images/";

    public static final int RESOURCE_COLLECTOR_PRIORITY = Thread.MIN_PRIORITY;

    public static final String DB_URL = "jdbc:idb:db/resources.prp";
    public static final String DB_INTERFACE = "org.enhydra.instantdb.jdbc.idbDriver";
    public static final String DB_USERNAME = "admin";
    public static final String DB_PASSWORD = "resources";

    public static final int FILE_TYPE_INVALID = -1;
    public static final int FILE_TYPE_IMAGE = 0;
    public static final int FILE_TYPE_TEXT = 1;
    public static final int FILE_TYPE_MOVIE = 2;

    public static final String[] ACCEPTED_FILE_TYPES = {"gif",
                                                        "jpg",
                                                        "jpeg",
                                                        "txt"};

    public static final int[] ACCEPTED_FILE_TYPES_FILTERS = {FILE_TYPE_IMAGE,
                                                             FILE_TYPE_IMAGE,
                                                             FILE_TYPE_IMAGE,
                                                             FILE_TYPE_TEXT};


    public static final String CACHE_DIR = "cache/";
    public static final String SUB_CACHED_DIR_BEGINS = "cache";


    /**
     * The root directory that all finished documents are output to...
     */
    public static final String OUTPUT_DIR = "output/";
    public static final String OUTPUT_RESOURCE_DIR = "files/";


    public static final int NUM_OF_SEARCH_THREADS = 4;
    public static final int NUM_OF_DOWNLOAD_THREADS = 2;
    public static final int NUM_OF_FILES_PER_SEARCH = 20;
    public static final long MAX_FILE_SIZE = 150000;
}