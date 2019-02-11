# PingServer
Implementation of a basic UDP Ping Server. This implementation was written while taking the Yale Computer Science course 533, Networks. This code is designed to run on the Yale "Zoo", the student computer network. For the detailed specification for this client and server, please see http://zoo.cs.yale.edu/classes/cs433/cs433-2018-fall/assignments/assign2/index.html


# Compile and Running
The client and server should be run in separate terminals.

To compile the client and server, do the following:

javac PingServer.java
javac PingClient.java

To run the client and server, do the following:

java PingServer [port]
java PingClient [host] [port] [password]

Port is the port number the server listens on, host is the name of the computer the server is running on, and the password is a string that will be added to ping requests so that the server only replies to valid pings.
