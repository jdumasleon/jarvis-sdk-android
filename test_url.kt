fun main() {
    val url = "not-a-valid-url"
    try {
        val result = url.substringAfter("://").substringBefore("/")
        println("Result: '$result'")
    } catch (e: Exception) {
        println("Exception: ${e.message}")
    }
}
