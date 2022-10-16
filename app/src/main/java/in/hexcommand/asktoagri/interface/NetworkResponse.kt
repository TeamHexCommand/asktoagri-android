package `in`.hexcommand.asktoagri.`interface`

interface NetworkResponse {
    fun onFailure(message: String): String
    suspend fun onResponse(response: String, next: String = ""): String
}