package kr.sjh.feature.adoption.screen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.AdoptionEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class AdoptionViewModelPagingGuardTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadMore_is_ignored_while_refreshing() = runTest {
        val repository = RecordingAdoptionRepository(delayMillis = 3_000)
        val viewModel = AdoptionViewModel(repository)
        val req = PetRequest(pageNo = 1, upkind = "422400")

        viewModel.onEvent(AdoptionEvent.Refresh(req))
        waitUntilRequestCount(repository, expected = 1)

        viewModel.onEvent(AdoptionEvent.LoadMore(req))
        runCurrent()

        assertEquals(1, repository.requestHistory.size)
        assertEquals(1, repository.requestHistory.first().pageNo)
    }

    @Test
    fun loadMore_uses_latest_active_request_context() = runTest {
        val repository = RecordingAdoptionRepository()
        val viewModel = AdoptionViewModel(repository)
        val latestReq = PetRequest(
            pageNo = 1,
            upkind = "422400",
            neuter_yn = "Y",
            upr_cd = "6110000",
            org_cd = "3220000",
            bgnde = "20250101",
            endde = "20250131"
        )

        viewModel.onEvent(AdoptionEvent.Refresh(latestReq))
        runCurrent()
        advanceUntilIdle()

        viewModel.onEvent(AdoptionEvent.LoadMore(PetRequest(pageNo = 1, upkind = "417000")))
        runCurrent()
        advanceUntilIdle()

        assertEquals(2, repository.requestHistory.size)
        val secondReq = repository.requestHistory[1]
        assertEquals(2, secondReq.pageNo)
        assertEquals("422400", secondReq.upkind)
        assertEquals("Y", secondReq.neuter_yn)
        assertEquals("6110000", secondReq.upr_cd)
        assertEquals("3220000", secondReq.org_cd)
        assertEquals("20250101", secondReq.bgnde)
        assertEquals("20250131", secondReq.endde)
    }

    @Test
    fun loadMore_without_previous_refresh_is_ignored() = runTest {
        val repository = RecordingAdoptionRepository()
        val viewModel = AdoptionViewModel(repository)

        viewModel.onEvent(AdoptionEvent.LoadMore(PetRequest(pageNo = 1)))
        runCurrent()

        assertTrue(repository.requestHistory.isEmpty())
    }

    private suspend fun waitUntilRequestCount(
        repository: RecordingAdoptionRepository,
        expected: Int
    ) {
        withTimeout(2_000) {
            while (repository.requestHistory.size < expected) {
                delay(10)
            }
        }
    }
}

class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class RecordingAdoptionRepository(
    private val delayMillis: Long = 0L
) : AdoptionRepository {
    val requestHistory = mutableListOf<PetRequest>()

    override suspend fun insertSidoList() = Unit

    override suspend fun insertSigunguList(list: List<Sigungu>) = Unit

    override fun getSidoList(): Flow<List<Sido>> = flowOf(emptyList())

    override fun getSigunguList(sido: Sido): Flow<List<Sigungu>> = flowOf(emptyList())

    override fun getPets(req: PetRequest): Flow<List<Pet>> = flow {
        requestHistory.add(req)
        if (delayMillis > 0) delay(delayMillis)
        emit(emptyList())
    }
}
