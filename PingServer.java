// PingServer.java
// Code adapted from Yale CS433/533 class administrators
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;

/* 
    * Server to process ping requests over UDP.
    */
    
public class PingServer
{
    private static double LOSS_RATE = 0.3;
    private static int AVERAGE_DELAY = 100; // milliseconds
    private static String PASSWORD;
    private static int port;

    public static void main(String[] args) throws Exception
    {
        // Get command line argument.
        switch(args.length) {
          case 2:
            port = Integer.parseInt(args[0]);
            PASSWORD = args[1];
            break;
          case 4:
            port = Integer.parseInt(args[0]);
            PASSWORD = args[1];
            if (args[2].equals("[-delay")) {
              String delay = args[3].replace("]","");
              AVERAGE_DELAY = Integer.parseInt(delay);
            } else {
              String loss = args[3].replace("]","");
              LOSS_RATE = Double.parseDouble(loss);
            }
            break;
          case 6:
            port = Integer.parseInt(args[0]);
            PASSWORD = args[1];
            String delay = args[3].replace("]","");
            AVERAGE_DELAY = Integer.parseInt(delay);
            String loss = args[5].replace("]","");
            LOSS_RATE = Double.parseDouble(loss);
            break;
          default:
            System.out.println("Usage: port passwd [-delay delay] [-loss loss]");
            return;
        }
        
        // Create random number generator for use in simulating
        // packet loss and network delay.
        Random random = new Random();

        // Create a datagram socket for receiving and sending
        // UDP packets through the port specified on the
        // command line.
        DatagramSocket socket = new DatagramSocket(port);
        InetAddress localh = InetAddress.getLocalHost();
        System.out.println(localh);
        // Processing loop.
        while (true) {

            // Create a datagram packet to hold incomming UDP packet.
            DatagramPacket
            request = new DatagramPacket(new byte[1024], 1024);
    
            // Block until receives a UDP packet.
            socket.receive(request);
        
            // Print the received data, for debugging
            printData(request);

            // Decide whether to reply, or simulate packet loss.
            if (random.nextDouble() < LOSS_RATE) {
            //System.out.println(" Reply not sent.");
            continue;
            }

            // Simulate prorogation delay.
            Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));

            // Send reply.
            InetAddress clientHost = request.getAddress();
            int clientPort = request.getPort();
            byte[] buf = request.getData();
            
            //handle received message
            //split message into components
            ByteArrayInputStream byteis = new ByteArrayInputStream(buf);
            InputStreamReader inputsr = new InputStreamReader(byteis);
            BufferedReader buffr = new BufferedReader(inputsr);
            String line = buffr.readLine();
            String[] words = line.split(" ");
           
            //form response message as specified:
            //PINGECHO SEQUENCE_NUMBER TIME_STAMP PASSWORD CRLF
            byte[] pingecho = "PINGECHO".getBytes();
            byte[] space = " ".getBytes();
            byte[] seq = words[1].getBytes();
            byte[] time = words[2].getBytes();
            byte[] passwordCRLF = words[3].getBytes();
            byte[] crlf = "\r\n".getBytes();

            //check if received password is valid password
            if (words[3].equals(PASSWORD)) {

              int array_size = pingecho.length + 4*space.length + seq.length + time.length + passwordCRLF.length + crlf.length;

              //create byte buffer to form response
              ByteBuffer resp_buff = ByteBuffer.allocate(array_size);
            
              //put message components into byte buffer
              resp_buff.put(pingecho);
              resp_buff.put(space);
              resp_buff.put(seq);
              resp_buff.put(space);
              resp_buff.put(time);
              resp_buff.put(space);
              resp_buff.put(passwordCRLF);
              resp_buff.put(space);
              resp_buff.put(crlf);
            
              //convert byte buffer to byte array
              byte[] response = resp_buff.array();
            
              //put response into datagram packet addressed to client
              DatagramPacket reply = new DatagramPacket(response, response.length, clientHost, clientPort);

              //send to client
              socket.send(reply);
    
              //System.out.println(" Reply sent.");
            } else {
              System.out.println(" Invalid password.");
            }
        } // end of while
    } // end of main

    /* 
    * Print ping data to the standard output stream.
    */
    private static void printData(DatagramPacket request) 
            throws Exception

    {
        // Obtain references to the packet's array of bytes.
        byte[] buf = request.getData();

        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais 
            = new ByteArrayInputStream(buf);

        // Wrap the byte array output stream in an input 
        // stream reader, so you can read the data as a
        // stream of **characters**: reader/writer handles 
        // characters
        InputStreamReader isr 
            = new InputStreamReader(bais);

        // Wrap the input stream reader in a bufferred reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any 
        // combination of \r and \n.)
        BufferedReader br 
            = new BufferedReader(isr);

        // The message data is contained in a single line, 
        // so read this line.
        String line = br.readLine();

        // Print host address and data received from it.
        System.out.println("Received from " +         
        request.getAddress().getHostAddress() +
        ": " +
        new String(line) );
    } // end of printData
} // end of class
