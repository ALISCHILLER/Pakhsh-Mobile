# Improvement Plan

## Dependency and Build Alignment
- Update third-party versions to compatible levels. The dependency catalog mixes very new Compose artifacts with legacy coroutines (`1.3.9`), Room (`2.5.0`), and Ktor (`3.1.2`); this combination is likely to produce build-time or runtime conflicts. Align the stack around current LTS releases (e.g., Coroutines 1.8.x, Room ≥2.6.1, Ktor 3.0.x) and remove duplicate Material dependencies from library modules to shrink the APK size.【F:gradle/dependency.versions.toml†L1-L45】【F:gradle/libs.versions.toml†L1-L40】

## Dependency Injection Hygiene
- Replace the `lazy { Gson() }` binding with a plain singleton instance so `BaseSharedPreferences` receives a `Gson` instead of `Lazy<Gson>`, and provide `FileLoggingTree` with the file path it expects rather than a `FileManager`. This resolves the current type mismatches when the graph is built.【F:core/src/main/java/com/zar/core/di/utilsModule.kt†L16-L38】【F:core/src/main/java/com/zar/core/data/storage/BaseSharedPreferences.kt†L16-L111】
- Restore the `AuthRepository` binding and correct the syntax in `repositoryModule`; `}withOptions {` never compiles, and the missing binding leaves login use cases without a data source. Add `.withOptions { createdAtStart() }` on a new line and point it at an implemented repository instance.【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/di/repositoryModule.kt†L1-L23】

## Network Layer Consistency
- Generalize `NetworkHandler.safeApiCall` so repositories can work with either a wrapped `ApiResponse<T>` or raw DTOs. The current helper forces every caller to emit `ApiResponse<T>` even though convenience methods already call `client.get(url).body()` directly, which is incompatible. Also remove `head(url).body()`—HEAD responses do not expose a body and this will throw at runtime.【F:core/src/main/java/com/zar/core/data/network/handler/NetworkHandler.kt†L36-L94】
- Expand `NetworkStatusMonitor` to reuse the shared `HttpClient` from `HttpClientFactory` (instead of building a new Android client per call) and expose offline caching/interceptors centrally. This avoids duplicated configuration and improves performance on low connectivity.【F:core/src/main/java/com/zar/core/data/network/utils/NetworkStatusMonitor.kt†L1-L83】

## Domain and Repository Contracts
- Define a domain-level `User` entity (and associated mapper) so `AuthRepository` compiles; the interface currently references an undefined type. Fill in `LoginUseCase`/`AuthRepositoryImpl` to return meaningful results instead of stubs.【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/domain/repository/AuthRepository.kt†L1-L12】
- In `PokemonRepositoryImpl`, consider persisting or caching the response via Room to enable offline mode, and surface errors through a unified mapper to keep the UI layer slim.【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/data/repository/PokemonRepositoryImpl.kt†L1-L32】

## Persistence Layer Corrections
- Add proper primary keys and indices to `ProductGroupEntity` and `ProductUnitEntity`. Without `@PrimaryKey` annotations Room will refuse to create the tables. Align DAO queries with the declared table names (e.g., either rename the table to `products` or update queries to `SELECT * FROM "Products"`).【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/data/local/entity/ProductGroupEntity.kt†L1-L16】【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/data/local/entity/ProductUnitEntity.kt†L1-L16】【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/data/local/entity/ProductEntity.kt†L1-L21】【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/data/local/dao/ProductDao.kt†L1-L34】
- Replace `fallbackToDestructiveMigration()` in `BaseDatabase` with explicit migration paths (or make it opt-in) to protect production data from unintended wipes during upgrades.【F:core/src/main/java/com/zar/core/data/database/BaseDatabase.kt†L1-L39】

## Background and Realtime Services
- Refactor `SignalRManager` into a regular scoped class instead of a `ViewModel`, add exponential backoff for reconnection, and remove `blockingAwait()` calls to keep the hub client fully non-blocking.【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/data/signalR/SignalRManager.kt†L1-L119】
- Harden `LiveLocationService`: request runtime permissions before starting updates, inject a real configuration URL rather than the placeholder, and throttle `sendLocationToServer` to avoid flooding the backend with random device IDs.【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/service/LiveLocationService.kt†L1-L74】
- Replace the ad-hoc `CoroutineScope(Dispatchers.IO)` in `MyFirebaseMessagingService` with `lifecycleScope`/`serviceScope` or enqueue a `WorkManager` job so database inserts are cancellable with the service lifecycle.【F:visitApp/src/main/java/com/zar/visitApp/service/firebase/MyFirebaseMessagingService.kt†L1-L83】

## Testing and Observability
- Add integration tests that exercise `NetworkHandler` with mocked Ktor engines and DAO instrumentation tests for the Room layer to catch schema regressions early. Wire `LoggerHelper` so logs can be asserted in tests or piped to Crashlytics in release builds.【F:core/src/main/java/com/zar/core/di/utilsModule.kt†L16-L38】【F:core/src/main/java/com/zar/core/utils/logger/LoggerHelper.kt†L1-L77】

## Productizing the UI
- Move Koin bootstrapping entirely into each Application class and let Compose screens focus on UI state. Build feature slices (e.g., customer list, tour tracking) that consume real use cases once repositories are implemented, replacing the current Pokémon placeholder screen.【F:visitApp/src/main/java/com/zar/visitApp/MainActivity.kt†L1-L60】【F:zarPakhsh/src/main/java/com/zar/zarpakhsh/presentation/screens/PokemonScreen.kt†L1-L120】