# ADR-002: Manual Service Locator Dependency Injection

## Status
**APPROVED**

## Context
A project of SurMaya's scale requires robust Dependency Injection (DI) to construct singleton data repositories, database instances, and orchestrating engines. While standard Android applications leverage automatic DI frameworks like Dagger/Hilt, these frameworks introduce heavy annotation processors, complex KSP configurations, slower incremental compile times, and boilerplate binding files that can clutter the workspace and increase build failure rates in cloud containerized builds.

## Decision
We implement a thread-safe, manual **Service Locator** container pattern inside the application context (`com.example.di.ServiceLocator`). 

```kotlin
object ServiceLocator {
    private val lock = Any()

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var singerEngine: ISingerEngine? = null

    @Volatile
    private var singerRepository: SingerRepository? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(lock) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "surmaya_studio.db"
            ).build().also { database = it }
        }
    }

    fun getSingerEngine(): ISingerEngine {
        return singerEngine ?: synchronized(lock) {
            singerEngine ?: AISingerEngine().also { singerEngine = it }
        }
    }

    fun getSingerRepository(context: Context): SingerRepository {
        return singerRepository ?: synchronized(lock) {
            singerRepository ?: SingerRepositoryImpl(
                singerDao = getDatabase(context).singerDao(),
                singerEngine = getSingerEngine()
            ).also { singerRepository = it }
        }
    }
}
```

ViewModels resolve their repositories dynamically by passing factory parameters to their constructors through a manual provider or via standard UI factory initializers.

## Rationale
- **Compilation Speed**: Completely eliminates the Hilt annotation processing compile overhead. Incremental compilation completes in seconds, ensuring extreme development agility.
- **Predictability & Simplicity**: No magic code generation. Singletons are instantiated only when first accessed, and their lifetimes and thread-safety are explicitly visible to developers in a single file.
- **Zero Runtime Overhead**: No reflection, no runtime scanning, and zero impact on application startup metrics.

## Consequences
- **Manual Registration**: Developers must manually define getters and handle instantiation locks in `ServiceLocator` when introducing new data repositories or engine singletons.
- **Testing Advantage**: Simplifies mock injection. Unit tests do not need heavy Dagger-Mock setups; they simply instantiate subject components by manually passing mock repositories to constructors.
