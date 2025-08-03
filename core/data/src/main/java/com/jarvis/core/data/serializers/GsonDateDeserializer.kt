package com.jarvis.core.data.serializers

import androidx.annotation.Keep
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.jarvis.core.common.extensions.format
import com.jarvis.core.common.extensions.supportedDateFormat
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Keep
class GsonDateDeserializer : JsonDeserializer<Date>, JsonSerializer<Date> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date {
        for (format in supportedDateFormat) {
            try {
                val stringDate = json?.asJsonPrimitive?.asString

                return stringDate?.let {
                    SimpleDateFormat(
                        format,
                        Locale.getDefault()
                    ).parse(it)
                } as Date
            } catch (_: Exception) {
            }
        }

        throw ParseException("Error parsing date", 0)
    }

    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        return if (src == null) {
            null
        } else {
            JsonPrimitive(src.format("yyyy-MM-dd'T'HH:mm:ss"))
        }
    }
}
