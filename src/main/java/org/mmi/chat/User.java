package org.mmi.chat;

import lombok.Data;

@Data
public class User {

    private String name;
    private String passwordHash;
    private String lastChannel;
}
