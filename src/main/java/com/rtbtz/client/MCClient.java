package com.rtbtz.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that is used for managing client state 
 * (connected/disconnected) for separate threads
 * @author Petr
 */
class Disconnector {
    private boolean disconnected = false;

    synchronized public boolean isDisconnected() {
        return disconnected;
    }

    synchronized public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }
}

/**
 * Single thread that is used for intercepting
 * messages from server
 * @author Petr
 */
class OutThread extends Thread {
    BufferedReader param;
    Disconnector disc;

    OutThread(BufferedReader param, Disconnector disc) {
        this.param = param;
        this.disc = disc;
    }

    @Override
    public void run() {
        try {
            String command;
            String responce;
            do {
                responce = param.readLine();
                if (responce == null) {
                    System.out.println("Connection closed.");
                    disc.setDisconnected(true);
                    break;
                }
                System.out.println(responce);
            } while (true);
        } catch (IOException ex) {
            Logger.getLogger(OutThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/**
 * Client class
 * @author Petr
 */
public class MCClient {

    public static final int DEFAULT_PORT = 8030;

    public static void main(String[] args) {
        BufferedReader clientInput = null;
        BufferedReader consoleInput = null;
        PrintWriter clientOutput = null;
        InetAddress host = null;
        Socket sock = null;
        Disconnector disc = new Disconnector();

        
        try {
            //A way of selecting address can be changed but now it's more rational to use local host.
            host = InetAddress.getLocalHost();
            sock = new Socket(host, DEFAULT_PORT);
            consoleInput = new BufferedReader(new InputStreamReader(System.in));
            clientInput = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            clientOutput = new PrintWriter(sock.getOutputStream());

            System.out.println("Your address: " + host);

            Thread myThread = new OutThread(clientInput, disc);
            myThread.start();

            String command;
            
            //Reading client commands in loop and sending them to server
            do {
                if (disc.isDisconnected()) {
                    break;
                }

                command = consoleInput.readLine();
                clientOutput.println(command);
                clientOutput.flush();
            } while (command.indexOf("/quit") != 0);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MCClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MCClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
