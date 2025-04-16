package kr.sjh.core.supabase.service.impl

import io.github.jan.supabase.postgrest.Postgrest
import kr.sjh.core.model.ReportForm
import kr.sjh.core.supabase.service.ReportService
import javax.inject.Inject

class ReportServiceImpl @Inject constructor(postgrest: Postgrest) : ReportService {
    private val reportTable = postgrest.from("reports")
    override suspend fun reportUsers(report: ReportForm) {
        try {
            reportTable.insert(
                report
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}