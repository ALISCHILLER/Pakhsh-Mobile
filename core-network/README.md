# core-network

Networking stack built on top of Ktor.

## Highlights

- `HttpClientFactory` configures Ktor with retry/backoff, logging, SSL pinning and token refresh.
- `NetworkClient` implements raw/envelope APIs, cache policies, circuit breaking and metadata
  propagation.
- Contracts (`TokenStore`, `AuthOrchestrator`, `NetworkStatusMonitor`) isolate authentication and
  connectivity concerns from platform code.
- Includes lightweight in-memory cache implementations for tests and offline flows.