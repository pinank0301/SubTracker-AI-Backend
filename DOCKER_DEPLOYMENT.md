# SubTracker Microservices — Docker Deployment Guide (AWS EC2)

This guide details how to build, deploy, monitor, and maintain all five Java Spring Boot microservices on a single AWS EC2 Ubuntu instance using Docker Compose.

---

## 1. Required Prerequisites

Before running the stack on your Ubuntu EC2 instance, ensure the following are installed:

### Install Docker Engine & Docker Compose Plugin on Ubuntu:
```bash
# 1. Update system packages
sudo apt update && sudo apt upgrade -y

# 2. Install prerequisites
sudo apt install -y ca-certificates curl gnupg lsb-release

# 3. Add Docker’s official GPG key & repository
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 4. Install Docker Engine and Docker Compose
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 5. Enable and start Docker service
sudo systemctl enable docker
sudo systemctl start docker

# 6. Add your Ubuntu user to the docker group (avoid sudo for docker commands)
sudo usermod -aG docker $USER
newgrp docker

# 7. Verify installation
docker --version
docker compose version
```

### PostgreSQL Database:
Ensure your PostgreSQL instance is running either:
- On the EC2 host itself (e.g. `sudo apt install -y postgresql`) with the 3 databases created: `authdb`, `subscriptiondb`, `notificationdb`.
- Or on an external managed service such as AWS RDS.

---

## 2. Project Docker Structure

```
L3_Case_Study/
├── docker-compose.yml              # Root multi-container orchestration
├── .env.example                    # Environment template with placeholders
├── .dockerignore                   # Root build exclusion rules
├── DOCKER_DEPLOYMENT.md            # This deployment guide
│
├── discovery-server/
│   ├── Dockerfile                  # Multi-stage Maven + Eclipse Temurin 21 JRE
│   ├── .dockerignore
│   └── pom.xml
│
├── api-gateway/
│   ├── Dockerfile                  # Multi-stage Maven + Eclipse Temurin 21 JRE
│   ├── .dockerignore
│   └── pom.xml
│
├── auth-service/
│   ├── Dockerfile                  # Multi-stage Maven + Eclipse Temurin 21 JRE
│   ├── .dockerignore
│   └── pom.xml
│
├── subscription-service/
│   ├── Dockerfile                  # Multi-stage Maven + Eclipse Temurin 21 JRE
│   ├── .dockerignore
│   └── pom.xml
│
└── notification-service/
    ├── Dockerfile                  # Multi-stage Maven + Eclipse Temurin 21 JRE
    ├── .dockerignore
    └── pom.xml
```

---

## 3. How to Build the Docker Images

To compile and package all 5 services using their multi-stage Dockerfiles:

```bash
docker compose build
```

To build in parallel with detailed progress output:
```bash
docker compose build --progress=plain
```

---

## 4. How to Start the Complete Stack

```bash
# 1. Create your local .env file from the template
cp .env.example .env

# 2. Edit .env with your actual passwords and settings
nano .env

# 3. Start all services in detached mode
docker compose up -d
```

`discovery-server` will start first. Once its healthcheck passes (`/actuator/health` returns `UP`), the dependent services (`auth-service`, `subscription-service`, `notification-service`, and `api-gateway`) start automatically.

---

## 5. How to View Logs Across All Services

Stream aggregated logs from all 5 containers:

```bash
docker compose logs -f
```

View the last 100 lines and continue streaming:
```bash
docker compose logs -f --tail=100
```

---

## 6. How to View Logs for a Specific Service

```bash
# API Gateway logs
docker compose logs -f api-gateway

# Discovery Server logs
docker compose logs -f discovery-server

# Auth Service logs
docker compose logs -f auth-service

# Subscription Service logs
docker compose logs -f subscription-service

# Notification Service logs
docker compose logs -f notification-service
```

---

## 7. How to Stop the Stack

Stop and remove all running containers and networks:

```bash
docker compose down
```

To stop containers without deleting them:
```bash
docker compose stop
```

To restart them:
```bash
docker compose start
```

