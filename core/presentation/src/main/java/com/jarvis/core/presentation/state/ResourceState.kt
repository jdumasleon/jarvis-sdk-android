package com.jarvis.core.presentation.state

/**
 * ResourceState sealed class for state management
 * Represents different states of a resource (data loading/success/error/idle)
 */
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
    
    /**
     * Maps the data if the state is Success, otherwise returns the same state
     */
    inline fun <R> map(transform: (T) -> R): ResourceState<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
        is Idle -> this
    }
    
    /**
     * Executes the given action if the state is Success
     */
    inline fun onSuccess(action: (T) -> Unit): ResourceState<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Executes the given action if the state is Error
     */
    inline fun onError(action: (Throwable, String?) -> Unit): ResourceState<T> {
        if (this is Error) action(exception, message)
        return this
    }
    
    /**
     * Executes the given action if the state is Loading
     */
    inline fun onLoading(action: () -> Unit): ResourceState<T> {
        if (this is Loading) action()
        return this
    }
}