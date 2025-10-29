# supervisorApp

Android application for supervisors.

## Highlights

- Provides the supervisor `AppConfig` so the shared modules resolve environment-specific endpoints.
- Boots Koin with the shared core/persistence modules for authentication and networking.
- Relies on `core-ui` for theming and can layer supervisor-only features on top of the shared stack.