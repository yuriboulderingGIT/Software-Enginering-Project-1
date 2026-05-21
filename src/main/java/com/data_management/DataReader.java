package com.data_management;

import java.io.IOException;

public interface DataReader {

    /**
     * Reads data from a specified source and stores it in the data storage.
     *
     * @param dataStorage the storage where data will be stored
     * @throws IOException if there is an error reading the data
     */
    void readData(DataStorage dataStorage) throws IOException;

    /**
     * Starts a real-time data stream from a source and stores incoming data
     * in the data storage as it arrives.
     *
     * <p>A default no-op implementation is provided so that existing classes
     * such as {@link FileDataReader} do not need to override this method.</p>
     *
     * @param dataStorage the storage where data will be stored
     * @throws Exception if there is an error starting the stream
     */
    default void startStreaming(DataStorage dataStorage) throws Exception {
        // default: no-op for file-based readers that do not support streaming
    }
}
