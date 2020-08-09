package info.hannes.timber

import android.util.Log
import info.hannes.countly.Analytics

class CountlyTree(private val analytics: Analytics, private val serverIgnoreToken: String? = null) : DebugTree() {

    // some regex explanation :
    // $t sign to search
    // . any char
    // + minimum once
    // ? null or once
    // | or
    // # beginning with #
    // [^#] any char except #
    // *$ till end of line

    // short variable to avoid lint warning "Duplicate char n inside Character class" in following line
    private val t = serverIgnoreToken
    private val regex: Regex = "$t.+?$t|$t[^$t]*$".toRegex()

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        // we ignore INFO, DEBUG and VERBOSE
        if (priority <= Log.INFO) {
            return
        }
        var localMessage = message
        serverIgnoreToken?.let {
            localMessage = regex.replace(message, "")
        }

        when {
            throwable != null -> analytics.recordError(throwable)
            priority == Log.WARN -> analytics.recordEvent(localMessage)
            else -> analytics.recordError(localMessage)
        }
    }

}