package com.carlosalbertoxw.crud_android_sqlite.ui.screens

import java.text.DateFormat
import java.util.Date

/** Formatea una marca de tiempo epoch (millis) como fecha y hora local corta. */
fun formatTimestamp(millis: Long): String =
    DateFormat
        .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(millis))
