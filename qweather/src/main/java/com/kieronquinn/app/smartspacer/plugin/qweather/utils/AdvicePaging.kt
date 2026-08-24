package com.kieronquinn.app.smartspacer.plugin.qweather.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Complication text is paged differently on Native Smartspace vs the Smartspacer widget.
 *
 * Native At a Glance has a short but still usable line — packing more than 8 code units
 * onto one page avoids burning the limited page budget. The widget's complication slot
 * is narrower, so it keeps the original 8-unit pages.
 */
enum class AdvicePaging(val maxChars: Int) {
    NATIVE(16),
    WIDGET(8);

    companion object {
        private val NATIVE_LAUNCHERS = setOf(
            "com.google.android.apps.nexuslauncher",
            "com.google.android.apps.pixel.launcher",
            "com.android.launcher3"
        )

        fun forContext(context: Context): AdvicePaging {
            return if (isNativeHome(context)) NATIVE else WIDGET
        }

        fun isNativeHome(context: Context): Boolean {
            return defaultHomePackage(context) in NATIVE_LAUNCHERS
        }

        internal fun defaultHomePackage(context: Context): String? {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolve = context.packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            return resolve?.activityInfo?.packageName
        }
    }
}
