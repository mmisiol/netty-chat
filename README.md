# netty-chat

JDK of version 17 or higher is required to run the project.

Clone or download the code from [https://github.com/mmisiol/netty-chat](https://github.com/mmisiol/netty-chat)

You can run the server with following command while in root project directory:

Windows

```shell
.\gradlew.bat run
```

Linux

```shell
./gradlew run
```

It is also possible to run the application by importing the project into Intellij Idea IDE and running class

```
org.mmi.chat.ChatServer
```

By default, sever starts listening on port 8080. This can be changed by providing desired port number as first command
line argument.

```
.\gradlew.bat run --args='10001'
```


To test the application you need a telnet command line client. After starting the server run following command. Remember to adjust the port number in case it was changed.

```
telnet localhost 8080
```

### Chat supports following commands

- **/help**, **/h** - Displays list of commands.
- **/login <name> <password>** - Logs in as **name**. If name is not in the system, creates a new user.
- **/join**, **/j** <channel> - Joins a channel and leaves the current one. Channel can hold up to 10 connections.
- **/leave** - Leaves current channel.
- **/disconnect**, **/dc** - Closes connection to the server.
- **/list**, **/ls** - Displays list of available channels.
- **/users**, **/u** - Lists users int the current channel. 
- **text message terminated with CR** - Sends message to all users in the current channel.




## Notes


- A user can only join one channel at a time restriction was applied to single user connection (netty channel)
as for to have a single channel bound to user auth instead  implies automatic switching of channels for each connection
authenticated as that user. That leads to awful amount of edge cases like "Is the user with 3 connections allowed to join a channel with
2 opens slots?". Task description does not cover any of these cases. I assumed the limitations is for the connection.


- Internally decided to use term "room" for what was described as a "channel" int the task to avoid confusion with Netty channel.


- For sake of simplicity there is no protection or support for too long input


- Even after adding a proper persistence implementation the app will not be ready for horizontal scaling. A layer handling cross-instance syncing the messages is needed.


- There is no proper error handling and meaningful error messages are missing.


- ChatHandler's channelRead method is synchronized and each netty channel holds its own instance
of the handler. We are safe from concurrency issues in this scope. It is by far not the most optimal solution but simple
one, just enough for MVP. What was left was to ensure shared resources are thread safe: Rooms, UserRepository.


- Using "Strategy" pattern would be awesome. Each command might have a separate class as a handler. Unfortunetly 
that was not possible due to the requirement to put logic in "ChatHandler"

