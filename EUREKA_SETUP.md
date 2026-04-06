# Eureka Service Discovery Setup

## What Changed

Your system now uses **Netflix Eureka** for dynamic service discovery instead of hardcoded URLs.

### Old Approach (Hardcoded)
```
api-gateway → http://localhost:8081 (hardcoded)
borrow-service → http://localhost:8081 (hardcoded)
```

### New Approach (Eureka Discovery)
```
api-gateway → [Eureka] → user-service (discovered)
borrow-service → [Eureka] → user-service (discovered)
```

---

## Architecture

```
┌─────────────────────────────────────────┐
│        EUREKA SERVER (port 8761)       │
│      (Service Registry + Discovery)     │
└─────────────────────────────────────────┘
           ▲              ▲
           │ (register)   │ (query)
      ┌────┴─────────────┴──────┐
      │                          │
  ┌───┴────┐              ┌──────┴────┐
  │Services│              │ API Gateway│
  │ Clients│              │            │
  └────────┘              └────────────┘
  - user-service           - Routing to
  - book-service             discovered
  - borrow-service          services
  - fine-service
  - auth-service
  - notification-service
```

---

## Services Added/Modified

### 1. **NEW: Eureka Server** (port 8761)
- Location: `eureka-server/`
- Role: Central service registry and discovery
- No client registration (standalone)

### 2. **Updated: All Microservices** (6 services)
- Now register with Eureka on startup
- Auto-discovery of other services
- Dependencies added: `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-loadbalancer`

### 3. **Updated: API Gateway**
- Old: `uri: http://localhost:8081`
- New: `uri: lb://user-service` (load-balanced discovery)

### 4. **Updated: Service Clients**
- `borrow-service` → auto-discovers `user-service`, `book-service`
- `notification-service` → auto-discovers `user-service`, `borrow-service`
- Uses `@LoadBalanced` RestClient for automatic load balancing

---

## How to Run

### Step 1: Build all services (including eureka-server)
```bash
cd ~/Documents/library2

# Build Eureka Server
mvn -q -DskipTests -f eureka-server/pom.xml compile

# Build all microservices
mvn -q -DskipTests -f user-service/pom.xml compile
mvn -q -DskipTests -f book-service/pom.xml compile
mvn -q -DskipTests -f borrow-service/pom.xml compile
mvn -q -DskipTests -f fine-service/pom.xml compile
mvn -q -DskipTests -f notification-service/pom.xml compile
mvn -q -DskipTests -f auth-service/pom.xml compile
mvn -q -DskipTests -f api-gateway/pom.xml compile
```

### Step 2: Start Eureka Server FIRST (in its own terminal)
```bash
cd ~/Documents/library2
mvn -q -f eureka-server/pom.xml spring-boot:run
```

**Wait 5 seconds** for Eureka Server to start.

### Step 3: Start all other services (in separate terminals)
```bash
# Terminal 2: Auth Service (port 8090)
cd ~/Documents/library2 && mvn -q -f auth-service/pom.xml spring-boot:run

# Terminal 3: API Gateway (port 8080)
cd ~/Documents/library2 && mvn -q -f api-gateway/pom.xml spring-boot:run

# Terminal 4: User Service (port 8081)
cd ~/Documents/library2 && mvn -q -f user-service/pom.xml spring-boot:run

# Terminal 5: Book Service (port 8082)
cd ~/Documents/library2 && mvn -q -f book-service/pom.xml spring-boot:run

# Terminal 6: Borrow Service (port 8083)
cd ~/Documents/library2 && mvn -q -f borrow-service/pom.xml spring-boot:run

# Terminal 7: Fine Service (port 8084)
cd ~/Documents/library2 && mvn -q -f fine-service/pom.xml spring-boot:run

# Terminal 8: Notification Service (port 8085)
cd ~/Documents/library2 && mvn -q -f notification-service/pom.xml spring-boot:run
```

**Wait ~30 seconds** for all services to register with Eureka.

---

## Verify Eureka Registration

### Check Eureka Dashboard
```bash
curl -s http://localhost:8761/eureka/apps | grep -E '<name>|<status>'
```

Expected output:
```
<name>EUREKA-SERVER</name>
<status>UP</status>
<name>USER-SERVICE</name>
<status>UP</status>
<name>BOOK-SERVICE</name>
<status>UP</status>
<name>BORROW-SERVICE</name>
<status>UP</status>
<name>FINE-SERVICE</name>
<status>UP</status>
<name>AUTH-SERVICE</name>
<status>UP</status>
<name>API-GATEWAY</name>
<status>UP</status>
<name>NOTIFICATION-SERVICE</name>
<status>UP</status>
```

---

## Test API Endpoints (Same as Before)

All endpoints work the same, but now go through service discovery:

```bash
# Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"librarian@library.local","password":"password"}' | python3 -m json.tool

# Get users (discovered via user-service)
curl -s -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

## Key Configuration Changes

### eureka-server/application.yml
```yaml
eureka:
  client:
    registerWithEureka: false  # Server doesn't register itself
    fetchRegistry: false        # Server doesn't query itself
```

### All Services: application.yml
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: false
    hostname: localhost
```

### borrow-service/application.yml
```yaml
app:
  clients:
    user-service-url: http://user-service:8081    # Was http://localhost:8081
    book-service-url: http://book-service:8082    # Was http://localhost:8082
```

### notification-service/application.yml
```yaml
app:
  clients:
    user-service-url: http://user-service:8081    # Was http://localhost:8081
    borrow-service-url: http://borrow-service:8083 # Was http://localhost:8083
```

### api-gateway/application.yml
```yaml
routes:
  - id: user-service
    uri: lb://user-service              # Was http://localhost:8081
    predicates:
      - Path=/api/v1/users/**
```

---

## Benefits

✅ **No hardcoded URLs** - Services discover each other dynamically
✅ **Scalability** - Add/remove service instances, Eureka auto-discovers
✅ **High Availability** - Services can be stopped/restarted without breaking others
✅ **Load Balancing** - Automatic load balancing across multiple instances
✅ **Health Monitoring** - Eureka tracks which services are UP/DOWN
✅ **Production Ready** - Industry standard service discovery pattern

---

## Troubleshooting

### Problem: Services not registering with Eureka

**Check 1:** Eureka Server is running on port 8761
```bash
curl http://localhost:8761/eureka/apps
```

**Check 2:** Each service has correct Eureka URL in application.yml
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

**Check 3:** Service logs show registration
```
com.netflix.discovery.DiscoveryClient - Registering application ... with EUREKA
```

### Problem: Gateway can't route to services

**Check:** API Gateway is also registered with Eureka
```bash
curl -s http://localhost:8761/eureka/apps/API-GATEWAY | grep -E '<status>'
```

Expected: `<status>UP</status>`

### Problem: "Connection refused" errors

Make sure services are started in this order:
1. Eureka Server ✅
2. Auth Service ✅
3. API Gateway ✅
4. Other services ✅

---

## Next Steps

### Optional: Production Hardening
1. Set `eureka.client.healthcheck.enabled: true` for health checks
2. Configure replica Eureka servers for high availability
3. Set `eureka.client.leaseRenewalIntervalInSeconds: 30` (default 30s)
4. Use `eureka.client.leaseExpirationDurationInSeconds: 90` to mark unhealthy

### Optional: Docker Setup
- Eureka Server can run in Docker on network `library-network`
- Services register using `http://eureka-server:8761/eureka/` (Docker hostname)

