package org.mmi.chat;

import io.netty.util.AttributeKey;

public interface Const {
    public static String LINE_END = "\r\n";
    public static AttributeKey<String> NAME = AttributeKey.newInstance("name");
}
