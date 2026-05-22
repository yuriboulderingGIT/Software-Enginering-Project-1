# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application designed to simulate real-time cardiovascular data for multiple patients. This tool is particularly useful for educational purposes, enabling students to interact with real-time data streams of ECG, blood pressure, blood saturation, and other cardiovascular signals.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  - Console output for direct observation.
  - File output for data persistence.
  - WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/tpepels/signal_project.git
   ```

2. Navigate to the project directory:

   ```sh
   cd signal_project
   ```

3. Compile and package the application using Maven:
   ```sh
   mvn clean package
   ```
   This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


## UML Models
See the [UML diagrams here](uml-models/uml_models.md)
The UML diagrams are automatically rendered when viewed through GitHub.

## Part 4 - Design Patterns
Implemented four design patterns across the alert subsystem. The Factory Method pattern
was applied through an abstract AlertFactory and three concrete subclasses that produce
typed alerts. The Strategy pattern replaced hardcoded alert logic in AlertGenerator with
a pluggable list of AlertStrategy implementations. The Decorator pattern allows alerts to
be wrapped with priority and repeat-count metadata. The Singleton pattern was applied to
DataStorage and HealthDataSimulator to ensure a single shared instance is used throughout
the application.

## Part 5 - Real-Time Data Processing
Extended the DataReader interface with a default startStreaming() method and implemented
WebSocketClientImpl, which extends the Java-WebSocket library client and implements
DataReader. The client connects to a WebSocket server, parses incoming messages in the
format patientId,timestamp,label,value, and stores them in DataStorage in real time.
Malformed messages are logged and skipped. Unexpected disconnections trigger an automatic
reconnect attempt.

## AI Assistance
I have used the assistance of AI for this project.
AI was primarily used to help with documentation for Parts 4 and 5,
assist in writing unit tests, and to better understand the course
concepts such as design patterns and how the different components of
the system interact with each other.

## Project Members
 - Student ID: i6443157 
