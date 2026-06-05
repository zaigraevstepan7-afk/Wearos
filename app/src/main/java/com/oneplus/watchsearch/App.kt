package com.oneplus.watchsearch

import android.app.Application
import android.content.Intent
import android.os.Process
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/**
 * Installs a process-wide crash handler. Instead of the app silently dying,
 * any uncaught exception is rendered on a [CrashActivity] screen so the
 * stack trace can be read (and screenshotted) directly on the watch.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))

                val intent = Intent(this, CrashActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(CrashActivity.EXTRA_TRACE, sw.toString())
                startActivity(intent)
            } catch (_: Throwable) {
                // If even the reporter fails, fall through to terminating.
            } finally {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }
}
