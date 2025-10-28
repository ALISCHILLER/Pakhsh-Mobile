# Pakhsh-Mobile Architecture

This project is organised as a multi-module Android codebase so that shared infrastructure can be
reused across the three role-based applications (`visitApp`, `supervisorApp`, `distributeApp`).

## Layers

| Layer | Modules | Responsibilities |
|-------|---------|------------------|
| Core foundation | `core-common`, `core-network`, `core-storage`, `core-di`, `core-ui`, `core-validation`, `core-logging`, `core-flags`, `core-media` | Platform-agnostic primitives (result wrappers, pagination, error types), the HTTP stack, encrypted storage, DI wiring, shared UI components and other cross-cutting utilities. |
| Persistence | `persistence` | Domain/data layer for authentication, session management, and realtime features shared between applications. Exposes repositories, use cases, DTOs, local caches and configuration factories. |
| Applications | `visitApp`, `supervisorApp`, `distributeApp` | Feature surfaces for each flavour. Each app supplies its own `AppConfig`, UI, and any flavour-specific persistence (e.g. the visit database) while relying on the shared modules for login, networking, storage and DI. |

## Dependency Flow

```
Applications → persistence → core-* modules
```

* Applications depend on the persistence layer for login/session flows and on specific core modules
  for UI, DI and utilities.
* The persistence layer depends only on the `core-*` modules.
* Core modules are platform-aware only where necessary (`core-di`, `core-ui`) and otherwise keep
  shared logic Android-agnostic (`core-common`, `core-network`, `core-storage`).

## Dependency Injection

Koin is used for dependency injection. Each app starts Koin in its `Application` class by wiring:

```kotlin
startKoin {
    androidContext(app)
    modules(appConfigModule, coreModule, utilsModule, persistenceModule, appSpecificModules)
}
```

* `coreModule`/`utilsModule` provide platform services, logging, HTTP client configuration, and the
  shared coroutine/time helpers.
* `persistenceModule` registers session storage, token handling, auth APIs/repositories, and
  use-cases.
* Each application contributes an `AppConfig` implementation and any flavour-specific modules
  (for example, the visit app’s Room database module).

## Networking

`core-network` contains the canonical HTTP client (`NetworkClient`) that supports:

* Unified `Outcome`/`Meta` responses for raw and envelope APIs.
* Cache policies with ETag awareness, offline fallbacks, and metadata propagation.
* Circuit breaking, retry configuration, and optional token refresh orchestration.

`HttpClientFactory` builds the Ktor `HttpClient` with standard logging, timeout, retry and
authentication interceptors. Token refresh integrates through the `TokenStore` / `AuthOrchestrator`
contracts so applications can supply custom refresh logic when needed.

## Storage

`core-storage` offers encrypted key-value storage via `BaseSharedPreferences` and implements an
encrypted `TokenStore`. The persistence layer builds on top with `SessionPrefs` and
`AuthLocalDataSource` to keep session snapshots in sync with token storage.

## Testing Support

Unit tests cover key infrastructure:

* `core-network` – ensures the HTTP client returns metadata and serves cached responses when offline.
* `persistence` – verifies session preferences and the authentication repository behaviour with fake
  stores and APIs.

Test utilities rely on Robolectric for Android-dependent code (`SessionPrefs`) and on Ktor’s mock
engine for HTTP client behaviour.

## Adding Features

1. Extend `persistence` with new repositories/use cases and expose them through `persistenceModule`.
2. Consume the new dependencies in the relevant application module by wiring them via Koin.
3. Reuse `core-*` helpers to avoid duplicating networking, storage, logging or validation logic.

This structure keeps the shared infrastructure centralised while allowing each application flavour to
layer its specific UI and configuration on top.