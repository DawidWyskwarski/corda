package com.example.corda.ui.screen.inspirations

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.inspirations.local.emptyInspirationWithLabels
import com.example.corda.data.inspirations.local.labelNames
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.MediaType
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels
import com.example.corda.data.inspirations.media.InspirationMediaStore
import com.example.corda.data.inspirations.repository.InspirationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class InspirationsUiState(
    val inspirations: List<InspirationWithLabels> = emptyList(),
    val searchQuery: String = "",
    val selectedLabel: String? = null,
    val availableLabels: List<LabelEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class InspirationsViewModel @Inject constructor(
    private val repository: InspirationsRepository,
    private val mediaStore: InspirationMediaStore,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedLabel = MutableStateFlow<String?>(null)

    private val inspirationsFlow = repository.observeInspirations()
    private val labelsFlow = repository.observeLabels()

    val listState: StateFlow<InspirationsUiState> = combine(
        inspirationsFlow,
        labelsFlow,
        _searchQuery,
        _selectedLabel,
    ) { inspirations, labels, searchQuery, selectedLabel ->
        InspirationsUiState(
            inspirations = inspirations,
            searchQuery = searchQuery,
            selectedLabel = selectedLabel,
            availableLabels = labels,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InspirationsUiState(),
    )

    private val _detailInspiration = MutableStateFlow<InspirationWithLabels?>(null)
    val detailInspiration: StateFlow<InspirationWithLabels?> = _detailInspiration.asStateFlow()

    private val _editInspiration = MutableStateFlow(emptyInspirationWithLabels())
    val editInspiration: StateFlow<InspirationWithLabels> = _editInspiration.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _mediaImportError = MutableStateFlow(false)
    val mediaImportError: StateFlow<Boolean> = _mediaImportError.asStateFlow()

    private val thumbnailBackfillStarted = AtomicBoolean(false)

    init {
        viewModelScope.launch {
            val inspirations = inspirationsFlow.first { it.isNotEmpty() }
            if (thumbnailBackfillStarted.compareAndSet(false, true)) {
                repository.backfillMissingThumbnails(inspirations)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedLabel(label: String?) {
        _selectedLabel.value = label
    }

    fun clearMediaImportError() {
        _mediaImportError.value = false
    }

    fun loadInspiration(id: Long) {
        viewModelScope.launch {
            _detailInspiration.value = repository.getInspiration(id)
        }
    }

    fun startEditing(id: Long?) {
        viewModelScope.launch {
            _editInspiration.value = if (id == null) {
                emptyInspirationWithLabels()
            } else {
                repository.getInspiration(id) ?: emptyInspirationWithLabels()
            }
        }
    }

    fun updateEditName(name: String) {
        _editInspiration.update { current ->
            current.copy(inspiration = current.inspiration.copy(name = name))
        }
    }

    fun updateEditDescription(description: String) {
        _editInspiration.update { current ->
            current.copy(inspiration = current.inspiration.copy(description = description))
        }
    }

    fun addLabel(label: LabelEntity) {
        _editInspiration.update { current ->
            if (current.labels.any { it.labelId == label.labelId }) current
            else current.copy(labels = current.labels + label)
        }
    }

    fun removeLabel(label: LabelEntity) {
        _editInspiration.update { current ->
            current.copy(labels = current.labels.filter { it.labelId != label.labelId })
        }
    }

    fun setEditMedia(
        path: String,
        type: MediaType,
        thumbnailPath: String?,
        aspectRatio: Float?,
    ) {
        _editInspiration.update { current ->
            current.copy(
                inspiration = current.inspiration.copy(
                    mediaPath = path,
                    mediaType = type,
                    thumbnailPath = thumbnailPath,
                    mediaAspectRatio = aspectRatio,
                ),
            )
        }
    }

    fun clearEditMedia() {
        val current = _editInspiration.value.inspiration
        mediaStore.deleteManagedFiles(current.mediaPath, current.thumbnailPath)
        _editInspiration.update { state ->
            state.copy(
                inspiration = state.inspiration.copy(
                    mediaPath = null,
                    mediaType = null,
                    thumbnailPath = null,
                    mediaAspectRatio = null,
                ),
            )
        }
    }

    fun importEditMediaFromPicker(uri: Uri) {
        viewModelScope.launch {
            mediaStore.importFromUri(uri)
                .onSuccess { imported ->
                    val previous = _editInspiration.value.inspiration
                    if (previous.mediaPath != null && previous.mediaPath != imported.path) {
                        mediaStore.deleteManagedFiles(
                            previous.mediaPath,
                            previous.thumbnailPath,
                        )
                    }
                    setEditMedia(
                        path = imported.path,
                        type = imported.type,
                        thumbnailPath = imported.thumbnailPath,
                        aspectRatio = imported.aspectRatio,
                    )
                    _mediaImportError.value = false
                }
                .onFailure {
                    _mediaImportError.value = true
                }
        }
    }

    fun createLabel(label: String) {
        viewModelScope.launch {
            repository.createLabel(label)
        }
    }

    fun updateLabel(oldLabel: String, newLabel: String) {
        if (newLabel.isBlank() || oldLabel == newLabel) return
        viewModelScope.launch {
            repository.renameLabelByName(oldLabel, newLabel)
            if (_selectedLabel.value == oldLabel) {
                _selectedLabel.value = newLabel.trim()
            }
            refreshEditIfLabelRenamed(oldLabel, newLabel.trim())
        }
    }

    fun deleteLabel(label: String) {
        viewModelScope.launch {
            repository.deleteLabelByName(label)
            if (_selectedLabel.value == label) {
                _selectedLabel.value = null
            }
            _editInspiration.update { current ->
                current.copy(labels = current.labels.filter { it.name != label })
            }
        }
    }

    fun deleteInspiration(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteInspiration(id).onSuccess {
                if (_detailInspiration.value?.inspiration?.inspirationId == id) {
                    _detailInspiration.value = null
                }
                onDeleted()
            }
        }
    }

    fun saveInspiration(onSaved: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val edit = _editInspiration.value
            repository.saveInspiration(edit.inspiration, edit.labelNames)
                .onSuccess { onSaved() }
            _isSaving.value = false
        }
    }

    private fun refreshEditIfLabelRenamed(oldName: String, newName: String) {
        _editInspiration.update { current ->
            current.copy(
                labels = current.labels.map { label ->
                    if (label.name == oldName) label.copy(name = newName) else label
                },
            )
        }
    }
}
