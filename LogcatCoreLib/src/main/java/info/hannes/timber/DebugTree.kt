package info.hannes.timber

import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

open class DebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
                "(%s:%d) %s.%s()",
                element.fileName,
                element.lineNumber, // format ensures line numbers have at least 3 places to align consecutive output from the same file
                // method is fully qualified only when class differs on filename otherwise it can be cropped on long lambda expressions
                super.createStackElementTag(element)?.replaceFirst(element.fileName.takeWhile { it != '.' }, ""),
                element.methodName
        )
    }

    // if there is an JSON string, try to print out pretty
    override fun logMessage(priority: Int, tag: String?, message: String, t: Throwable?, vararg args: Any?) {
        var localMessage = message.trim()
        if (localMessage.startsWith("{") && localMessage.endsWith("}")) {
            try {
                val json = JSONObject(message)
                localMessage = json.toString(3)
            } catch (e: JSONException) {
            }
        }
        super.logMessage(priority, tag, localMessage, t, *args)
    }
}
