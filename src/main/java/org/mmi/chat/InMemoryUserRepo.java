package org.mmi.chat;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepo implements UsersRepository {

    /*I really don't want to check if name locking is enough to protect
    regular HashMap from breaking.
     */
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public User findByName(String name) {
        return users.get(name);
    }

    @Override
    public void save(User user) {
        Objects.requireNonNull(user.getName());
        users.put(user.getName(), user);
    }
}
