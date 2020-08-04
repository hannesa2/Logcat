package info.hannes.countly

import android.app.Activity

interface IAnalytics {

    fun isInitialized(): Boolean

    fun recordEvent(event: String, vararg args: Any?)

    fun recordWarning(message: String, vararg args: Any?)

    fun recordError(message: String, vararg args: Any?)

    fun recordError(throwable: Throwable)

    fun onStart(activity: Activity?)

    fun onStop()
}