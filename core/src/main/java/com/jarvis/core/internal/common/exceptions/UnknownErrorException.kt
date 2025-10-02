package com.jarvis.core.internal.common.exceptions

import androidx.annotation.RestrictTo

import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class UnknownErrorException(message: String) : IOException(message)

