package kr.sjh.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.sjh.core.model.setting.SettingType

@Entity("setting")
data class SettingEntity(
    @PrimaryKey val id: Int = 1,
    val theme: String = SettingType.SYSTEM_THEME.title,
)