package com.kieronquinn.app.smartspacer.plugin.shared.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

class DialogLauncherActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FRAGMENT_CLASS = "extra_fragment_class"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val fragmentClassName = intent.getStringExtra(EXTRA_FRAGMENT_CLASS)
            val fragmentClass = Class.forName(fragmentClassName).asSubclass(DialogFragment::class.java)
            val fragment = fragmentClass.newInstance()
            fragment.arguments = intent.extras
            fragment.show(supportFragmentManager, "dialog_fragment")
        } catch (e: ClassNotFoundException) {
            finish()
        } catch (e: InstantiationException) {
            finish()
        } catch (e: IllegalAccessException) {
            finish()
        }
    }
}
