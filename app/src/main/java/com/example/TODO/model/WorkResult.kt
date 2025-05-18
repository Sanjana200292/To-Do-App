package com.example.TODO.model

typealias Mapper<Input, Output> = (Input) -> Output

sealed class WorkResult<T> {

    // Define LoadingResult as a generic class
    class LoadingResult<T> : WorkResult<T>()

    // SuccessResult carries the successful data
    data class SuccessResult<T>(
        val data: T
    ) : WorkResult<T>()

    // ErrorResult carries an exception for error scenarios
    data class ErrorResult<T>(
        val error: Exception
    ) : WorkResult<T>()

    // Map function to transform WorkResult from one type to another
    fun <R> map(mapper: Mapper<T, R>? = null): WorkResult<R> = when (this) {
        is LoadingResult -> LoadingResult()
        is ErrorResult -> ErrorResult(this.error)
        is SuccessResult -> {
            if (mapper == null) {
                throw IllegalStateException("Mapper should not be null for SuccessResult")
            }
            SuccessResult(mapper(this.data))
        }
    }
}
