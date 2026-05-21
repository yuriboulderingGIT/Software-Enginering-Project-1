package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

/**
 * A WebSocket client that connects to a real-time patient data server
 * and stores incoming messages in DataStorage.
 *
 * <p>This class extends the Java-WebSocket library's {@code WebSocketClient}
 * and also implements the {@code DataReader} interface so it fits alongside
 * {@link FileDataReader} in the existing data management system.</p>
 *
 * <p>Expected message format from the server:
 * {@code patientId,timestamp,label,value}
 * <br>Example: {@code 1,1714376789050,SystolicPressure,120.0}</p>
 */
public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    /** The storage object where all received patient data is saved. */
    private DataStorage dataStorage;

    /**
     * Tracks whether we closed the connection ourselves.
     * If true, onClose() will not attempt to reconnect.
     */
    private boolean intentionallyClosed;

    /**
     * Creates a new WebSocketClientImpl ready to connect to a server.
     *
     * @param serverUri   the URI of the WebSocket server
     *                    (for example: {@code URI.create("ws://localhost:8080")})
     * @param dataStorage the storage object where received patient data will be saved
     */
    public WebSocketClientImpl(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.dataStorage = dataStorage;
        this.intentionallyClosed = false;
    }

    /**
     * Called automatically when the WebSocket connection is successfully opened.
     * Logs the event so it is visible in the console.
     *
     * @param handshake handshake details sent by the server
     */
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[WebSocketClientImpl] Connected to server: " + getURI());
    }

    /**
     * Called automatically whenever a text message arrives from the server.
     * Parses the message and saves the data to DataStorage.
     *
     * <p>Malformed messages (wrong number of fields, non-numeric values, empty)
     * are logged and skipped — they do not crash the client.</p>
     *
     * @param message the raw text message received from the server
     */
    @Override
    public void onMessage(String message) {
        // Ignore null or blank messages
        if (message == null || message.trim().isEmpty()) {
            System.err.println("[WebSocketClientImpl] Received empty or null message, skipping.");
            return;
        }

        // Split on comma to get the four expected fields
        String[] parts = message.split(",");

        if (parts.length != 4) {
            System.err.println("[WebSocketClientImpl] Malformed message (expected 4 fields, got "
                    + parts.length + "): " + message);
            return;
        }

        try {
            int    patientId = Integer.parseInt(parts[0].trim());
            long   timestamp = Long.parseLong(parts[1].trim());
            String label     = parts[2].trim();
            double value     = Double.parseDouble(parts[3].trim());

            if (label.isEmpty()) {
                System.err.println("[WebSocketClientImpl] Empty label field in message: " + message);
                return;
            }

            dataStorage.addPatientData(patientId, value, label, timestamp);

        } catch (NumberFormatException e) {
            System.err.println("[WebSocketClientImpl] Could not parse a numeric field in message: "
                    + message + " | Error: " + e.getMessage());
        }
    }

    /**
     * Called automatically when the connection closes.
     * Logs the reason and, if the server closed the connection unexpectedly,
     * attempts one reconnect.
     *
     * @param code   status code indicating why the connection closed
     * @param reason a human-readable description of the close reason
     * @param remote true if the server initiated the close; false if this client did
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WebSocketClientImpl] Connection closed."
                + " Code: " + code
                + ", Reason: " + reason
                + ", Closed by remote: " + remote);

        // Only reconnect if the server closed the connection without us asking
        if (!intentionallyClosed && remote) {
            System.out.println("[WebSocketClientImpl] Server closed connection unexpectedly, attempting to reconnect...");
            try {
                reconnect();
            } catch (Exception e) {
                System.err.println("[WebSocketClientImpl] Reconnect attempt failed: " + e.getMessage());
            }
        }
    }

    /**
     * Called automatically when a WebSocket error occurs.
     * Logs the error so it can be diagnosed, but does not crash.
     *
     * @param ex the exception that caused the error
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("[WebSocketClientImpl] WebSocket error: " + ex.getMessage());
    }

    /**
     * Implements the {@link DataReader} interface by delegating to
     * {@link #startStreaming(DataStorage)}.
     * This allows the WebSocket client to be used wherever a DataReader is expected.
     *
     * @param dataStorage the storage where data will be saved
     * @throws IOException if the connection cannot be started
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try {
            startStreaming(dataStorage);
        } catch (Exception e) {
            throw new IOException("Failed to start WebSocket streaming: " + e.getMessage(), e);
        }
    }

    /**
     * Initiates the WebSocket connection and begins receiving real-time data.
     * This method is non-blocking: it starts the connection and returns immediately.
     * Data will arrive asynchronously through {@link #onMessage(String)}.
     *
     * @param dataStorage the storage where received data will be saved
     * @throws Exception if the connection attempt fails to start
     */
    @Override
    public void startStreaming(DataStorage dataStorage) throws Exception {
        this.dataStorage = dataStorage;
        this.intentionallyClosed = false;
        connect(); // non-blocking: starts the connection in the background
    }

    /**
     * Closes the WebSocket connection cleanly.
     * Marks the close as intentional so {@link #onClose} does not try to reconnect.
     */
    public void stopStreaming() {
        intentionallyClosed = true;
        close();
    }
}
