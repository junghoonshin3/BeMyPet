package kr.sjh.core.supabase.service

import kr.sjh.core.model.ReportForm

interface ReportService {
    suspend fun reportUsers(report: ReportForm)
}