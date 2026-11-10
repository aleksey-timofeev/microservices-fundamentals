# Test strategy and current coverage

## Purpose and workflow

The root `pom.xml` aggregates the five independent Maven modules: Eureka Server, API Gateway, Resource Service, Resource Processor, and Song Service. Each service retains its own POM and test dependencies. Surefire runs `*Test` unit tests; Failsafe runs the `*IT` integration, component, contract, and E2E tests.

### Default test suite

From the repository root, run:

```shell
mvn clean verify
```

This runs all unit, integration, component, and contract tests, while excluding tests requiring the full application stack. Docker must be available because the Resource Service and Song Service database tests and the Song Service component test use Testcontainers.

### E2E test suite

E2E tests are opt-in. Start the complete stack manually from the repository root; the Maven build does not start or stop Compose:

```shell
docker compose up --build -d
```

The repository-root `.env` must provide a valid `LOCALSTACK_AUTH_TOKEN`. Wait until the stack is ready and the Gateway can route to the services before running the test. Then run:

```shell
mvn -Pe2e clean verify
```

The E2E profile includes the upload journey against the running stack. When the test finishes, stop the stack manually:

```shell
docker compose down
```

Do not add `-v` if you want to retain Compose volumes. The E2E test has no application startup/readiness orchestration; an unavailable stack causes the test to fail. Its bounded polling for the song metadata is part of the asynchronous upload scenario.

Surefire reports are in each module's `target/surefire-reports`; Failsafe reports are in `target/failsafe-reports`.

## Current test inventory

Counts below are test invocations reported by Maven, including parameterized invocations.

| Level | Module | Test class | Current coverage | Invocations |
|---|---|---|---|---:|
| Unit | `resource-service` | `ResourceServiceTest` | Upload validation and persistence/event request behavior; resource retrieval; deletion CSV validation and deletion of existing IDs | 7 |
| Unit | `resource-service` | `ResourceEventListenerTest` | Listener forwards the uploaded resource ID to the publisher | 1 |
| Unit | `resource-service` | `ResourceEventPublisherRetryTest` | Retry on transient Rabbit failure, no retry on permanent failure, and retries exhausted when publication is rejected | 3 |
| Unit | `resource-processor` | `SongMapperTest` | Song request mapping, invalid/null duration handling, and duration rounding/formatting | 16 |
| Unit | `song-service` | `SongServiceTest` | Duplicate metadata conflict and deletion ID validation/filtering | 4 |
| Integration | `resource-service` | `ResourceRepositoryIT` | Existing-ID query against PostgreSQL via Testcontainers | 1 |
| Integration | `resource-processor` | `Mp3ExtractorIT` | Apache Tika extracts title, artist, album, duration, and year from the MP3 fixture | 1 |
| Integration | `song-service` | `SongRepositoryIT` | Existing-ID query against PostgreSQL via Testcontainers | 1 |
| Component | `song-service` | `SongServiceComponentIT` | HTTP POST persists metadata; duplicate POST returns HTTP 409 with the expected error details | 1 |
| Contract | `resource-processor` | `SongClientContractIT` | Real `SongClient`/`RestClient` exchange with a local HTTP server; verifies POST path, content type, request fields, and response decoding | 1 |
| E2E | `resource-processor` | `ResourceUploadE2EIT` | Upload fixture through Gateway; poll the song endpoint and verify extracted metadata through the complete running stack | 1 |

**Totals:** 31 unit, 3 integration, 1 component, 1 contract, and 1 E2E invocation. The default `mvn clean verify` runs 36; `mvn -Pe2e clean verify` runs all 37.

There are no test classes currently in `api-gateway` or `eureka-server`. The planned E2E deletion journey is not yet implemented. There is also no current test that exercises the actual RabbitMQ publication-to-consumption path or verifies Resource Service S3 behavior as an isolated integration/component test. The publisher retry test mocks `StreamBridge`; it does not verify broker routing. The Song Client contract test uses a local HTTP stub, not a running Song Service or a provider-generated contract artifact.

## Architecture and coverage intent

The upload journey is:

`Gateway → Resource Service → S3 → Resource DB → RabbitMQ → Resource Processor → Resource Service (binary retrieval) → MP3 extraction → Song Service → Song DB`

Gateway routes `/resources/**` and `/songs/**` using Eureka. The E2E upload test verifies this complete route with real services and infrastructure; lower-level tests cover selected rules and seams independently.

Test levels in this project mean:

- **Unit:** test a class's local behavior with collaborators isolated where useful.
- **Integration:** verify a real adapter or external seam, such as a repository against PostgreSQL or the extractor against a real MP3 fixture.
- **Component:** drive one service through its API while that service runs with its own real dependencies.
- **Contract:** verify a service client's HTTP/messaging request and response expectations at a boundary. The current Song Client test uses a local HTTP server.
- **E2E:** execute a business journey through the Gateway with the complete real application stack.

Keep the E2E suite small and journey-focused. Add coverage at the narrowest level that proves behavior; do not treat proposed or unimplemented scenarios as existing coverage.
