package kr.sjh.core.model.setting

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Stable
@Serializable
@Parcelize
data class Setting(
    val darkTheme: Boolean = false,
) : Parcelable