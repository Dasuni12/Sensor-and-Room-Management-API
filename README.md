# Smart Campus Sensor & Room Management API

A RESTful API for managing campus rooms and IoT sensors built with JAX-RS (Jersey) and Grizzly HTTP server.


### Prerequisites
- Java 23 (or JDK 17+)
- Maven 3.6+

### Build & Run
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
cd smart-campus-api

# Build the project
mvn clean package

# Run the server
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

The API will start on `http://localhost:8080/api/v1`

---

##  API Endpoints

### Discovery
```bash
GET /api/v1
```
Returns API metadata and available resources.

### Rooms
```bash
GET    /api/v1/rooms              # Get all rooms
POST   /api/v1/rooms              # Create a room
GET    /api/v1/rooms/{roomId}     # Get specific room
DELETE /api/v1/rooms/{roomId}     # Delete room (if no sensors)
```

### Sensors
```bash
GET    /api/v1/sensors                    # Get all sensors
GET    /api/v1/sensors?type=Temperature   # Filter by type
POST   /api/v1/sensors                    # Create sensor
GET    /api/v1/sensors/{sensorId}         # Get specific sensor
```

### Sensor Readings
```bash
GET    /api/v1/sensors/{sensorId}/readings   # Get reading history
POST   /api/v1/sensors/{sensorId}/readings   # Add new reading
```

---

## Sample curl Commands

### 1. Get API Info
```bash
curl http://localhost:8080/api/v1
```

### 2. Get All Rooms
```bash
curl http://localhost:8080/api/v1/rooms
```

### 3. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"SCI-201","name":"Science Lab","capacity":40}'
```

### 4. Create a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"LIB-301"}'
```

### 5. Add a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":26.7}'
```

### 6. Filter Sensors by Type
```bash
curl "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 7. Delete a Room
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/SCI-201
```

---

##  Report - Question Answers

### Part 1: Service Architecture and Setup

**Q1: What is the lifecycle of a JAX-RS Resource class?**

By default, JAX-RS resource classes follow a per-request lifecycle. This means a new instance is created for every incoming HTTP request and destroyed after the response is sent. This stateless design ensures thread safety but requires careful management of shared data structures like our in-memory DataStore. Since DataStore uses static HashMaps, all resource instances share the same data, but  must ensure thread-safe access if concurrent requests modify the same resources.

**Q2: Why is HATEOAS important?**

HATEOAS (Hypermedia as the Engine of Application State) makes APIs self-documenting by including navigational links in responses. Instead of clients needing separate documentation to discover endpoints, the API itself tells them where to go next. For example, our Discovery endpoint returns `{"resources": {"rooms": "/api/v1/rooms"}}`, allowing clients to programmatically explore the API. This reduces coupling between client and server and makes the API more discoverable.

---

### Part 2: Room Management

**Q3: IDs only vs full objects - what are the trade-offs?**

Returning only IDs reduces bandwidth and response size, which is beneficial for mobile clients or large datasets. However, clients must make additional requests to fetch full details, increasing latency and server load. Returning full objects increases initial response size but provides all necessary data in one request, reducing round trips. The best approach depends on the use case: for dashboards showing many rooms, IDs with selective fetching is better; for detailed views, full objects are more efficient.

**Q4: Is DELETE idempotent?**

Yes, DELETE is idempotent in our implementation. Calling `DELETE /api/v1/rooms/LIB-301` multiple times produces the same result: the first call deletes the room and returns 204 No Content, subsequent calls return 404 Not Found because the room no longer exists. The system state is the same regardless of how many times DELETE is called — the room is gone. This idempotency is a core REST principle and allows clients to safely retry DELETE operations without side effects.

---

### Part 3: Sensor Operations

**Q5: What happens if a client sends wrong content type?**

If a client sends `text/plain` or `application/xml` to an endpoint marked `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS automatically rejects the request with HTTP 415 Unsupported Media Type before our code even runs. This is content negotiation  - JAX-RS checks the Content-Type header against @Consumes and only processes matching requests. This protects our endpoints from receiving malformed data and clearly communicates supported formats to clients.

