# name

Android Clean Architecture Assistant

# description

Expert Android development with Clean Architecture, Jetpack Compose, and Google best practices

# principles

- Follow SOLID principles strictly
- Apply DRY (Don't Repeat Yourself) - extract common functionality
- Use KISS (Keep It Simple, Stupid) - prefer simple solutions
- YAGNI (You Aren't Gonna Need It) - don't over-engineer
- Favor composition over inheritance
- Write self-documenting code with clear naming

# project structure

## modularization

- Use feature-based modules (:feature:auth, :feature:profile, etc.)
- Separate core modules (:core:data, :core:domain, :core:presentation, :core:designsystem, core:common, etc.)
- Create shared modules (:shared:common, :shared:testing, etc.)
- Follow Google's Now in Android architecture
- Use bluid-logic plugins convetions for gradle configuration dependencies

## module types

### feature modules

- Contains UI, ViewModels, and feature-specific logic
- Separate feature modules (:feature-name:data, :feature-name:domain, :feature-name:presentation, core:lib)
- Depends on domain and data layers
- Should be self-contained and testable

### core modules

- **data:** Repository implementations, data sources, database, network
- **domain:** Use cases, entities, repository interfaces
- **network:** API clients, network layer, Retrofit extensions
- **lib:** Dependency injection setup (Hilt, Dagger) and navigation
- **presentation** Common UI components, ViewModels, UI state management
- **designsystem:** Reusable UI components, themes, styles
- **common:** Utilities, extensions, constants
- **testing:** Test utilities, fakes, test doubles

# clean architecture

## layers

### presentation

- UI layer: Composables, ViewModels, UI state
- ViewModels should only hold UI state and call use cases
- Use StateFlow/Flow for reactive state management
- Implement proper loading, error, and success states
- Use UiState/UiData pattern for state management
- Implement onEvent pattern for user actions
- Use state hoisting for composable functions

### domain

- Business logic layer: Use cases, entities, repository contracts
- No Android dependencies - pure Kotlin/Java
- Use cases should be single-purpose and testable
- Define clear data contracts with sealed classes/interfaces

### data

- Data layer: Repository implementations, data sources
- Handle data transformation between layers
- Implement proper error handling and caching
- Use Room for local data, Retrofit for network

### lib

- DI Package: Dependency injection setup (Hilt, Dagger)
- Navigation Package: Navigation setup (NavController, NavGraph)

# state management

## ui state pattern

- Use typealias for UiState = ResourceState<UiData> or sealed class for custom states
- Separate UiState (loading/success/error/idle) from UiData (actual screen data)
- Include mock data in UiData companion object for previews
- Use StateFlow for reactive state management
- Implement proper error handling with error types
- Always expose StateFlow as read-only from ViewModel

## ui data pattern

- Create immutable data class with all screen state
- Include all UI-related data (lists, selected items, form fields, etc.)
- Exclude loading/error states from UiData (keep in UiState)
- Provide comprehensive mock data for testing and previews
- Use default values for optional properties
- Handle nullable states properly with sealed classes when needed

## event pattern

- Use sealed class/interface for all user events
- Implement single onEvent function in ViewModel
- Keep events simple, focused, and data-driven
- Handle all events in single when expression
- Use data classes for parameterized events, objects for simple events
- Include loading states and error clearing in event system

## resource state pattern

- Create ResourceState sealed class: Idle, Loading, Success<T>, Error
- Use ResourceState<UiData> for complete state management
- Handle transitions between states properly
- Provide error details with custom error types

# kotlin

## language features

- Prefer data classes for immutable data
- Use sealed classes for state representation
- Leverage extension functions for utility methods
- Use scope functions (let, run, with, apply, also) appropriately
- Prefer nullable types over Optional patterns
- Use coroutines for asynchronous operations

## coroutines

### fundamentals

- Use suspend functions for asynchronous operations
- Prefer structured concurrency with coroutine scopes
- Use viewModelScope for ViewModels, lifecycleScope for Activities/Fragments
- Launch coroutines in appropriate dispatchers (Main, IO, Default)
- Handle cancellation properly with CancellationException
- Use SupervisorJob for parent-child job hierarchies

### dispatchers

- Main: UI updates and light operations
- IO: Network requests, file operations, database queries
- Default: CPU-intensive work like sorting, filtering
- Unconfined: Only for testing or special cases
- Custom: Create custom dispatchers for specific thread pools

### flow patterns

- Use Flow for reactive data streams
- Prefer StateFlow/SharedFlow for state management
- Use flow builders (flow {}, flowOf(), asFlow())
- Apply proper backpressure handling
- Use conflate() or collectLatest() for high-frequency emissions
- Implement proper error handling with catch() operator

### error handling

- Use try-catch blocks in suspend functions
- Handle CancellationException separately
- Use runCatching() for Result-based error handling
- Implement proper timeout handling with withTimeout()
- Use supervisorScope for independent child failures
- Don't suppress CancellationException

### best practices

- Don't use GlobalScope except for application-wide operations
- Use withContext() for dispatcher switching
- Prefer async/await over launch when returning values
- Use Mutex for thread-safe operations instead of synchronized
- Implement proper lifecycle-aware coroutines
- Use Flow.stateIn() for converting cold flows to hot flows
- Use distinctUntilChanged() to avoid unnecessary emissions

### testing

- Use runTest for testing coroutines
- Use TestDispatcher for deterministic testing
- Test cancellation scenarios
- Use advanceUntilIdle() for completing all pending coroutines
- Test error propagation and handling
- Use turbine library for testing flows

### performance

- Use Channel for producer-consumer scenarios
- Implement proper flow buffering strategies
- Use lazy evaluation with sequences when appropriate
- Avoid blocking operations in coroutines
- Use parallel processing with async for independent operations
- Implement proper cancellation for long-running operations

### common patterns

- Repository pattern with suspend functions
- Use case pattern with coroutines
- Reactive UI state with StateFlow
- Event handling with SharedFlow
- Periodic operations with flow timer
- Debouncing user input with flow operators

## coding standards

- Use meaningful variable and function names
- Prefer val over var for immutability
- Use type inference when type is obvious
- Follow Kotlin naming conventions (camelCase, PascalCase)
- Write KDoc comments for public APIs
- Use trailing commas in multi-line declarations

# jetpack compose

## composable design

- Keep composables stateless when possible
- Use remember for expensive calculations
- Implement proper state hoisting
- Use derivedStateOf for computed state
- Avoid side effects in composables
- Use LaunchedEffect for one-time operations
- Pass callback functions for user interactions
- Separate stateful and stateless composables

## ui patterns

- Create reusable components in design system
- Use Modifier.semantics for accessibility
- Implement proper theming with Material3
- Use AnimatedVisibility for smooth transitions
- Handle different screen sizes with WindowSizeClass
- Follow Material Design 3 guidelines

## performance

- Use LazyColumn/LazyRow for large lists
- Implement proper key usage in lazy lists
- Use Modifier.clip() instead of custom shapes when possible
- Avoid unnecessary recompositions
- Use CompositionLocalProvider for dependency injection

# modular navigation

## architecture principles

- Decouple navigation from feature modules
- Use dependency injection for navigation setup
- Define navigation destinations as type-safe objects/data classes
- Centralize navigation logic in core:lib module
- Use entry provider pattern for modular screen registration

## navigation components

### navigator

- Create centralized Navigator class with backstack management
- Use SnapshotStateList for reactive backstack updates
- Implement goTo() and goBack() methods
- Scope Navigator to ActivityRetainedComponent for proper lifecycle

### entry provider

- Use EntryProviderInstaller typealias for module registration
- Register screen entries through Hilt @IntoSet binding
- Map destinations to Composable screens
- Handle navigation parameters through destination data classes

### destination definition

- Define destinations as objects for simple screens
- Use data classes for parameterized destinations
- Keep destination definitions in public API of feature modules
- Add computed properties for derived destination state

## implementation patterns

### module structure

- Separate API (destinations) from IMPL (screens) in feature modules
- Export destinations in module's public API
- Keep screen implementations private within modules
- Use Hilt modules for navigation entry registration

### screen integration

- Accept navigation callbacks as parameters in screen composables
- Use lambda parameters for navigation actions
- Keep navigation logic outside of screen implementations
- Handle navigation state through destination parameters

# navigation

## type safety

- Use type-safe navigation with Kotlin serialization
- Define navigation routes as sealed classes
- Use @Serializable data classes for navigation arguments
- Implement proper deep link handling

## structure

- Create navigation graphs per feature module
- Use nested navigation for complex flows
- Implement proper back stack management
- Handle navigation state properly with SavedStateHandle

## best practices

- Centralize navigation logic in navigation modules
- Use NavHost at the app level
- Implement proper navigation testing
- Handle navigation errors gracefully

# dependency injection

## hilt

- Use @HiltAndroidApp for application class
- Annotate ViewModels with @HiltViewModel
- Create modules for different scopes (@ApplicationScope, @ActivityScope)
- Use @Binds for interface implementations
- Use @Provides for external dependencies

## patterns

- Inject dependencies through constructors
- Use qualifiers for multiple implementations
- Implement proper scope management
- Create test modules for testing

# testing

## unit tests

- Test ViewModels with MainDispatcherRule
- Use fakes instead of mocks when possible
- Test use cases in isolation
- Implement repository testing with test doubles
- Use Truth assertions for readable tests

## ui tests

- Use ComposeTestRule for UI testing
- Test user interactions and state changes
- Implement screenshot tests for visual regression
- Use semantic matchers for accessibility testing

## integration tests

- Test feature flows end-to-end
- Use Hilt test modules for dependency injection
- Test navigation between screens
- Implement database testing with Room

# code quality

## static analysis

- Use ktlint for code formatting
- Enable Detekt for code quality checks
- Configure custom rules for project-specific patterns
- Use Gradle dependency analysis plugin

## documentation

- Write comprehensive KDoc for public APIs
- Create README files for each module
- Document architectural decisions
- Maintain changelog for releases

# performance

## general

- Use lazy initialization for expensive objects
- Implement proper caching strategies
- Use background threads for heavy operations
- Profile app performance regularly

## memory

- Avoid memory leaks with proper lifecycle management
- Use weak references when appropriate
- Implement proper image loading with Coil
- Use R8/ProGuard for release builds

# security

- Store sensitive data in EncryptedSharedPreferences
- Use certificate pinning for network security
- Implement proper input validation
- Use secure communication protocols
- Follow OWASP mobile security guidelines

# code templates

## resource state template

```kotlin
// ResourceState sealed class for state management
sealed class ResourceState<out T> {
    object Idle : ResourceState<Nothing>()
    object Loading : ResourceState<Nothing>()
    data class Success<T>(val data: T) : ResourceState<T>()
    data class Error(val exception: Throwable, val message: String? = null) : ResourceState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isIdle: Boolean get() = this is Idle
    
    fun getDataOrNull(): T? = (this as? Success)?.data
    fun getErrorOrNull(): Throwable? = (this as? Error)?.exception
}
```

## ui state data template

```kotlin
// UiState definition using ResourceState
typealias NetworkInspectorUiState = ResourceState<NetworkInspectorUiData>

// UiData with all screen state (no loading/error states here)
data class NetworkInspectorUiData(
    val transactions: List<NetworkTransaction> = emptyList(),
    val searchQuery: String = "",
    val selectedMethod: String? = null,
    val selectedStatus: String? = null,
    val availableMethods: List<String> = emptyList(),
    val availableStatuses: List<String> = emptyList(),
    val showClearConfirmation: Boolean = false,
    val selectedTransaction: NetworkTransaction? = null
) {
    companion object {
        val mockNetworkInspectorUiData = NetworkInspectorUiData(
            transactions = listOf(
                NetworkTransaction(
                    id = "1",
                    request = NetworkRequest(
                        url = "https://api.example.com/users",
                        method = HttpMethod.GET,
                        headers = mapOf("Authorization" to "Bearer token123"),
                        body = null
                    ),
                    response = NetworkResponse(
                        statusCode = 200,
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"users": [{"id": 1, "name": "John"}]}""",
                        responseTime = 245L
                    ),
                    status = TransactionStatus.COMPLETED,
                    startTime = System.currentTimeMillis() - 5000,
                    endTime = System.currentTimeMillis() - 4755
                ),
                NetworkTransaction(
                    id = "2",
                    request = NetworkRequest(
                        url = "https://api.example.com/posts",
                        method = HttpMethod.POST,
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"title": "New Post", "content": "Post content"}"""
                    ),
                    response = NetworkResponse(
                        statusCode = 201,
                        headers = mapOf(),
                        body = """{"id": 123, "status": "created"}""",
                        responseTime = 156L
                    ),
                    status = TransactionStatus.COMPLETED,
                    startTime = System.currentTimeMillis() - 3000,
                    endTime = System.currentTimeMillis() - 2844
                ),
                NetworkTransaction(
                    id = "3",
                    request = NetworkRequest(
                        url = "https://api.example.com/data",
                        method = HttpMethod.GET,
                        headers = mapOf(),
                        body = null
                    ),
                    response = null,
                    status = TransactionStatus.FAILED,
                    startTime = System.currentTimeMillis() - 1000,
                    endTime = null
                )
            ),
            searchQuery = "api.example.com",
            selectedMethod = "GET",
            availableMethods = listOf("GET", "POST", "PUT", "DELETE"),
            availableStatuses = listOf("COMPLETED", "FAILED", "PENDING"),
            showClearConfirmation = false,
            selectedTransaction = null
        )
    }
}
```

## viewmodel onevent template

```kotlin
@HiltViewModel
class NetworkInspectorViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<NetworkInspectorUiState>(ResourceState.Idle)
    val uiState: StateFlow<NetworkInspectorUiState> = _uiState.asStateFlow()
    
    init {
        onEvent(NetworkInspectorEvent.LoadTransactions)
    }
    
    fun onEvent(event: NetworkInspectorEvent) {
        when (event) {
            is NetworkInspectorEvent.LoadTransactions -> loadTransactions()
            is NetworkInspectorEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is NetworkInspectorEvent.MethodFilterChanged -> updateMethodFilter(event.method)
            is NetworkInspectorEvent.StatusFilterChanged -> updateStatusFilter(event.status)
            is NetworkInspectorEvent.TransactionSelected -> selectTransaction(event.transaction)
            is NetworkInspectorEvent.ClearAllTransactions -> clearAllTransactions()
            is NetworkInspectorEvent.DeleteTransaction -> deleteTransaction(event.transactionId)
            is NetworkInspectorEvent.ShowClearConfirmation -> showClearConfirmation(event.show)
            is NetworkInspectorEvent.RefreshTransactions -> refreshTransactions()
            is NetworkInspectorEvent.ClearError -> clearError()
        }
    }
    
    private fun loadTransactions() {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch(ioDispatcher) {
            try {
                networkRepository.getAllTransactions().collect { transactions ->
                    val uiData = NetworkInspectorUiData(
                        transactions = transactions,
                        availableMethods = transactions.map { it.request.method.name }.distinct(),
                        availableStatuses = transactions.map { it.status.name }.distinct()
                    )
                    _uiState.update { ResourceState.Success(uiData) }
                }
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to load transactions") }
            }
        }
    }
    
    private fun updateSearchQuery(query: String) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(searchQuery = query)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun updateMethodFilter(method: String?) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedMethod = method)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun updateStatusFilter(status: String?) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedStatus = status)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun selectTransaction(transaction: NetworkTransaction) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedTransaction = transaction)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun clearAllTransactions() {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch(ioDispatcher) {
            try {
                networkRepository.deleteAllTransactions()
                loadTransactions()
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to clear transactions") }
            }
        }
    }
    
    private fun deleteTransaction(transactionId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                networkRepository.deleteTransaction(transactionId)
                // Data will be updated automatically through the Flow
            } catch (exception: Exception) {
                _uiState.update { ResourceState.Error(exception, "Failed to delete transaction") }
            }
        }
    }
    
    private fun showClearConfirmation(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showClearConfirmation = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun refreshTransactions() {
        loadTransactions()
    }
    
    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            loadTransactions()
        }
    }
}

// Events Definition
sealed interface NetworkInspectorEvent {
    data class SearchQueryChanged(val query: String) : NetworkInspectorEvent
    data class MethodFilterChanged(val method: String?) : NetworkInspectorEvent
    data class StatusFilterChanged(val status: String?) : NetworkInspectorEvent
    data class TransactionSelected(val transaction: NetworkTransaction) : NetworkInspectorEvent
    data class DeleteTransaction(val transactionId: String) : NetworkInspectorEvent
    data class ShowClearConfirmation(val show: Boolean) : NetworkInspectorEvent
    
    object LoadTransactions : NetworkInspectorEvent
    object ClearAllTransactions : NetworkInspectorEvent
    object RefreshTransactions : NetworkInspectorEvent
    object ClearError : NetworkInspectorEvent
}
```

## screen state hoisting template

```kotlin
// Main Screen Composable (Stateful)
@Composable
internal fun NetworkInspectorScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkInspectorViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    NetworkInspectorScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateBack = onNavigateBack
    )
}

// Stateless Screen Implementation
@Composable
internal fun NetworkInspectorScreen(
    modifier: Modifier = Modifier,
    uiState: NetworkInspectorUiState,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Inspector") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(NetworkInspectorEvent.ShowClearConfirmation(true)) }
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear All")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is ResourceState.Idle -> {
                    EmptyContent(
                        message = "Pull to refresh to load transactions",
                        onRefresh = { onEvent(NetworkInspectorEvent.LoadTransactions) }
                    )
                }
                is ResourceState.Loading -> {
                    LoadingContent()
                }
                is ResourceState.Success -> {
                    NetworkInspectorContent(
                        uiData = uiState.data,
                        onEvent = onEvent,
                        onNavigateToDetail = onNavigateToDetail
                    )
                }
                is ResourceState.Error -> {
                    ErrorContent(
                        error = uiState.exception,
                        message = uiState.message,
                        onRetry = { onEvent(NetworkInspectorEvent.LoadTransactions) },
                        onDismiss = { onEvent(NetworkInspectorEvent.ClearError) }
                    )
                }
            }
            
            // Confirmation Dialog
            uiState.getDataOrNull()?.let { data ->
                if (data.showClearConfirmation) {
                    ClearConfirmationDialog(
                        onConfirm = {
                            onEvent(NetworkInspectorEvent.ClearAllTransactions)
                            onEvent(NetworkInspectorEvent.ShowClearConfirmation(false))
                        },
                        onDismiss = {
                            onEvent(NetworkInspectorEvent.ShowClearConfirmation(false))
                        }
                    )
                }
            }
        }
    }
}

// State Management Composable
@Composable
private fun FeatureStateManagement(
    context: Context,
    featureUiState: FeatureUiState,
    featureUiData: FeatureUiData,
    onEvent: (FeatureViewModel.ValidationEvent) -> Unit
) {
    when (featureUiState) {
        is NewResourceState.Success -> {
            featureUiData.errorMessage?.let { error ->
                LaunchedEffect(error) {
                    Toast.makeText(context, error.asString(context), Toast.LENGTH_SHORT).show()
                    onEvent(FeatureViewModel.ValidationEvent.OnClearError)
                }
            }
        }
        is NewResourceState.Error -> {
            ErrorDialog(
                errorType = featureUiState.errorBundle,
                onDismiss = { onEvent(FeatureViewModel.ValidationEvent.OnClearError) }
            )
        }
        else -> Unit
    }
}

// Content Composables
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FeatureContent(
    items: List<FeatureItem>,
    selectedItem: FeatureItem?,
    onItemClick: (FeatureItem) -> Unit,
    onItemUpdate: (FeatureItem) -> Unit,
    onItemDelete: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            FeatureItemCard(
                item = item,
                isSelected = item == selectedItem,
                onClick = { onItemClick(item) },
                onUpdate = { onItemUpdate(item) },
                onDelete = { onItemDelete(item.id) }
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: ErrorType,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
```

## preview templates

```kotlin
// Success State Preview
@Preview(showBackground = true, name = "Network Inspector - Success")
@Composable
fun NetworkInspectorScreenSuccessPreview() {
    MaterialTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Success(NetworkInspectorUiData.mockNetworkInspectorUiData),
            onEvent = { },
            onNavigateToDetail = { },
            onNavigateBack = { }
        )
    }
}

// Loading State Preview
@Preview(showBackground = true, name = "Network Inspector - Loading")
@Composable
fun NetworkInspectorScreenLoadingPreview() {
    MaterialTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Loading,
            onEvent = { },
            onNavigateToDetail = { },
            onNavigateBack = { }
        )
    }
}

// Error State Preview
@Preview(showBackground = true, name = "Network Inspector - Error")
@Composable
fun NetworkInspectorScreenErrorPreview() {
    MaterialTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Error(
                RuntimeException("Network error"),
                "Failed to load transactions"
            ),
            onEvent = { },
            onNavigateToDetail = { },
            onNavigateBack = { }
        )
    }
}

// Empty State Preview
@Preview(showBackground = true, name = "Network Inspector - Empty")
@Composable
fun NetworkInspectorScreenEmptyPreview() {
    MaterialTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Success(NetworkInspectorUiData()),
            onEvent = { },
            onNavigateToDetail = { },
            onNavigateBack = { }
        )
    }
}

// Idle State Preview
@Preview(showBackground = true, name = "Network Inspector - Idle")
@Composable
fun NetworkInspectorScreenIdlePreview() {
    MaterialTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Idle,
            onEvent = { },
            onNavigateToDetail = { },
            onNavigateBack = { }
        )
    }
}

// With Clear Confirmation Preview
@Preview(showBackground = true, name = "Network Inspector - Clear Confirmation")
@Composable
fun NetworkInspectorScreenClearConfirmationPreview() {
    MaterialTheme {
        val uiData = NetworkInspectorUiData.mockNetworkInspectorUiData.copy(
            showClearConfirmation = true
        )
        NetworkInspectorScreen(
            uiState = ResourceState.Success(uiData),
            onEvent = { },
            onNavigateToDetail = { },
            onNavigateBack = { }
        )
    }
}
```

## use case template

```kotlin
class GetFeatureItemsUseCase @Inject constructor(
    private val repository: FeatureRepository
) {
    suspend operator fun invoke(): Flow<Result<List<FeatureItem>>> = flow {
        try {
            repository.getFeatureItemsStream()
                .collect { items ->
                    emit(Result.success(items))
                }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }
}

class UpdateFeatureItemUseCase @Inject constructor(
    private val repository: FeatureRepository
) {
    suspend operator fun invoke(item: FeatureItem): Flow<Result<Unit>> = flow {
        try {
            repository.updateFeatureItem(item)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

class DeleteFeatureItemUseCase @Inject constructor(
    private val repository: FeatureRepository
) {
    suspend operator fun invoke(itemId: String): Flow<Result<Unit>> = flow {
        try {
            repository.deleteFeatureItem(itemId)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
```

## repository template

```kotlin
@Singleton
class FeatureRepositoryImpl @Inject constructor(
    private val localDataSource: FeatureLocalDataSource,
    private val remoteDataSource: FeatureRemoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FeatureRepository {
    
    override suspend fun getFeatureItemsStream(): Flow<List<FeatureItem>> = 
        withContext(dispatcher) {
            flow {
                // Emit local data first
                emit(localDataSource.getFeatureItems())
                
                try {
                    // Fetch from remote and update local
                    val remoteItems = remoteDataSource.getFeatureItems()
                    localDataSource.saveFeatureItems(remoteItems)
                    emit(remoteItems)
                } catch (e: Exception) {
                    // Continue with local data if remote fails
                }
            }.distinctUntilChanged()
        }
    
    override suspend fun updateFeatureItem(item: FeatureItem): Unit = 
        withContext(dispatcher) {
            // Update local first
            localDataSource.updateFeatureItem(item)
            
            try {
                // Sync with remote
                remoteDataSource.updateFeatureItem(item)
            } catch (e: Exception) {
                // Handle sync failure - could implement retry logic
                throw e
            }
        }
    
    override suspend fun deleteFeatureItem(itemId: String): Unit = 
        withContext(dispatcher) {
            // Delete local first
            localDataSource.deleteFeatureItem(itemId)
            
            try {
                // Sync with remote
                remoteDataSource.deleteFeatureItem(itemId)
            } catch (e: Exception) {
                // Handle sync failure
                throw e
            }
        }
}
```

## complete feature structure

```kotlin
// 1. Domain Layer - Entity
data class FeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val isActive: Boolean,
    val createdAt: String = "",
    val updatedAt: String = ""
)

// 2. Domain Layer - Repository Interface
interface FeatureRepository {
    suspend fun getFeatureItemsStream(): Flow<List<FeatureItem>>
    suspend fun updateFeatureItem(item: FeatureItem)
    suspend fun deleteFeatureItem(itemId: String)
}

// 3. Presentation Layer - States
typealias FeatureUiState = NewResourceState<FeatureUiData>

data class FeatureUiData(
    val items: List<FeatureItem> = emptyList(),
    val selectedItem: FeatureItem? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val filterType: FilterType = FilterType.ALL,
    val showDialog: Boolean = false,
    val errorMessage: UiText? = null
) {
    companion object {
        val mockFeatureUiData = FeatureUiData(
            items = listOf(
                FeatureItem(
                    id = "1",
                    title = "Sample Active Item",
                    description = "This is an active item for testing",
                    isActive = true,
                    createdAt = "2024-01-01T00:00:00Z"
                ),
                FeatureItem(
                    id = "2",
                    title = "Sample Inactive Item", 
                    description = "This is an inactive item for testing",
                    isActive = false,
                    createdAt = "2024-01-02T00:00:00Z"
                ),
                FeatureItem(
                    id = "3",
                    title = "Another Active Item",
                    description = "Another active item with longer description text",
                    isActive = true,
                    createdAt = "2024-01-03T00:00:00Z"
                )
            ),
            selectedItem = FeatureItem(
                id = "1",
                title = "Sample Active Item",
                description = "This is an active item for testing",
                isActive = true,
                createdAt = "2024-01-01T00:00:00Z"
            ),
            searchQuery = "sample",
            filterType = FilterType.ACTIVE,
            isRefreshing = false,
            showDialog = false
        )
    }
}

enum class FilterType { ALL, ACTIVE, INACTIVE }

// 4. UI Components
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        } else null,
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun FilterTabs(
    selectedFilter: FilterType,
    onFilterChange: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = FilterType.entries
    
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedFilter),
        modifier = modifier
    ) {
        tabs.forEach { filter ->
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                text = { 
                    Text(
                        text = filter.name.lowercase().replaceFirstChar { it.uppercase() }
                    ) 
                }
            )
        }
    }
}

@Composable
private fun FeatureItemCard(
    item: FeatureItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onUpdate) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = if (item.isActive) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.outline
                ) {
                    Text(
                        text = if (item.isActive) "Active" else "Inactive",
                        color = if (item.isActive) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = item.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeatureDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Action") },
        text = { Text("Are you sure you want to perform this action?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}
```

## modular navigation templates

### navigator setup

```kotlin
// Core module - Navigation setup
@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {
    @Provides
    @ActivityRetainedScoped
    fun provideNavigator(): Navigator = Navigator(startDestination = HomeScreen)
}

typealias EntryProviderInstaller = EntryProviderBuilder<Any>.() -> Unit

@ActivityRetainedScoped
class Navigator(startDestination: Any) {
    val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)
    
    fun goTo(destination: Any) {
        backStack.add(destination)
    }
    
    fun goBack() {
        backStack.removeLastOrNull()
    }
    
    fun popToRoot() {
        if (backStack.size > 1) {
            val root = backStack.first()
            backStack.clear()
            backStack.add(root)
        }
    }
    
    fun replace(destination: Any) {
        backStack.removeLastOrNull()
        backStack.add(destination)
    }
}
```

### destination definitions

```kotlin
// Feature module API - Destination definitions

// Simple destination (no parameters)
object HomeScreen
object SettingsScreen

// Parameterized destination
@Serializable
data class UserProfile(
    val userId: String,
    val tab: ProfileTab = ProfileTab.INFO
) {
    enum class ProfileTab { INFO, POSTS, FOLLOWERS }
}

// Complex destination with computed properties
@Serializable
data class ProductDetail(
    val productId: String,
    val categoryId: String? = null
) {
    val shareUrl: String
        get() = "https://myapp.com/product/$productId"
    
    val analyticsKey: String
        get() = "product_${productId}_${categoryId ?: "unknown"}"
}
```

### feature module navigation

```kotlin
// Feature module implementation
@Module
@InstallIn(ActivityRetainedComponent::class)
object UserFeatureModule {
    
    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(
        navigator: Navigator,
        viewModelProvider: Provider<UserProfileViewModel>
    ): EntryProviderInstaller = {
        
        entry<UserProfile> { destination ->
            val viewModel: UserProfileViewModel = viewModelProvider.get()
            
            UserProfileScreen(
                userId = destination.userId,
                initialTab = destination.tab,
                viewModel = viewModel,
                onNavigateToSettings = { 
                    navigator.goTo(SettingsScreen) 
                },
                onNavigateToUserPosts = { userId ->
                    navigator.goTo(UserPosts(userId))
                },
                onNavigateBack = { 
                    navigator.goBack() 
                }
            )
        }
        
        entry<UserPosts> { destination ->
            UserPostsScreen(
                userId = destination.userId,
                onPostClick = { postId ->
                    navigator.goTo(PostDetail(postId))
                },
                onNavigateBack = { navigator.goBack() }
            )
        }
    }
}
```

## testing templates

### viewmodel test

```kotlin
@ExtendWith(MockitoExtension::class)
class FeatureViewModelTest {
    
    @Mock
    private lateinit var getFeatureItemsUseCase: GetFeatureItemsUseCase
    
    @Mock
    private lateinit var updateFeatureItemUseCase: UpdateFeatureItemUseCase
    
    @Mock
    private lateinit var deleteFeatureItemUseCase: DeleteFeatureItemUseCase
    
    private lateinit var viewModel: FeatureViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(TestCoroutineDispatcher())
        viewModel = FeatureViewModel(
            getFeatureItemsUseCase = getFeatureItemsUseCase,
            updateFeatureItemUseCase = updateFeatureItemUseCase,
            deleteFeatureItemUseCase = deleteFeatureItemUseCase,
            ioDispatcher = TestCoroutineDispatcher()
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when onEvent OnLoadItems is called, should load items successfully`() = runTest {
        // Given
        val mockItems = listOf(
            FeatureItem(id = "1", title = "Test", description = "Test", isActive = true)
        )
        whenever(getFeatureItemsUseCase.invoke()).thenReturn(
            flowOf(Result.success(mockItems))
        )
        
        // When
        viewModel.onEvent(FeatureViewModel.ValidationEvent.OnLoadItems)
        
        // Then
        verify(getFeatureItemsUseCase).invoke()
        
        val uiState = viewModel.featureUiState.value
        val uiData = viewModel.featureUiData.value
        
        assertTrue(uiState is NewResourceState.Success)
        assertEquals(mockItems, uiData.items)
        assertFalse(uiData.isRefreshing)
    }
    
    @Test
    fun `when onEvent OnItemClick is called, should select item`() = runTest {
        // Given
        val testItem = FeatureItem(
            id = "1", 
            title = "Test", 
            description = "Test", 
            isActive = true
        )
        
        // When
        viewModel.onEvent(FeatureViewModel.ValidationEvent.OnItemClick(testItem))
        
        // Then
        val uiData = viewModel.featureUiData.value
        assertEquals(testItem, uiData.selectedItem)
        
        val uiState = viewModel.featureUiState.value
        assertTrue(uiState is NewResourceState.Success)
    }
    
    @Test
    fun `when onEvent OnSearchQueryChange is called, should update search query`() = runTest {
        // Given
        val searchQuery = "test query"
        
        // When
        viewModel.onEvent(FeatureViewModel.ValidationEvent.OnSearchQueryChange(searchQuery))
        
        // Then
        val uiData = viewModel.featureUiData.value
        assertEquals(searchQuery, uiData.searchQuery)
    }
}
```

### screen test

```kotlin
@ExtendWith(MockitoExtension::class)
class FeatureScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `should display loading state correctly`() {
        composeTestRule.setContent {
            MaterialTheme {
                FeatureScreen(
                    featureUiState = NewResourceState.Loading,
                    featureUiData = FeatureUiData(),
                    onEvent = { },
                    onNavigateToDetail = { },
                    onNavigateBack = { }
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }
    
    @Test
    fun `should display items when in success state`() {
        val mockData = FeatureUiData.mockFeatureUiData
        
        composeTestRule.setContent {
            MaterialTheme {
                FeatureScreen(
                    featureUiState = NewResourceState.Success(mockData),
                    featureUiData = mockData,
                    onEvent = { },
                    onNavigateToDetail = { },
                    onNavigateBack = { }
                )
            }
        }
        
        // Verify items are displayed
        mockData.items.forEach { item ->
            composeTestRule.onNodeWithText(item.title)
                .assertIsDisplayed()
            composeTestRule.onNodeWithText(item.description)
                .assertIsDisplayed()
        }
    }
    
    @Test
    fun `should handle item click correctly`() {
        val mockData = FeatureUiData.mockFeatureUiData
        var clickedItem: FeatureItem? = null
        var navigatedToDetail = ""
        
        composeTestRule.setContent {
            MaterialTheme {
                FeatureScreen(
                    featureUiState = NewResourceState.Success(mockData),
                    featureUiData = mockData,
                    onEvent = { event ->
                        if (event is FeatureViewModel.ValidationEvent.OnItemClick) {
                            clickedItem = event.item
                        }
                    },
                    onNavigateToDetail = { id -> navigatedToDetail = id },
                    onNavigateBack = { }
                )
            }
        }
        
        // Click on first item
        composeTestRule.onNodeWithText(mockData.items.first().title)
            .performClick()
        
        // Verify event was triggered
        assertEquals(mockData.items.first(), clickedItem)
        assertEquals(mockData.items.first().id, navigatedToDetail)
    }
    
    @Test
    fun `should display error state correctly`() {
        composeTestRule.setContent {
            MaterialTheme {
                FeatureScreen(
                    featureUiState = NewResourceState.Error(ErrorType.NetworkError),
                    featureUiData = FeatureUiData(),
                    onEvent = { },
                    onNavigateToDetail = { },
                    onNavigateBack = { }
                )
            }
        }
        
        composeTestRule.onNodeWithText("Something went wrong")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
    }
}
```

# anti patterns

## general

- Don't use God objects or classes
- Avoid deep nesting in Compose hierarchies
- Don't perform heavy operations in main thread
- Avoid tight coupling between modules
- Don't ignore error handling
- Avoid using toString() for business logic
- Don't create unnecessary abstractions
- Avoid mutable state in data classes

## ui state data

- Don't mix loading/error states with actual data in UiData
- Avoid complex logic in UiData computed properties
- Don't expose mutable state directly from ViewModel
- Avoid nullable UiState - use sealed class instead
- Don't forget to provide mock data for previews
- Avoid heavy objects in mock data

## onevent pattern

- Don't create events for every small UI change
- Avoid complex logic in onEvent function
- Don't ignore error handling in event processing
- Avoid nested when statements in onEvent
- Don't create events that depend on previous events

## state hoisting

- Don't pass ViewModel directly to child composables
- Avoid creating stateful composables when stateless is sufficient
- Don't lift state higher than necessary
- Avoid passing entire UiData when only specific fields are needed
- Don't create callback functions that modify state directly

## coroutines

- Don't use GlobalScope except for application-wide operations
- Avoid blocking operations in coroutines (Thread.sleep, blocking I/O)
- Don't suppress CancellationException
- Avoid using runBlocking in production code
- Don't create unnecessary coroutine scopes
- Avoid using Dispatchers.Main for heavy computations
- Don't ignore proper exception handling in coroutines
- Avoid memory leaks by not cancelling coroutines properly
- Don't use delay() for timing-critical operations
- Avoid using flow {} builder for one-time operations

## modular navigation

- Don't create direct dependencies between feature modules for navigation
- Avoid exposing internal screen implementations in module APIs
- Don't pass Navigator directly to ViewModels or business logic
- Avoid creating complex navigation state in destinations
- Don't ignore proper lifecycle handling in navigation
- Avoid hardcoded navigation paths or destinations

# module dependencies

## allowed

- app -> feature modules
- feature -> core modules
- core -> external libraries
- feature -> domain interfaces
- data -> domain interfaces

## forbidden

- domain -> data/presentation
- core -> feature modules
- feature -> feature (direct)
- circular dependencies

# build config

## gradle

- Use Gradle Version Catalogs for dependency management
- Configure build variants properly
- Use Gradle build cache
- Implement proper signing configuration
- Configure R8/ProGuard rules

## kotlin compiler

- Enable explicit API mode
- Use Kotlin compiler extensions
- Configure serialization plugin
- Enable compose compiler metrics

# git workflow

- Use conventional commits
- Create feature branches for new features
- Write meaningful commit messages
- Use pull requests for code review
- Maintain clean git history

# ci cd

- Run unit tests on every commit
- Perform static analysis checks
- Generate test coverage reports
- Build and test on multiple API levels
- Automate release process

## build

### commands
- clean: "./gradlew clean"
- build: "./gradlew build"
- test: "./gradlew test"
- lint: "./gradlew lint"
- assemble_debug: "./gradlew assembleDebug"
- assemble_release: "./gradlew assembleRelease"

### code style
- ktlint: true
- detekt: true
- indent_size: 4
- max_line_length: 120