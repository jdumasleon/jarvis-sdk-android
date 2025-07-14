package com.jarvis.demo.presentation.home

import com.jarvis.core.navigation.Destination
import kotlinx.serialization.Serializable

internal sealed interface HomeDestinationsGraph : Destination {
    @Serializable
    data object Home : HomeDestinationsGraph {
        override val titleTextId: Int
            get() = TODO("Not yet implemented")
        override val route: String
            get() = TODO("Not yet implemented")
        override val destination: Destination
            get() = TODO("Not yet implemented")
    }
}