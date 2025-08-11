# Preferences Feature Architecture

## Correct Layer Responsibilities

### **Domain Layer** (Pure Business Logic)
- Entities: `AppPreference`, `PreferenceType`, etc.
- Use Cases: `GetPreferencesUseCase`, `UpdatePreferenceUseCase`
- **Configuration Interface**: Define contract for configuration
- Repository Interfaces

### **Data Layer** (Implementation)
- Repository Implementations
- Data Sources: `PreferencesDataStoreScanner`, etc.
- **Configuration Implementation**: Implement configuration interface
- Local storage logic

### **Presentation Layer** (UI)
- ViewModels
- UI State
- Compose screens

### **Lib Layer** (Public API)
- **API Exposure Only**: Navigation, DI modules
- Should NOT contain business logic
- Should NOT contain configuration management

## Fixed Dependency Flow
```
jarvis -> preferences.lib
preferences.lib -> preferences.presentation (api)
preferences.lib -> preferences.data (impl)  
preferences.lib -> preferences.domain (impl)
preferences.data -> preferences.domain
preferences.presentation -> preferences.domain

Configuration Flow:
jarvis -> preferences.domain (via interface)
preferences.data -> preferences.domain (implements interface)
```

## Configuration Strategy
1. **Interface in Domain**: `PreferencesConfigProvider` interface
2. **Implementation in Data**: Concrete implementation 
3. **DI in Lib**: Binds interface to implementation
4. **Usage in Jarvis**: Injects domain interface