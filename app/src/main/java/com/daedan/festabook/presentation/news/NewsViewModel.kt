package com.daedan.festabook.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daedan.festabook.di.viewmodel.ViewModelKey
import com.daedan.festabook.domain.model.Lost
import com.daedan.festabook.domain.repository.FAQRepository
import com.daedan.festabook.domain.repository.LostItemRepository
import com.daedan.festabook.domain.repository.NoticeRepository
import com.daedan.festabook.presentation.news.faq.FAQUiState
import com.daedan.festabook.presentation.news.faq.model.FAQItemUiModel
import com.daedan.festabook.presentation.news.faq.model.toUiModel
import com.daedan.festabook.presentation.news.lost.LostUiState
import com.daedan.festabook.presentation.news.lost.model.LostUiModel
import com.daedan.festabook.presentation.news.lost.model.toLostGuideItemUiModel
import com.daedan.festabook.presentation.news.lost.model.toLostItemUiModel
import com.daedan.festabook.presentation.news.notice.NoticeUiState
import com.daedan.festabook.presentation.news.notice.NoticeUiState.Companion.DEFAULT_POSITION
import com.daedan.festabook.presentation.news.notice.model.NoticeUiModel
import com.daedan.festabook.presentation.news.notice.model.toUiModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(NewsViewModel::class)
@Inject
class NewsViewModel(
    private val noticeRepository: NoticeRepository,
    private val faqRepository: FAQRepository,
    private val lostItemRepository: LostItemRepository,
) : ViewModel() {
    private val _noticeUiState: MutableStateFlow<NoticeUiState> =
        MutableStateFlow(NoticeUiState(content = NoticeUiState.Content.InitialLoading))
    val noticeUiState: StateFlow<NoticeUiState> = _noticeUiState.asStateFlow()

    private val _faqUiState: MutableStateFlow<FAQUiState> =
        MutableStateFlow(FAQUiState.InitialLoading)
    val faqUiState: StateFlow<FAQUiState> = _faqUiState.asStateFlow()

    private val _lostUiState: MutableStateFlow<LostUiState> =
        MutableStateFlow(LostUiState(content = LostUiState.Content.InitialLoading))
    val lostUiState: StateFlow<LostUiState> = _lostUiState.asStateFlow()

    private var noticeIdToExpand: Long? = null

    init {
        loadAllNotices(NoticeUiState(content = NoticeUiState.Content.InitialLoading))
        loadAllFAQs(FAQUiState.InitialLoading)
        loadAllLostItems(LostUiState(content = LostUiState.Content.InitialLoading))
    }

    fun loadAllNotices(state: NoticeUiState) {
        viewModelScope.launch {
            _noticeUiState.value = state
            val result = noticeRepository.fetchNotices()
            result
                .onSuccess { notices ->
                    val updatedNotices =
                        notices.map {
                            it.toUiModel().let { notice ->
                                if (notice.id == noticeIdToExpand) notice.copy(isExpanded = true) else notice
                            }
                        }
                    val expandPosition =
                        notices.indexOfFirst { it.id == noticeIdToExpand }.let {
                            if (it == -1) DEFAULT_POSITION else it
                        }
                    _noticeUiState.value =
                        NoticeUiState(
                            content =
                                NoticeUiState.Content.Success(updatedNotices, expandPosition),
                        )
                    noticeIdToExpand = null
                }.onFailure {
                    _noticeUiState.value = NoticeUiState(content = NoticeUiState.Content.Error(it))
                }
        }
    }

    fun toggleNotice(notice: NoticeUiModel) {
        updateNoticeUiState { notices ->
            notices.map { updatedNotice ->
                if (notice.id == updatedNotice.id) {
                    updatedNotice.copy(isExpanded = !updatedNotice.isExpanded)
                } else {
                    updatedNotice
                }
            }
        }
    }

    fun expandNotice(noticeIdToExpand: Long) {
        this.noticeIdToExpand = noticeIdToExpand
        if (_noticeUiState.value.content !is NoticeUiState.Content.InitialLoading) {
            loadAllNotices(_noticeUiState.value)
        }
    }

    fun toggleFAQ(faqItem: FAQItemUiModel) {
        updateFAQUiState { faqItems ->
            faqItems.map { updatedFAQItem ->
                if (faqItem.questionId == updatedFAQItem.questionId) {
                    updatedFAQItem.copy(isExpanded = !updatedFAQItem.isExpanded)
                } else {
                    updatedFAQItem
                }
            }
        }
    }

    fun toggleLostGuide() {
        updateLostUiState { lostUiModels ->
            lostUiModels.map { lostUiModel ->
                if (lostUiModel is LostUiModel.Guide) {
                    lostUiModel.copy(isExpanded = !lostUiModel.isExpanded)
                } else {
                    lostUiModel
                }
            }
        }
    }

    fun loadAllLostItems(state: LostUiState) {
        viewModelScope.launch {
            _lostUiState.value = state
            val result = lostItemRepository.getLost()

            val lostUiModels =
                result.map { lost ->
                    when (lost) {
                        is Lost.Guide -> lost.toLostGuideItemUiModel()
                        is Lost.Item -> lost.toLostItemUiModel()
                        null -> LostUiModel.Guide()
                    }
                }
            _lostUiState.value = LostUiState(content = LostUiState.Content.Success(lostUiModels))
        }
    }

    private fun loadAllFAQs(state: FAQUiState) {
        viewModelScope.launch {
            _faqUiState.value = state

            val result = faqRepository.getAllFAQ()

            result
                .onSuccess { faqItems ->
                    _faqUiState.value = FAQUiState.Success(faqItems.map { it.toUiModel() })
                }.onFailure {
                    _faqUiState.value = FAQUiState.Error(it)
                }
        }
    }

    private fun updateNoticeUiState(onUpdate: (List<NoticeUiModel>) -> List<NoticeUiModel>) {
        val currentState = _noticeUiState.value
        _noticeUiState.value =
            when (val currentContent = currentState.content) {
                is NoticeUiState.Content.Success -> {
                    currentState.copy(
                        content =
                            currentContent.copy(notices = onUpdate(currentContent.notices)),
                    )
                }

                else -> {
                    return
                }
            }
    }

    private fun updateFAQUiState(onUpdate: (List<FAQItemUiModel>) -> List<FAQItemUiModel>) {
        val currentState = _faqUiState.value
        _faqUiState.value =
            when (currentState) {
                is FAQUiState.Success -> currentState.copy(faqs = onUpdate(currentState.faqs))
                else -> currentState
            }
    }

    private fun updateLostUiState(onUpdate: (List<LostUiModel>) -> List<LostUiModel>) {
        val currentState = _lostUiState.value
        val currentContent = currentState.content
        _lostUiState.value =
            when (currentContent) {
                is LostUiState.Content.Success -> {
                    _lostUiState.value.copy(
                        content = currentContent.copy(lostItems = onUpdate(currentContent.lostItems)),
                    )
                }

                else -> {
                    currentState
                }
            }
    }
}
