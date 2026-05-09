package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;


/**
 * Streams patient data records to a single TCP client.
 * Starts a TCP server on the given port and waits for one client to connect.
 * Output is silently ignored until a client connects.
 */

public class TcpOutputStrategy implements OutputStrategy {

     /** The server socket listening for incoming connections. */
    private ServerSocket serverSocket;

     /** The socket of the connected client. */
    private Socket clientSocket;

     /** Used to send data to the connected client. */
    private PrintWriter out;

    /**
     * Starts a TCP server on the given port and waits for one client in the background.
     *
     * @param port the port to listen on, between 1 and 65535
     */

    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a data record to the connected TCP client as: patientId,timestamp,label,data
     * Does nothing if no client is connected yet.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the recording time in milliseconds since epoch
     * @param label     the data category
     * @param data      the value of the data record
     */
    
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