---

## 8. How to Rebuild After Code Changes

When you push new Java code or modify dependencies:

```bash
# Rebuild and restart only the changed containers
docker compose up -d --build

# Or rebuild a single service, e.g. api-gateway:
docker compose up -d --build api-gateway
```

---

## 9. How to Check Running Containers and Health Status

```bash
docker compose ps
```

Expected output:
```
NAME                   IMAGE                                COMMAND                  SERVICE                CREATED          STATUS                    PORTS
api-gateway            l3_case_study-api-gateway            "sh -c 'java $JAVA_O…"   api-gateway            10 seconds ago   Up 8 seconds (healthy)    0.0.0.0:8080->8080/tcp
auth-service           l3_case_study-auth-service           "sh -c 'java $JAVA_O…"   auth-service           12 seconds ago   Up 10 seconds (healthy)   8081/tcp
discovery-server       l3_case_study-discovery-server       "sh -c 'java $JAVA_O…"   discovery-server       30 seconds ago   Up 28 seconds (healthy)   0.0.0.0:8761->8761/tcp
notification-service   l3_case_study-notification-service   "sh -c 'java $JAVA_O…"   notification-service   12 seconds ago   Up 10 seconds (healthy)   8083/tcp
subscription-service   l3_case_study-subscription-service   "sh -c 'java $JAVA_O…"   subscription-service   12 seconds ago   Up 10 seconds (healthy)   8082/tcp
```

---

## 10. Publicly Exposed Ports vs. Internal Ports

| Service | Port | Exposure | Purpose |
|---|---|---|---|
| **api-gateway** | `8080` | **Public** (`8080:8080`) | Main entry point for frontend, mobile, and reverse proxy |
| **discovery-server** | `8761` | Internal (mapped to host for debug) | Eureka dashboard (`http://<EC2-IP>:8761`) |
| **auth-service** | `8081` | **Internal Only** | Reached only through Gateway via `lb://AUTH-SERVICE` |
| **subscription-service** | `8082` | **Internal Only** | Reached only through Gateway via `lb://SUBSCRIPTION-SERVICE` |
| **notification-service** | `8083` | **Internal Only** | Reached only through Gateway via `lb://NOTIFICATION-SERVICE` |

Only port `8080` needs to be open in your AWS EC2 Security Group for client traffic (or port `80`/`443` if using Nginx).

---

## 11. Internal Service Communication Architecture

All 5 services reside on a dedicated private Docker bridge network named `subtracker-network`.

```
Internet / Clients
       │
       ▼ (Port 8080)
┌──────────────────────────────────────────────────────────────┐
│  EC2 Host                                                    │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Docker Network: subtracker-network                      │  │
│  │                                                        │  │
│  │    ┌──────────────────┐                                │  │
│  │    │ discovery-server │ <────── All services register  │  │
│  │    │  (Eureka: 8761)  │         and discover peers     │  │
│  │    └────────┬─────────┘                                │  │
│  │             │                                          │  │
│  │    ┌────────▼─────────┐                                │  │
│  │    │   api-gateway    │ (Exposed :8080)                │  │
│  │    └────────┬─────────┘                                │  │
│  │             │                                          │  │
│  │   ┌─────────┼─────────────────────┐                    │  │
│  │   │ lb://   │ lb://               │ lb://              │  │
│  │   ▼         ▼                     ▼                    │  │
│  │ ┌─────┐   ┌────────────────────┐ ┌──────────────────┐  │  │
│  │ │Auth │   │Subscription Service│ │Notification Svc │  │  │
│  │ │:8081│   │       :8082        │ │      :8083       │  │  │
│  │ └─────┘   └────────────────────┘ └────────┬─────────┘  │  │
│  │                                           │ Feign      │  │
│  │                                           ▼            │  │
│  │                     Calls Auth & Subscription via lb://│  │
│  └────────────────────────────────────────────────────────┘  │
│                                │                             │
│                                │ host.docker.internal:5432   │
│                                ▼                             │
│                  PostgreSQL (authdb, subscriptiondb, etc.)   │
└────────────────────────────────┼─────────────────────────────┘
                                 │ HTTPS
                                 ▼
         Python AI Agent on Render (https://subtracker-ai-agent.onrender.com)
```

