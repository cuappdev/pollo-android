package com.cornellappdev.android.pollo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SavedPoll(
        val id: String? = null,
        val createdAt: String? = null,
        var text: String,
        var options: ArrayList<String>,
        var updatedAt: String? = null
) : Parcelable {}