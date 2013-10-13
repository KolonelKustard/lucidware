package com.totalchange.lucidware.resources;

import java.io.*;
import java.util.*;

/**
 * Title:        Final Year Project
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Flatulence Inc.
 *
 * Not yet implemented, but this method will return an array of keywords parsed
 * from the file provided.  Must be sure not to return any duplicate keywords,
 * and must be sure not to produce too many keywords.  Maybe have a short
 * dictionary to parse words through.
 *
 * Also, all keywords MUST BE LOWERCASE.
 *
 * @author Kolonel Kustard
 * @version 1.0
 */
public class TextFileParser {
    /**
     * Minimum length a word must be to be considered a keyword...
     */
    public static final int MINIMUM_KEYWORD_LENGTH = 6;

    /**
     * Only accept these chars in keywords...
     */
    public static final char[] ACCEPTED_CHARS = {'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9'};

    public static String[] getKeywords(File textFile) {
        long fileLength;
        String thisString = "";
        char thisChar;
        boolean acceptableChar;
        String[] keywords;
        ArrayList strings = new ArrayList();
        DataInputStream in;

        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(textFile)));

            fileLength = textFile.length();
            for(long charNum = 0; charNum < fileLength; charNum++) {
                acceptableChar = false;
                thisChar = (char)in.read();

                thisChar = String.valueOf(thisChar).toLowerCase().charAt(0);

                for(int num = 0; num < ACCEPTED_CHARS.length; num++) {
                    if (thisChar == ACCEPTED_CHARS[num]) {
                        acceptableChar = true;
                        break;
                    }
                }

                if (acceptableChar) {
                    thisString += thisChar;
                }
                else {
                    if (thisString.length() >= MINIMUM_KEYWORD_LENGTH) {
                        if (!strings.contains(thisString)) {
                            strings.add(thisString);
                        }
                    }
                    thisString = "";
                }
            }
        }
        catch(Exception e) {e.printStackTrace();}

        keywords = new String[strings.size()];
        for(int num = 0; num < strings.size(); num++) {
            keywords[num] = (String)strings.get(num);
        }

        return keywords;
    }

    /**
     * For testing...
     */
    public static void main(String[] args) {
        long startTime, stopTime;

        startTime = System.currentTimeMillis();
        String[] words = getKeywords(new File("c:/poopy.doc"));
        stopTime = System.currentTimeMillis();

        System.out.println("Time taken: " + (stopTime - startTime) + "ms");
        System.out.println("Num of keywords: " + words.length);

        for(int num = 0; num < words.length; num++) {
            System.out.print(words[num] + ", ");
        }
        System.out.println();

        System.out.println("\nFinding duplicates...");
        int numDuplicates = 0;

        for(int num1 = 0; num1 < words.length; num1++) {
            for(int num2 = 0; num2 < words.length; num2++) {
                if (num1 != num2) {
                    if (words[num1].equals(words[num2])) {
                        numDuplicates++;
                    }
                }
            }
        }

        System.out.println("Num duplicates: " + numDuplicates);
    }
}