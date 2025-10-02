package com.jarvis.core.internal.common.exceptions

import androidx.annotation.RestrictTo

import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NullResponseBodyException(message: String = NullResponseBodyException::class.java.simpleName) :
    IOException(message)