package com.cornellappdev.android.pollo.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group(val id: Int, val name: String, val code: String, val updatedAt: String?, val isLive: Boolean?) : Parcelable