**Q6: Query param vs path-based filtering — which is better?**

Query parameters (`/sensors?type=CO2&status=ACTIVE`) are superior for filtering because:
1. **Scalability**: Easy to add multiple filters without changing URL structure
2. **Optional**: Filters can be omitted; path-based requires explicit routes for each combination
3. **Clarity**: Clearly separates resource identification (path) from filtering (query)
4. **RESTful**: Path identifies resources; query parameters modify how they're retrieved

Path-based filtering (`/sensors/type/CO2/status/ACTIVE`) quickly becomes unmanageable with multiple optional filters.

---

### Part 4: Sub-Resources

**Q7: Benefits of the Sub-Resource Locator pattern?**

The Sub-Resource Locator pattern delegates nested resource handling to dedicated classes, improving:
1. **Separation of Concerns**: SensorReadingResource handles only reading logic, not mixed with sensor logic
2. **Code Organization**: Prevents massive controller classes; each resource focuses on one entity
3. **Maintainability**: Changes to reading operations don't affect sensor operations
4. **Reusability**: SensorReadingResource can be reused or tested independently
5. **Scalability**: Easy to add deeper nesting (e.g., `/readings/{id}/metadata`) without bloating parent classes

This pattern keeps our API codebase clean and modular as it grows.

---

### Part 5: Error Handling

**Q8: Why is 422 better than 404 for missing roomId reference?**

HTTP 404 means "the endpoint doesn't exist" - but `/api/v1/sensors` does exist. The request body is valid JSON and syntactically correct; the issue is that the `roomId` value references a non-existent room. HTTP 422 Unprocessable Entity means "I understand your request format, but the data has semantic errors." This is more accurate because the client successfully sent JSON to a valid endpoint; the failure is in the business logic validation, not resource addressing. 422 gives clients more precise feedback about what went wrong.

**Q9: Security risks of exposing stack traces?**

Exposing Java stack traces to external clients reveals:
1. **Internal file paths**: Shows exact project structure (`com.smartcampus.resource.SensorResource.java:42`)
2. **Library versions**: Identifies frameworks and dependencies with known vulnerabilities
3. **Code flow**: Shows method call chains, revealing business logic structure
4. **Database details**: May expose SQL queries, table names, or connection strings
5. **Attack surface**: Helps attackers identify injection points or logic flaws

Our GlobalExceptionMapper prevents this by logging errors server-side while returning generic 500 messages to clients.

**Q10: Why use filters for logging instead of manual logs?**

JAX-RS filters provide **cross-cutting concern** implementation:
1. **DRY Principle**: Write logging once, applies to all endpoints automatically
2. **Consistency**: Guaranteed uniform log format across the entire API
3. **Maintainability**: Change logging behavior in one place, not scattered across 20 methods
4. **Completeness**: Can't forget to log an endpoint; filter covers everything
5. **Separation**: Business logic stays clean; logging is handled externally
6. **Toggle-ability**: Comment out `@Provider` to disable all logging instantly

Manual logging in every method leads to duplication, inconsistency, and maintenance nightmares.

---

##  Architecture

- **Framework**: JAX-RS (Jersey 3.1.3)
- **Server**: Grizzly HTTP Server (embedded)
- **JSON Processing**: Jackson
- **Storage**: In-memory HashMaps (no database)
- **Design Pattern**: RESTful resource-based architecture with sub-resource locators

---

## Testing

All endpoints tested using Postman. Error handling verified for:
- 409 Conflict: Deleting room with sensors
- 422 Unprocessable Entity: Creating sensor with non-existent roomId
- 403 Forbidden: Adding reading to MAINTENANCE sensor
- 500 Internal Server Error: Global exception handling


## Author

**Dasuni Samarakoon**


