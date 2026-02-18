package kr.sjh.feature.adoption.screen.filter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.feature.adoption.state.FilterEvent
import kr.sjh.feature.adoption.state.FilterUiState
import kr.sjh.feature.adoption.state.Neuter
import kr.sjh.feature.adoption.state.SideEffect
import kr.sjh.feature.adoption.state.UpKind
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class FilterFetchSideEffectTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun confirmUpKind_cat_emits_fetch_with_cat_code() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(FilterEvent.ConfirmUpKind(UpKind.CAT))

        assertEquals("422400", awaitFetchRequest(viewModel).upkind)
    }

    @Test
    fun confirmUpKind_dog_emits_fetch_with_dog_code() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(FilterEvent.ConfirmUpKind(UpKind.DOG))

        assertEquals("417000", awaitFetchRequest(viewModel).upkind)
    }

    @Test
    fun confirmUpKind_etc_emits_fetch_with_etc_code() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(FilterEvent.ConfirmUpKind(UpKind.ETC))

        assertEquals("429900", awaitFetchRequest(viewModel).upkind)
    }

    @Test
    fun confirmNeuter_yes_emits_fetch_with_yes_code() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(FilterEvent.ConfirmNeuter(Neuter.YES))

        assertEquals("Y", awaitFetchRequest(viewModel).neuter_yn)
    }

    @Test
    fun confirmDateRange_emits_fetch_with_selected_dates() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(FilterEvent.ConfirmDateRange("20250101", "20250131"))

        val req = awaitFetchRequest(viewModel)
        assertEquals("20250101", req.bgnde)
        assertEquals("20250131", req.endde)
    }

    @Test
    fun confirmLocation_emits_fetch_with_selected_codes() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(
            FilterEvent.ConfirmLocation(
                sido = Sido(orgCd = "6110000", orgdownNm = "서울특별시"),
                sigungu = Sigungu(uprCd = "6110000", orgCd = "3220000", orgdownNm = "강남구")
            )
        )

        val req = awaitFetchRequest(viewModel)
        assertEquals("6110000", req.upr_cd)
        assertEquals("3220000", req.org_cd)
    }

    @Test
    fun reset_emits_fetch_with_default_filter_request() = runTest {
        val viewModel = FilterViewModel(FakeAdoptionRepository())
        drainInitialFetch(viewModel)

        viewModel.onEvent(FilterEvent.ConfirmUpKind(UpKind.CAT))
        awaitFetchRequest(viewModel)
        viewModel.onEvent(FilterEvent.ConfirmNeuter(Neuter.YES))
        awaitFetchRequest(viewModel)
        viewModel.onEvent(
            FilterEvent.ConfirmLocation(
                sido = Sido(orgCd = "6110000", orgdownNm = "서울특별시"),
                sigungu = Sigungu(uprCd = "6110000", orgCd = "3220000", orgdownNm = "강남구")
            )
        )
        awaitFetchRequest(viewModel)

        viewModel.onEvent(FilterEvent.Reset)

        val req = awaitFetchRequest(viewModel)
        val defaultReq = FilterUiState().toPetRequest()
        assertEquals(defaultReq.upkind, req.upkind)
        assertEquals(defaultReq.neuter_yn, req.neuter_yn)
        assertEquals(defaultReq.bgnde, req.bgnde)
        assertEquals(defaultReq.endde, req.endde)
        assertEquals(defaultReq.upr_cd, req.upr_cd)
        assertEquals(defaultReq.org_cd, req.org_cd)
    }

    private suspend fun drainInitialFetch(viewModel: FilterViewModel) {
        withTimeout(2_000) {
            viewModel.sideEffect.filterIsInstance<SideEffect.FetchPets>().first()
        }
    }

    private suspend fun awaitFetchRequest(viewModel: FilterViewModel): PetRequest {
        return withTimeout(2_000) {
            viewModel.sideEffect.filterIsInstance<SideEffect.FetchPets>().first().req
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

private class FakeAdoptionRepository : AdoptionRepository {
    override suspend fun insertSidoList() = Unit

    override suspend fun insertSigunguList(list: List<Sigungu>) = Unit

    override fun getSidoList(): Flow<List<Sido>> = flowOf(emptyList())

    override fun getSigunguList(sido: Sido): Flow<List<Sigungu>> = flowOf(emptyList())

    override fun getPets(req: PetRequest): Flow<List<Pet>> = flowOf(emptyList())
}
