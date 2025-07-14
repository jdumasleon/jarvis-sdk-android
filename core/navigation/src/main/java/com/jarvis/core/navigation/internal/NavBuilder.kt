package com.jarvis.core.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.jarvis.core.navigation.Destination
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer

@ExperimentalSerializationApi
@PublishedApi
internal fun <T : Destination> SavedStateHandle.decodeArguments(
	serializer: KSerializer<T>,
): T {
	val decoder = UriDataDecoder(SavedStateDataMap(this))
	return decoder.decodeSerializableValue(serializer)
}
