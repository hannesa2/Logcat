package info.hannes.crashlytic

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
class CrashlyticsTree(private val identifier: String? = null) : Timber.Tree() {

    override fun logMessage(priority: Int, tag: String?, message: String, t: Throwable?, vararg args: Any?) {
        if (priority < Log.INFO) {
            return
        }

        super.log(priority, tag, message, t)

        FirebaseCrashlytics.getInstance().setCustomKey("PRIORITY", when (priority) {
            2 -> "Verbose"
            3 -> "Debug"
            4 -> "Info"
            5 -> "Warn"
            6 -> "Error"
            7 -> "Assert"
            else -> priority.toString()
        })
        tag?.let { FirebaseCrashlytics.getInstance().setCustomKey(KEY_TAG, it) }
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_MESSAGE, message)
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_UNIT_TEST, isRunningUnitTests.toString())
        FirebaseCrashlytics.getInstance().setCustomKey(KEY_ESPRESSO, isRunningEspresso().toString())
        identifier?.let { FirebaseCrashlytics.getInstance().setUserId(it) }

        if (priority > Log.WARN) {
            if (t != null)
                FirebaseCrashlytics.getInstance().recordException(t)
            else
                FirebaseCrashlytics.getInstance().recordException(Throwable(message))
        } else if (priority > Log.INFO) {
            FirebaseCrashlytics.getInstance().log(message)
        }
    }

    companion object {
        const val KEY_TAG = "TAG"
        const val KEY_MESSAGE = "message"
        const val KEY_ESPRESSO = "Espresso"
        const val KEY_UNIT_TEST = "UnitTest"

        private var runningTest: AtomicBoolean? = null
        private var runUnitTest: Boolean? = null

        val isRunningUnitTests: Boolean
            get() {
                if (runUnitTest == null) {
                    runUnitTest = "true" == System.getProperty("run-under-test", "false") || !System.getProperty("java.vendor")!!.contains("Android")
                }
                return runUnitTest!!
            }

        @Synchronized
        fun isRunningEspresso(): Boolean {
            if (runningTest == null) {
                var isTest: Boolean = try {
                    Class.forName("android.support.test.espresso.Espresso")
                    true
                } catch (e: ClassNotFoundException) {
                    false
                }

                if (!isTest) {
                    isTest = try {
                        Class.forName("androidx.test.espresso.Espresso")
                        true
                    } catch (e: ClassNotFoundException) {
                        false
                    }
                }

                runningTest = AtomicBoolean(isTest)
            }
            return runningTest!!.get()
        }
    }
}
