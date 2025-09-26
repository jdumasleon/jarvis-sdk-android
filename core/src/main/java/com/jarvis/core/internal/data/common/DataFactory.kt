package com.jarvis.core.internal.data.common

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface DataFactory<C, R> {

    val cacheDataStore: C
    val remoteDataStore: R
}
