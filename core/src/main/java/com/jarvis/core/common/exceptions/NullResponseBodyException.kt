package com.jarvis.core.common.exceptions

import java.io.IOException

class NullResponseBodyException(message: String = NullResponseBodyException::class.java.simpleName) :
    IOException(message)