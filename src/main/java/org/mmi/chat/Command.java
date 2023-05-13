package org.mmi.chat;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum Command {
    HELP(array("help", "h"), " - lists available commands"),
    LOGIN(array("login"), "<name> <password> - logs in or creates a new user"),
    JOIN(array("join", "j"), "<channel> - joins channel"),
    LEAVE(array("leave"), "- leave the current channel"),
    DISCONNECT(array("disconnect", "dc"), "disconnect from the chat"),
    LIST(array("list", "ls"), "List available channels"),
    USERS(array("users", "u"), "List active users in the current channel"),
    MESSAGE(array(), "Send message to all users in the channel");;

    private final static String PREFIX = "/";
    private final static Map<String, Command> MAPPINGS = new HashMap<>();

    static {
        for (Command command : values()) {
            for (String chatCommand : command.chatCommands) {
                MAPPINGS.put(PREFIX + chatCommand.trim().toLowerCase(), command);
            }
        }
    }


    private final String[] chatCommands;
    private final String description;

    private static String[] array(String... strings) {
        return Arrays.asList(strings).toArray(new String[0]);
    }

    public static Command parseCommand(String message) {
        String[] words = message.split("\\s+");
        String firstWord = words[0];
        return MAPPINGS.getOrDefault(firstWord.toLowerCase(), MESSAGE);
    }
}