---

## 12. Environment Variables Required on EC2

Create your `.env` file from `.env.example`:

| Variable | Description | Example / Recommended Value |
|---|---|---|
| `AUTH_DB_URL` | JDBC URL for Auth DB | `jdbc:postgresql://host.docker.internal:5432/authdb` |
| `SUBSCRIPTION_DB_URL` | JDBC URL for Subscription DB | `jdbc:postgresql://host.docker.internal:5432/subscriptiondb` |
| `NOTIFICATION_DB_URL` | JDBC URL for Notification DB | `jdbc:postgresql://host.docker.internal:5432/notificationdb` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `<your-secure-password>` |
| `JWT_SECRET` | 256-bit secret for JWT signing & verification | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `AI_AGENT_SERVICE_URL` | Production Render URL for Python AI Agent | `https://subtracker-ai-agent.onrender.com` |
| `EUREKA_SERVER_URL` | Internal Eureka discovery URL | `http://discovery-server:8761/eureka/` |
| `SPRING_MAIL_USERNAME` | SMTP Email username | `<your-email>@gmail.com` |
| `SPRING_MAIL_PASSWORD` | SMTP Email App Password | `<your-app-password>` |
| `GATEWAY_PORT` | Port exposed on host | `8080` |
| `JAVA_OPTS` | JVM memory limits per container | `-Xms256m -Xmx384m -XX:+UseG1GC` |

---

## 13. How Eureka Service Discovery Works Inside Docker

1. `discovery-server` starts up and listens on port `8761`.
2. Each client service (`api-gateway`, `auth-service`, `subscription-service`, `notification-service`) connects to:
   ```properties
   eureka.client.service-url.defaultZone=http://discovery-server:8761/eureka/
   ```
3. With `eureka.instance.prefer-ip-address=true`, each service registers its internal Docker container IP address with Eureka.
4. Clients query Eureka for registry updates, caching the IP address of active service instances.

---

## 14. How API Gateway Reaches Internal Services

`api-gateway` uses Spring Cloud Gateway with reactive load balancing (`lb://<SERVICE-NAME>`):

* **Auth requests** (`/api/auth/**`) → routed to `lb://AUTH-SERVICE`
* **Subscription requests** (`/api/subscriptions/**`) → routed to `lb://SUBSCRIPTION-SERVICE`
* **Billing requests** (`/api/billing/**`) → routed to `lb://BILLING-SERVICE`
* **Notification requests** (`/api/notifications/**`) → routed to `lb://NOTIFICATION-SERVICE`

Spring Cloud LoadBalancer automatically resolves the target container IP via Eureka.

---

## 15. How the Java Backend Reaches the Python AI Agent on Render

In `api-gateway/src/main/resources/application.properties`, Route 4 routes AI requests directly to the Render endpoint:

```properties
spring.cloud.gateway.routes[4].id=ai-agent-service
spring.cloud.gateway.routes[4].uri=${AI_AGENT_SERVICE_URL:http://localhost:8000}
spring.cloud.gateway.routes[4].predicates[0]=Path=/api/ai/**
spring.cloud.gateway.routes[4].filters[0].name=CorrelationIdFilter
```

In `docker-compose.yml`, `AI_AGENT_SERVICE_URL` is set to:
```yaml
AI_AGENT_SERVICE_URL: ${AI_AGENT_SERVICE_URL:-https://subtracker-ai-agent.onrender.com}
```

When a user or frontend calls:
`POST http://<EC2-IP>:8080/api/ai/chat`

The API Gateway transparently proxies the request to:
`POST https://subtracker-ai-agent.onrender.com/api/ai/chat`

---

## 16. Optional: Setting up Nginx Reverse Proxy with HTTPS

Later, when you attach a domain and SSL certificate (e.g. via Let's Encrypt / Certbot), point Nginx to the API Gateway:

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
