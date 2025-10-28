# core-storage

Encrypted key-value storage utilities used by the persistence and network layers.

## Highlights

- `BaseSharedPreferences` wraps Android `SharedPreferences` with optional Google Tink encryption and
  convenience APIs for objects and primitives.
- `TokenStoreEncrypted` implements the shared `TokenStore` contract so HTTP clients can read/write
  bearer tokens securely.
- Ships with Gson so modules can serialise complex payloads without duplicating configuration.