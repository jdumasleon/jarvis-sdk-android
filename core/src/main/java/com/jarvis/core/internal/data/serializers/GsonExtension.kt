package com.jarvis.core.internal.data.serializers

import androidx.annotation.RestrictTo

import com.google.gson.GsonBuilder
import java.util.Date

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun GsonBuilder.setupTypeAdapters(): GsonBuilder {
    return this.apply {
        registerTypeAdapter(Date::class.java, GsonDateDeserializer())
    }
}
