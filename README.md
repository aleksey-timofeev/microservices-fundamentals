### Installation

#### LocalStack token

LocalStack requires a Personal Auth Token for usage.

1. Create `.localstack.env` from `.localstack.env.example` in the root of the project.
2. Sign up at https://app.localstack.cloud, choose AWS and non-commercial usage, and copy your Personal Auth Token from the Getting Started page or Settings > Auth tokens.
3. Insert the token into `.localstack.env` and save the file.

#### Environment configuration

1. Create `.env` from `.env.example` in the root of the project.
2. Review the configuration values and update them if necessary.
3. Keep your actual credentials and tokens out of version control.

### Local deployment

In local deployment mode, infrastructure components run in Docker containers, while microservices run directly on the local machine.

1. Start the infrastructure components using Docker Compose:

   ```bash
   docker compose up -d resource-db song-db rabbitmq localstack
   ```

2. Configure the following environment variables for both `resource-service` and `resource-processor` in their respective run configurations:

   ```text
   SPRING_RABBITMQ_HOST=localhost
   SPRING_RABBITMQ_PORT=5672
   SPRING_RABBITMQ_USERNAME=app_user
   SPRING_RABBITMQ_PASSWORD=app_password
   ```

3. Start the microservices in the following order:
   - Eureka Server
   - API Gateway
   - Song Service
   - Resource Service
   - Resource Processor

4. Verify that all microservices are registered in Eureka and that their health endpoints return `UP`.

The remaining configuration uses the default values defined in the application configuration files.

### Full Docker deployment

In full Docker deployment mode, all microservices and infrastructure components run as Docker Compose services.

1. Make sure `.env` and `.localstack.env` are configured.
2. Build and start all services:

   ```bash
   docker compose up -d --build
   ```

3. Check container status:

   ```bash
   docker compose ps
   ```

4. Verify that all application health endpoints return `UP`.

Docker Compose automatically loads configuration values from `.env` and `.localstack.env`. No additional IDE configuration or manual application profile switching is required.

### Stopping the environment

To stop all containers:

```bash
docker compose down
```