package com.example.domainsearch.extensions

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.domainsearch.util.SharedPrefs

fun Context.getDefaultSharedPrefs(): SharedPrefs =
    PreferenceManager.getDefaultSharedPreferences(this)
