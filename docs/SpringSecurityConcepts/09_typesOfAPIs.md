# Types of API

There are several other types of APIs based on **communication patterns, design principles, and state management**. Let’s explore the key types:

---

## 🔹 **1. Stateful APIs**
###  Description:
- A **stateful API** maintains the state of the client on the server between requests.
- The server stores information about the client’s session (like user identity, shopping cart, etc.).
- Each request depends on the previous one, and the server remembers the client's context.

###  Example:
- **Session-based authentication** (like using `JSESSIONID` or `PHPSESSID`).
- **WebSocket-based communication** (e.g., chat applications).
- Bank transactions or shopping carts where state matters.

###  When to Use:
- When the server needs to retain user state.  
- Complex multi-step workflows (like checkout).  
- Real-time data exchange (e.g., WebSockets).

---

## 🔹 **2. Stateless APIs**
###  Description:
- A **stateless API** does NOT store client state between requests.
- Each request is self-contained and processed independently.
- Typically used in **RESTful APIs** and **microservices**.

###  Example:
- **JWT-based authentication** – every request carries the token.
- **Microservices** and **cloud-native** architectures.

###  When to Use:
- RESTful services.  
- High scalability and fault tolerance needed.  
- Stateless token-based authentication (OAuth, JWT).

---

## 🔹 **3. RESTful APIs**
###  Description:
- REST (Representational State Transfer) follows specific architectural principles:
    - **Stateless**
    - **Cacheable**
    - **Uniform interface**
    - **Client-server separation**
    - **Layered system**
- Uses standard HTTP methods (`GET`, `POST`, `PUT`, `DELETE`).

###  Example:
```http
GET /users/123
```
Returns:
```json
{
    "id": 123,
    "name": "John Doe",
    "email": "john@example.com"
}
```

###  When to Use:
- Web-based APIs.  
- Microservices and cloud-native applications.  
- When you need a consistent and standardized approach.

---

## 🔹 **4. SOAP APIs** *(Simple Object Access Protocol)*
###  Description:
- XML-based protocol.
- Highly structured and strict messaging format.
- Uses HTTP, SMTP, or TCP as transport protocols.
- Includes built-in security and error handling (WS-Security).

###  Example:
SOAP request to get user info:
```xml
<soap:Envelope>
   <soap:Body>
      <GetUserInfo>
         <UserId>123</UserId>
      </GetUserInfo>
   </soap:Body>
</soap:Envelope>
```

###  When to Use:
- When strict contract and security are needed (e.g., banking).  
- Legacy systems that require SOAP integration.  
- High reliability and ACID compliance.

---

## 🔹 **5. GraphQL APIs**
###  Description:
- A query-based API where the client specifies exactly the data they need.
- Single endpoint (`/graphql`) that supports custom queries.
- Clients can request nested and filtered data in a single request.

###  Example:
Request:
```graphql
{
  user(id: "123") {
    name
    email
    posts {
      title
      comments {
        text
      }
    }
  }
}
```
Response:
```json
{
  "data": {
    "user": {
      "name": "John Doe",
      "email": "john@example.com",
      "posts": [
        {
          "title": "GraphQL Intro",
          "comments": [
            {"text": "Great post!"}
          ]
        }
      ]
    }
  }
}
```

###  When to Use:
- When you need flexible and efficient data retrieval.  
- When clients need to avoid over-fetching or under-fetching data.  
- Real-time applications where data requirements are dynamic.

---

## 🔹 **6. gRPC APIs** *(Google Remote Procedure Call)*
###  Description:
- Uses **Protocol Buffers** (Protobuf) for serialization (binary format).
- Faster and more compact than REST and SOAP.
- Supports bidirectional streaming (client and server).

###  Example:
- Define the service:
```proto
service UserService {
  rpc GetUser (UserRequest) returns (UserResponse);
}
```
- Client request:
```bash
grpcurl -d '{"id":123}' localhost:50051 UserService/GetUser
```

###  When to Use:
- High-performance and low-latency communication.  
- Microservices communicating over HTTP/2.  
- Real-time streaming or large data transfers.

---

## 🔹 **7. WebSocket APIs**
###  Description:
- Provides full-duplex, persistent connections.
- Unlike HTTP, WebSockets allow two-way communication.
- Ideal for real-time applications (e.g., chat, stock updates).

###  Example:
- Client opens a WebSocket connection:
```javascript
const socket = new WebSocket('ws://example.com');
socket.send(JSON.stringify({ type: "subscribe", channel: "chat" }));
```

###  When to Use:
- Real-time, low-latency communication.  
- Event-driven or push-based systems.  
- Online games, chat apps, stock tickers, live streaming.

---

## 🔹 **8. Event-Driven APIs**
###  Description:
- Server pushes events to the client when data changes.
- Often uses **WebSockets**, **Server-Sent Events (SSE)**, or **Kafka**.
- The client subscribes to a stream of events.

###  Example:
- Server-Sent Events:
```javascript
const eventSource = new EventSource('/events');
eventSource.onmessage = (event) => {
  console.log(event.data);
};
```

###  When to Use:
- Real-time data streams.  
- Notification services.  
- IoT systems.

---

## 🔹 **9. RPC APIs** *(Remote Procedure Call)*
###  Description:
- Client calls a function on the server as if it were local.
- Uses JSON-RPC, XML-RPC, or gRPC for communication.

###  Example:
JSON-RPC request:
```json
{
  "jsonrpc": "2.0",
  "method": "getUser",
  "params": { "id": 123 },
  "id": 1
}
```

###  When to Use:
- When low latency is required.  
- Simple and fast remote execution.

---

## 🔹 **10. OpenAPI/Swagger**
###  Description:
- OpenAPI is a specification for building RESTful APIs.
- Swagger is a tool that allows you to generate documentation and client SDKs from an OpenAPI spec.

###  Example:
OpenAPI definition:
```yaml
paths:
  /users/{id}:
    get:
      summary: Get a user
      responses:
        200:
          description: Successful response
```

###  When to Use:
- When you need auto-generated documentation.  
- When you want to simplify client SDK generation.

---

##  **Comparison of API Types**
| Type | Stateful/Stateless | Best For | Complexity | Performance |
|-------|---------------------|----------|------------|-------------|
| **Stateless** | Stateless | RESTful APIs, microservices | Low | High |
| **Stateful** | Stateful | Web apps, real-time apps | Medium | Moderate |
| **REST** | Stateless | Web, mobile apps | Low | High |
| **SOAP** | Stateful | Banking, enterprise | High | Moderate |
| **GraphQL** | Stateless | Data-driven apps | High | High |
| **gRPC** | Stateless | Microservices, streaming | High | Very High |
| **WebSocket** | Stateful | Chat, games | Medium | Very High |
| **RPC** | Stateless | Microservices | Low | High |
| **Event-Driven** | Stateful | Real-time updates | Medium | High |

---

##  **Summary**
-  Use **REST** for web and mobile apps.
-  Use **GraphQL** for flexible data needs.
-  Use **gRPC** for high-performance microservices.
-  Use **WebSockets** for real-time communication.
-  Use **SOAP** for secure, contract-based APIs.
-  Use **Event-Driven** for notifications and updates.

