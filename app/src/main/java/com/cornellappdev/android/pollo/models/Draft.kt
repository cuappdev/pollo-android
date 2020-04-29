package com.cornellappdev.android.pollo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Draft(
        val id: String? = null,
        val createdAt: String? = null,
        val text: String,
        val options: ArrayList<String>,
        val updatedAt: String? = null
) : Parcelable {}