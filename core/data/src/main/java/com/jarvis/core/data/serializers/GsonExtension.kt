package com.jarvis.core.data.serializers

import com.google.gson.GsonBuilder
import java.util.Date

fun GsonBuilder.setupTypeAdapters(): GsonBuilder {
    return this.apply {
        registerTypeAdapter(Date::class.java, GsonDateDeserializer())
    }
}
