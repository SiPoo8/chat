# Java Chat Application

A simple multi-client chat application written in **Java** using `Socket` programming.  
This project includes a **server** and a **client** that can communicate in real-time over a local network (or localhost).

---

## 📝 Features

- Multi-client support using threads.
- Broadcast messages to all connected clients.
- Custom nickname for each client.
- Commands:
    - `/name newname` – change your nickname.
    - `/quit` – leave the chat gracefully.
- Easy-to-run console-based client and server.

---

## 💻 Project Structure


- **Server.java** – Handles incoming connections, broadcasts messages, and manages connected clients.
- **Client.java** – Connects to the server, sends messages, and displays messages from other users.

---

## ⚙️ Requirements

- Java JDK 17 or higher
- IDE (optional) such as IntelliJ IDEA, Eclipse, or VS Code
- Terminal/console to run the application

---

##  How to Run

### 1️ Start the Server

```bash
javac src/Server.java
java src/Server


You should see:

Server started on port 9999...



Open a new terminal (for each client) and run:

javac src/Client.java
java src/Client


You should see:

Connected to the chat server!

3️⃣ Chat Commands

Send message: Type your message and press Enter

Change nickname: /name newNickname

Quit chat: /quit

All messages are broadcast to every connected client.

🔧 Notes

Default server port is 9999. If you get Address already in use, either:

Change the port in Server.java and Client.java

Close any process using port 9999

Start the server before connecting clients.










