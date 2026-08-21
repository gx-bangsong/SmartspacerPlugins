package com.kieronquinn.app.smartspacer.plugin.shared.ui.activities

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

// This activity is used with the shared Material3 dialog theme.  It must not
// extend AppCompatActivity because that requires a Theme.AppCompat parent,
// while Theme.Smartspacer.Dialog is a Material3 theme.
class DialogLauncherActivity : FragmentActivity() {

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
        } catch (e: Exception) {
            finish()
        }
    }
}
