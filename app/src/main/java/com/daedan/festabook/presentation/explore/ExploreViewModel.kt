package com.daedan.festabook.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.repository.ExploreRepository
import com.daedan.festabook.presentation.explore.model.SearchResultUiModel
import com.daedan.festabook.presentation.explore.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(FlowPreview::class)
@ContributesIntoMap(AppScope::class)
@ViewModelKey(ExploreViewModel::class)
@Inject
class ExploreViewModel(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<ExploreSideEffect>(replay = 0, extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    init {
        checkFestivalId()
        observeSearchQuery()
    }

    private fun checkFestivalId() {
        val festivalId = exploreRepository.getFestivalId()
        Timber.d("festival ID : $festivalId")
        if (festivalId != null) {
            _uiState.update { it.copy(hasFestivalId = true) }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _uiState
                .map { it.query }
                .distinctUntilChanged()
                .debounce(300L)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchState = SearchUiState.Idle) }
                        return@collectLatest
                    }

                    _uiState.update { it.copy(searchState = SearchUiState.Loading) }

                    exploreRepository
                        .search(query)
                        .onSuccess { universitiesFound ->
                            Timber.d("검색 성공 - received: $universitiesFound")
                            val uiModels = universitiesFound.map { it.toUiModel() }
                            _uiState.update {
                                it.copy(searchState = SearchUiState.Success(universitiesFound = uiModels))
                            }
                        }.onFailure { throwable ->
                            Timber.d(throwable, "검색 실패")
                            _uiState.update {
                                it.copy(searchState = SearchUiState.Error(throwable))
                            }
                        }
                }
        }
    }

    fun onUniversitySelected(university: SearchResultUiModel) {
        exploreRepository.saveFestivalId(university.festivalId)
        viewModelScope.launch {
            _sideEffect.tryEmit(ExploreSideEffect.NavigateToMain(university))
        }
    }

    fun onTextInputChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }
}
