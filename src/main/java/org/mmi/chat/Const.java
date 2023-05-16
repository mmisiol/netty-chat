package org.mmi.chat;

import io.netty.util.AttributeKey;

public interface Const {
    String LINE_END = "\r\n";
    AttributeKey<String> NAME = AttributeKey.newInstance("name");
}
