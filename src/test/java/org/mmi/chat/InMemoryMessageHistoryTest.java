package org.mmi.chat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class InMemoryMessageHistoryTest {


    @Test
    void emptyWhenEmpty() {
        MessageHistory history = new InMemoryMessageHistory();
        Assertions.assertTrue(history.latest("someChannel", 5).isEmpty());
    }


    @Test
    void singleEntry() {
        MessageHistory history = new InMemoryMessageHistory();
        String channel = "channel";
        String m1 = "m1";

        history.put(channel, m1);
        history.put(channel + "2", "somethingElse");

        List<String> expected = List.of(m1);
        List<String> actual = history.latest(channel, 3);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void moreThanRequired() {
        MessageHistory history = new InMemoryMessageHistory();
        String channel = "channel";
        history.put(channel, "m1");
        history.put(channel, "m2");
        history.put(channel, "m3");
        history.put(channel, "m4");
        history.put(channel, "m5");
        history.put(channel + "2", "somethingElse");

        List<String> expected = List.of("m2", "m3", "m4", "m5");
        List<String> actual = history.latest(channel, 4);

        Assertions.assertEquals(expected, actual);
    }
}