package com.jarvis.core.data.common

interface DataFactory<C, R> {

    val cacheDataStore: C
    val remoteDataStore: R
}
