package org.mmi.chat;

public interface UsersRepository {

    User findByName(String name);

    void save(User user);

}
