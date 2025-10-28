# core-di

Dependency injection entry points for Koin.

## Highlights

- `coreModule` provisions platform services (string provider, logger, dispatchers), networking
  configuration, HTTP clients, and the connectivity monitor.
- `utilsModule` exposes shared singletons such as `Gson`, encrypted `BaseSharedPreferences`,
  validators and logging sinks.
- Intended to be included by every application alongside the persistence module during `startKoin`.