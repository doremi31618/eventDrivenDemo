# Event Driven Demo

This is a sample Java Spring Boot project demonstrating event-driven architecture using Kafka and RabbitMQ.

## Prerequisites
- **Windows 10/11 with WSL2 enabled**
- **Docker Desktop** (with WSL2 integration enabled)
- **Ubuntu 22.04 installed in WSL**

## Steps to Start Kafka and RabbitMQ

1. **Open WSL Terminal (Ubuntu 22.04)**
    ```bash
    wsl -d Ubuntu-22.04
    ```

2. **Navigate to the project directory where `docker-compose.yml` is located**
    ```bash
    cd /path/to/your/project
    ```

3. **Start the containers**
    ```bash
    docker compose up -d
    ```

4. **Verify that Kafka and RabbitMQ are running**
    ```bash
    docker ps
    ```

## Port Mapping
| Service     | Container Port | Host Port |
|-------------|----------------|-----------|
| Kafka       | 9092            | 29092     |
| RabbitMQ    | 5672            | 5672      |
| RabbitMQ UI | 15672           | 15672     |

## Additional Notes
- Kafka inside container uses **9092** for Docker internal network communication.
- Kafka is exposed to the host machine via **29092**.
- RabbitMQ UI can be accessed at: [http://localhost:15672](http://localhost:15672)

## Useful Commands
- **Stop Containers**
    ```bash
    docker compose down
    ```
- **View Logs**
    ```bash
    docker compose logs -f
    ```
## Build and Run Project
### Build the project using Maven Wrapper:

#### Mac/Linux:

```bash
./mvnw clean package
```

Windows (in PowerShell or CMD):
```commandline
mvnw.cmd clean package
```

#### Run the application:

```bash
java -jar target/eventdrivendemo.jar
```

### Useful Commands
#### Stop Containers

```bash
docker compose down
```

#### View Logs

```bash
docker compose logs -f
```

## Troubleshooting
- Make sure Docker Desktop is running.
- Ensure WSL2 integration is enabled in Docker Desktop settings.
- If ports conflict, adjust them in `docker-compose.yml`.