// PingClient.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.nio.ByteBuffer;

/* 
    * Client to send ping requests over UDP.
    */
    
public class PingClient {

    static InetAddress host;
    static int port;
    static String passwd;
    static DatagramSocket socket;
    //DatagramPacket message;
    static int message_number;
    static long[] rtt_array = {0,0,0,0,0,0,0,0,0,0};

    public static void main(String[] args) throws Exception {
        // Get command line argument.
        if (args.length != 3) {
            System.out.println("Required arguments: host port passwd");

            return;
        }

        host = InetAddress.getByName(args[0]);
        port = Integer.parseInt(args[1]);
        passwd = args[2];
   
        //create socket for sending messages
        socket = new DatagramSocket();

        //initialize sequence number
        message_number = 0;
        for (int i = 0; i < 10; i++) {

          //create task and timer task to send the messages 10 times
          //we schedule the 10 messages to be sent at time 
          //t=1 seconds, 2 seconds, etc.
          TimerTask task = new MessageSend();
          Timer timer = new Timer(true);

          timer.schedule(task, i*1000);
                }
        //sleep 20 seconds so program does not end before we
        //send and receive messages
        Thread.sleep(20000);

        //output minimum, maximum, and average rtt
        int non_zero = 0;
        long min = 0;
        long max = 0;
        long ave = 0;
        long rtt_sum = 0;
        double loss = 1.0;
        for (int m = 0; m < rtt_array.length; m++) {
          //System.out.println(rtt_array[m]);
          if (rtt_array[m] != 0) {
            
            non_zero++;

            if (min == 0) {
              min = rtt_array[m];
            } else {
              if (rtt_array[m] < min) {
                min = rtt_array[m];
              }
            }

            if (rtt_array[m] > max) {
              max = rtt_array[m];
            }

            rtt_sum += rtt_array[m];
          }

        }

        if (non_zero != 0) {
          ave = rtt_sum/non_zero;
          loss = non_zero / 10.0;
        }

        System.out.println("Minimum RTT: ");
        System.out.println(min);
        System.out.println("Maximum RTT: ");
        System.out.println(max);
        System.out.println("Average RTT: ");
        System.out.println(ave);
        System.out.println("Loss Rate: ");
        System.out.println(loss);

    }
    //specify timer task that will send and receive messages with server
    public static class MessageSend extends TimerTask {

      //run method specifies task
      public void run() {
        //message_number++;

        //form the message as specified: PING SEQUENCE_NUMBER TIME_STAMP PASSWORD CRLF
        byte[] ping = "PING".getBytes();
        ByteBuffer seq_buff = ByteBuffer.allocate(2);
        seq_buff.putShort((short) message_number);
        //System.out.println(message_number);
        byte[] seq_number = seq_buff.array();
        //byte[] seq_number = String.valueOf(message_number).getBytes();
        byte[] space = " ".getBytes();
        ByteBuffer time_buff = ByteBuffer.allocate(8);
        time_buff.putLong(System.currentTimeMillis());
        byte[] time = time_buff.array();
        //byte[] time = String.valueOf(System.currentTimeMillis()).getBytes();
        byte[] password = passwd.getBytes();
        byte[] crlf = "\r\n".getBytes();

        int array_size = ping.length + 4*space.length + seq_number.length + time.length + password.length + crlf.length;

        //create byte buffer to construct message
        ByteBuffer msg_buff = ByteBuffer.allocate(array_size);
          
        //add message components into byte buffer
        msg_buff.put(ping);
        msg_buff.put(space);
        msg_buff.put(seq_number);
        msg_buff.put(space);
        msg_buff.put(time);
        msg_buff.put(space);
        msg_buff.put(password);
        msg_buff.put(space);
        msg_buff.put(crlf);
          
        //convert byte buffer into byte array
        byte[] msg = msg_buff.array();

        //insert message into datagram packet with server host and port
        DatagramPacket message = new DatagramPacket(msg, msg.length, host, port);
        
        try {
          //send message to server
          socket.send(message);
          //System.out.println("message sent");
          long rtt = 0;
          long recTime = 0;
          //record current time to determine rtt
          long sendTime = System.currentTimeMillis();
          while(true) {

            byte[] data = new byte[1024];
            DatagramPacket receiveData = new DatagramPacket(data, data.length);
            
            //long start = System.currentTimeMillis();
 
            //wait 1 second for server to respond
            socket.setSoTimeout(1000);
            try {
              //receive server response
              socket.receive(receiveData);
            } catch (IOException e) {
              //System.out.println("Packet not received");
              break;
            }
            //record time when message was received
            recTime = System.currentTimeMillis();
            //rtt is different between time message sent and received
            rtt = recTime - sendTime;

            //check if the sequence number on sent message matches
            //sequence number on received message
            if ((msg[5] == receiveData.getData()[9]) && (msg[6] == receiveData.getData()[10])) {
              //System.out.println("sequence numbers are good");
              rtt_array[message_number] = rtt;
            } else {
              System.out.println("sequence number mismatch!");
            }

            //System.out.println("rtt is: ");
            //System.out.println(rtt);
            //printData(receiveData);
            break;
          }
        } catch (Exception e) {
          System.out.println(e);
        }
        message_number++;
      }
    }

    
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
    }// end of printData
}
