package info.hannes.timber

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import info.hannes.logcat.Event
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
@SuppressLint("LogNotTimber")
open class FileLoggingTree(externalCacheDir: File, context: Context? = null, filename: String = UUID.randomUUID().toString()) : DebugTree() {

    var file: File
        private set

    val lastLogEntry: LiveData<Event<String>>
        get() = _lastLogEntry

    protected val _lastLogEntry = MutableLiveData<Event<String>>()

    private var logImpossible = false

    init {
        if (!externalCacheDir.exists()) {
            if (!externalCacheDir.mkdirs())
                Log.e(LOG_TAG, "couldn't create ${externalCacheDir.absoluteFile}")
        }
        val fileNameTimeStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        file = if (context != null) {
            File(externalCacheDir, "${context.packageName}.$fileNameTimeStamp.log")
        } else {
            File(externalCacheDir, "$filename.$fileNameTimeStamp.log")
        }
    }

    @SuppressLint("LogNotTimber")
    override fun logMessage(priority: Int, tag: String?, message: String, t: Throwable?, vararg args: Any?) {
        try {
            val logTimeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(Date())

            val priorityText = when (priority) {
                2 -> "V:"
                3 -> "D:"
                4 -> "I:"
                5 -> "W:"
                6 -> "E:"
                7 -> "A:"
                else -> "$priority"
            }

            val writer = FileWriter(file, true)
            val textLine = "$priorityText $logTimeStamp$tag$message\n"
            writer.append(textLine)
            writer.flush()
            writer.close()

            if (Thread.currentThread().name == "main")
                _lastLogEntry.value = Event(textLine)
            else
                Handler(Looper.getMainLooper()).post { _lastLogEntry.value = Event(textLine) }

        } catch (e: Exception) {
            // Log to prevent an endless loop
            if (!logImpossible) {
                // log this output just once
                Log.w(LOG_TAG, "Can't log into file : $e")
                logImpossible = true
            }
        }
        // Don't call super, otherwise it logs twice
        //super.logMessage(priority, tag, message, t, args)
    }

    fun getFileName(): String = file.absolutePath

    companion object {

        private val LOG_TAG = FileLoggingTree::class.java.simpleName
    }
}
