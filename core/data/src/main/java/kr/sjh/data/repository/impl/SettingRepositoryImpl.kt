package kr.sjh.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.sjh.core.model.setting.Setting
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.toSetting
import kr.sjh.data.toSettingEntity
import kr.sjh.database.dao.SettingDao
import javax.inject.Inject

class SettingRepositoryImpl @Inject constructor(private val dao: SettingDao) : SettingRepository {
    override fun getSetting(): Flow<Setting> = dao.select().map {
        it.toSetting()
    }.flowOn(Dispatchers.IO)

    override suspend fun insertSetting(setting: Setting) {

        dao.insert(setting.toSettingEntity())
    }

    override suspend fun isSettingExists(): Boolean = dao.isSettingExists()
}