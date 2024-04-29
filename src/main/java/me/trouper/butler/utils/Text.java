package me.trouper.butler.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text {

    public static String getPacketType(String input) {
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static String getPacketArgs(String input) {
        Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\s*\\]");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            int endIndex = matcher.end();
            return input.substring(endIndex).trim();
        } else {
            return null;
        }
    }
}
