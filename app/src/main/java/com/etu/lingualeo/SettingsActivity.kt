package com.etu.lingualeo

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.etu.lingualeo.restUtil.RestUtil


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : SharedPreferences.OnSharedPreferenceChangeListener,
        PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this)

            val editTextPreference: EditTextPreference? = findPreference("ll_password")

            editTextPreference?.setOnBindEditTextListener { editText ->
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (sharedPreferences != null) {
                val login = sharedPreferences.getString("ll_login", null)
                val password = sharedPreferences.getString("ll_password", null)
                if (login != null && password != null) {
                    val restUtil = RestUtil()
                    restUtil.login(login, password, { status: Boolean ->
                        activity!!.runOnUiThread {
                            Toast.makeText(
                                context,
                                if (status) "Авторизация пройдена успешна" else "Ошибка авторизации",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }
        }
    }
}