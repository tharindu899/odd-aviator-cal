package com.aviator.hoc.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.data.Stats
import com.aviator.hoc.data.db.SignalStatus
import com.aviator.hoc.domain.ResultRow
import com.aviator.hoc.domain.SortMode
import com.aviator.hoc.domain.StatusFilter
import com.aviator.hoc.domain.filterAndSort
import com.aviator.hoc.domain.filterCounts
import com.aviator.hoc.domain.toRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the user can change on the screen. */
data class ResultsControls(
    val filter: StatusFilter = StatusFilter.ALL,
    val query: String = "",
    val sort: SortMode = SortMode.ACTIVE_FIRST,
    val searchOpen: Boolean = false,
    val expandedId: Long? = null,
    val deleteId: Long? = null,
    val deleteAllOpen: Boolean = false,
)

data class ResultsUiState(
    val rows: List<ResultRow> = emptyList(),
    val total: Int = 0,
    val counts: Map<StatusFilter, Int> = emptyMap(),
    val stats: Stats = SignalRepository.statsOf(emptyList()),
    val controls: ResultsControls = ResultsControls(),
)

class ResultsViewModel(private val repository: SignalRepository) : ViewModel() {

    private val controls = MutableStateFlow(ResultsControls())

    // pending rows show a live countdown, so rebuild once per second
    private val ticker = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(now)
            delay(1000 - (now % 1000) + 5)
        }
    }

    val state: StateFlow<ResultsUiState> =
        combine(repository.signals, ticker, controls) { list, now, c ->
            val shown = filterAndSort(list, c.filter, c.query, c.sort, now)
            ResultsUiState(
                rows = shown.map { toRow(it, now) },
                total = list.size,
                counts = filterCounts(list),
                stats = SignalRepository.statsOf(list, now),
                controls = c,
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultsUiState())

    fun setFilter(f: StatusFilter) = controls.update { it.copy(filter = f) }
    fun setQuery(q: String) = controls.update { it.copy(query = q) }
    fun setSort(s: SortMode) = controls.update { it.copy(sort = s) }
    fun toggleSearch() = controls.update { it.copy(searchOpen = !it.searchOpen) }
    fun clearFilters() = controls.update { it.copy(filter = StatusFilter.ALL, query = "") }

    fun toggleExpand(id: Long) = controls.update { it.copy(expandedId = if (it.expandedId == id) null else id) }

    fun askDelete(id: Long?) = controls.update { it.copy(deleteId = id) }
    fun askDeleteAll(open: Boolean) = controls.update { it.copy(deleteAllOpen = open) }

    fun confirmDelete() {
        val id = controls.value.deleteId ?: return
        controls.update { it.copy(deleteId = null, expandedId = null) }
        viewModelScope.launch { repository.delete(id) }
    }

    fun confirmDeleteAll() {
        controls.update { ResultsControls(searchOpen = it.searchOpen) }
        viewModelScope.launch { repository.clearAll() }
    }

    fun markWin(id: Long) = viewModelScope.launch { repository.mark(id, SignalStatus.WIN) }
    fun markLoss(id: Long) = viewModelScope.launch { repository.mark(id, SignalStatus.LOSS) }
    fun reset(id: Long) = viewModelScope.launch { repository.mark(id, SignalStatus.PENDING) }
}
