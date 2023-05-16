package org.mmi.chat;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Command {
    HELP(array("help", "h"), "               - lists available commands"),
    LOGIN(array("login"), "<name> <password> - logs in or creates a new user"),
    JOIN(array("join", "j"), "<channel>      - joins channel"),
    LEAVE(array("leave"), "                  - leave the current channel"),
    DISCONNECT(array("disconnect", "dc"), "        - disconnect from the chat"),
    LIST(array("list", "ls"), "              - list available channels"),
    USERS(array("users", "u"), "              - list active users in the current channel"),
    MESSAGE(array(), "<message>               - send message to all users in the channel");

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
        return strings.clone();
    }

    public static Command parseCommand(String message) {
        String[] words = message.split("\\s+");
        String firstWord = words[0];
        return MAPPINGS.getOrDefault(firstWord.toLowerCase(), MESSAGE);
    }

    public static String help() {
        StringBuilder help = new StringBuilder();
        help.append(Const.LINE_END);
        for (Command command : values()) {
            String commands = Arrays.stream(command.chatCommands)
                    .map(com -> PREFIX + com)
                    .collect(Collectors.joining(", "));

            help.append(commands)
                    .append(" ")
                    .append(command.description)
                    .append(Const.LINE_END);
        }

        return help.toString();
    }
}
