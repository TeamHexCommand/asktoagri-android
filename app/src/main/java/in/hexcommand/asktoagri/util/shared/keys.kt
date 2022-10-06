package `in`.hexcommand.asktoagri.util.shared

object Keys {

    init {
        System.loadLibrary("native-lib")
    }

    external fun apiKey(): String
}