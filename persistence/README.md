# persistence

Shared domain/data layer for authentication and realtime infrastructure.

## Highlights

- `AuthRepositoryImpl` coordinates login/logout, persists session snapshots, and updates the shared
  `TokenStore`.
- `AuthLocalDataSource` implements the `AuthSessionStore` contract, exposing flows backed by encrypted
  preferences.
- `AuthApi` wraps the unified `RawApi` interface so network behaviour is consistent with other
  features.
- `PakhshModule` wires repositories, use cases, device identifiers, and session storage for injection
  into application modules.