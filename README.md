# CROSS - an exChange oRder bOokS Service

Implementation of the order book, a fundamental service in financial markets used to support the exchange of assets (stocks, securities, other financial instruments, cryptocurrencies) in centralized trading services.

---

---

**User Manual**

**External Libraries Used:** gson, CROSSSharedUtils.

**Compilation & Execution Manual**
To compile the server from the Server folder:

```
javac -d bin -cp "lib/gson-2.11.0.jar" -sourcepath src;../SharedUtils/src src/*.java ../SharedUtils/src

```

To execute the server:

```
java -cp "bin;lib/gson-2.11.0.jar" cross.server.ServerMain

```

To compile the client from the Client folder:

```
javac -d bin -cp "lib/gson-2.11.0.jar" -sourcepath src;../SharedUtils/src src/*.java ../SharedUtils/src

```

To execute the client:

```
java -cp "bin;lib/gson-2.11.0.jar" cross.client.ClientMain

```

---

**JAR Execution**
To launch the client application:

```
java -jar ClientMain.jar

```

To launch the server application:

```
java -jar ServerMain.jar

```

---

#Implementation

## Client
The ClientMain class manages the connection to a server using TCP and UDP protocols, allowing users to interact via a Command Line Interface (CLI).

**Configuration Parameters:**
The configuration is read through the ConfigurationReader class, which retrieves connection parameters from a configuration file.

**Server Connection:**
The TCP connection is established through the ServerConnection class, which also maintains socket information.

The UDP connection for notification listening is managed by a dedicated thread, NotificationListener.

**CLI (User Interface):**
User interaction takes place through a menu of options displayed in the console.

**Action Execution:**
The Strategy design pattern is used to manage actions based on menu choices, utilizing instances of the ActionStrategy superclass. User input parameters are collected, a packet is constructed if needed, and then sent to the server.

The Strategy pattern is also used to handle server responses.

The client maintains the user's state (logged in or not) to perform checks before executing actions, thus avoiding unnecessary packet transmissions.

**UDP Notifications:**
The NotificationListener thread listens for and manages server notifications.

**Program Termination:**
Before execution ends, the program closes allocated resources, such as the scanner, the TCP socket, and waits for the UDP thread to terminate.

---

**ServerConnection**
This class manages the TCP connection socket and the associated input/output streams. It is also responsible for initiating communication.

It is implemented as a singleton to ensure only one instance exists.

**ServerCommunication**
This class handles communication between the client and server. It is responsible for sending requests to the server in JSON message format and receiving responses, converting them into JSON objects for further processing.

---

**Threads in the Client**
A main thread manages the TCP connection and user interactions, while another thread handles incoming UDP notifications.

**Synchronization**
Main and UDP connection threads synchronize through a monitor on a shared object, syncConsole, to prevent overlapping console prints.

---

## Server

**Thread Management**

- **ScheduledExecutorService**: Backup operations are executed every 10 minutes.
- **ThreadPoolExecutor**: A flexible pool is used to handle client management threads efficiently. The core thread count is twice the machine’s capacity since tasks are I/O bound.
- **NotifyUDP Thread**: A separate thread manages UDP notifications regarding completed orders, triggered by the pool threads.

**Resource Sharing**
The SharedResources class manages shared data among threads. Non-concurrent structures are protected through synchronized blocks to prevent conflicts, e.g., order management.

**Server Termination**
The server ensures proper shutdown of resources (thread pool, backup thread, notification thread, and other utilities like scanners) using the addShutdownHook method when the program closes (e.g., via Ctrl+C).

**Main (Connection Acceptance)**
The main thread utilizes TcpServerHandler to handle client connections, accepting requests and delegating processing to a thread in the pool.

Each thread receives the client’s connected socket and a reference to shared resources.

**Backup Thread**
Every 10 minutes, a backup of registered users and completed orders is stored in permanent memory.

Registered users are loaded into memory at startup, so only updated data needs to be saved. Completed orders are backed up periodically, adding newly completed ones while clearing the queue.

**Notification Thread**
The notification thread checks for new orders to notify. If none are available, it enters a wait state, using the orders-to-notify structure as a monitor.

When an order is completed, pool threads wake the notification thread, which retrieves the recipient's IP and port from a shared structure.

Once a user logs out, they are no longer reachable for notifications.

---

**Client Handler**
Receives a client request and processes it using the Strategy pattern, sending a response packet.

**Server-Side Data Structures:**

- **bidSize, askSize**: Integers used to check if a market order fails due to insufficient demand.
- **registeredUsers**: Hashmap containing <Username, Password>.
- **loggedUsers**: Hashmap containing <Username, ConnectionInfo>, i.e., IP address and port.
- **fulfilledOrders**: Queue ordered by timestamp, storing completed orders for backup.
- **toNotifyOrders**: Orders pending notification, containing <Username, List of Orders>. Username maps to IP/port if logged in.
- **bidLimitOrders**: Unfulfilled limit buy orders.
- **askLimitOrders**: Unfulfilled limit sell orders.
- **bidStopOrders**: Unfulfilled stop buy orders.
- **askStopOrders**: Unfulfilled stop sell orders.

Buy orders are sorted by ascending price, while sell orders are sorted in descending order.

**Synchronization Primitives**

- Monitors (wait/notify) for signaling new fulfilled orders.
- Monitors for synchronizing order operations (Strategy methods).
- Concurrent data structures are used to handle login and registration operations efficiently.

**UDP Port Management**
The first exchanged message between client and server contains the UDP port dynamically allocated for receiving notifications.

---

**Utility Package**
Contains shared operations for client-server communication, such as string-to-JsonObject conversion, date validation (MMMyyyy), and configuration file reading.

---

**PriceHistory Format:**

```
{
  "response": NUMBER;
  "values": [
    {
      "day": DATE(DD-MM-YYYY);
      "openingPrice": NUMBER;
      "closingPrice": NUMBER;
      "minPrice": NUMBER;
      "maxPrice": NUMBER;
    }
  ]
}

```

Response is 101 in case of an error, 100 otherwise.

**Order Management**

- Stop orders convert to market orders when their threshold price is reached.
- If demand is insufficient, the order fails but remains stored as a market order.
- Partially executed orders are stored separately and notified individually.

