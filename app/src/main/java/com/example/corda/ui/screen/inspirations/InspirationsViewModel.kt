package com.example.corda.ui.screen.inspirations

import androidx.lifecycle.ViewModel
import com.example.corda.data.inspirations.model.Inspiration
import com.example.corda.data.inspirations.model.InspirationAttribute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class InspirationsUiState(
    val inspirations: List<Inspiration> = emptyList(),
    val searchQuery: String = "",
    val selectedLabel: String? = null,
    val availableLabels: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class InspirationDetailUiState(
    val inspiration: Inspiration? = null,
    val isLoading: Boolean = false
)

data class InspirationEditUiState(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val labels: List<String> = emptyList(),
    val attributes: List<InspirationAttribute> = emptyList(),
    val imageUri: String? = null,
    val availableLabels: List<String> = emptyList(),
    val isSaving: Boolean = false
)

@HiltViewModel
class InspirationsViewModel @Inject constructor() : ViewModel() {

    private val inspirationsData = mutableListOf<Inspiration>()
    private val labelsData = mutableListOf("Want to learn", "Song", "Learned", "Image", "Something")

    private val _listState = MutableStateFlow(
        InspirationsUiState(
            inspirations = inspirationsData.toList(),
            availableLabels = labelsData.toList()
        )
    )
    val listState: StateFlow<InspirationsUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(InspirationDetailUiState())
    val detailState: StateFlow<InspirationDetailUiState> = _detailState.asStateFlow()

    private val _editState = MutableStateFlow(InspirationEditUiState())
    val editState: StateFlow<InspirationEditUiState> = _editState.asStateFlow()

    fun setSearchQuery(query: String) {
        _listState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedLabel(label: String?) {
        _listState.update { it.copy(selectedLabel = label) }
    }

    fun loadInspiration(id: String) {
        val inspiration = inspirationsData.find { it.id == id }
        _detailState.update { it.copy(inspiration = inspiration) }
    }

    fun startEditing(id: String?) {
        val available = labelsData.toList()
        if (id == null) {
            _editState.update { InspirationEditUiState(availableLabels = available) }
        } else {
            val inspiration = inspirationsData.find { it.id == id }
            _editState.update {
                InspirationEditUiState(
                    id = inspiration?.id,
                    name = inspiration?.name ?: "",
                    description = inspiration?.description ?: "",
                    labels = inspiration?.labels ?: emptyList(),
                    attributes = inspiration?.attributes ?: emptyList(),
                    imageUri = inspiration?.imageUri,
                    availableLabels = available
                )
            }
        }
    }

    fun updateEditName(name: String) {
        _editState.update { it.copy(name = name) }
    }

    fun updateEditDescription(description: String) {
        _editState.update { it.copy(description = description) }
    }

    fun addLabel(label: String) {
        _editState.update { it.copy(labels = it.labels + label) }
    }

    fun removeLabel(label: String) {
        _editState.update { it.copy(labels = it.labels - label) }
    }

    fun createLabel(label: String) {
        if (label.isNotBlank() && !labelsData.contains(label)) {
            labelsData.add(label)
            val updated = labelsData.toList()
            _listState.update { it.copy(availableLabels = updated) }
            _editState.update { it.copy(availableLabels = updated) }
        }
    }

    fun addAttribute(attribute: InspirationAttribute) {
        _editState.update { it.copy(attributes = it.attributes + attribute) }
    }

    fun updateAttribute(attribute: InspirationAttribute) {
        _editState.update { state ->
            val idx = state.attributes.indexOfFirst { it.id == attribute.id }
            if (idx >= 0) {
                state.copy(attributes = state.attributes.toMutableList().also { it[idx] = attribute })
            } else {
                state
            }
        }
    }

    fun removeAttribute(id: String) {
        _editState.update { state ->
            state.copy(attributes = state.attributes.filter { it.id != id })
        }
    }

    fun updateLabel(oldLabel: String, newLabel: String) {
        if (newLabel.isBlank() || oldLabel == newLabel) return
        val idx = labelsData.indexOf(oldLabel)
        if (idx < 0) return
        labelsData[idx] = newLabel
        val updatedInspirations = inspirationsData.map { inspiration ->
            inspiration.copy(labels = inspiration.labels.map { if (it == oldLabel) newLabel else it })
        }
        inspirationsData.clear()
        inspirationsData.addAll(updatedInspirations)
        val updated = labelsData.toList()
        _listState.update {
            it.copy(
                availableLabels = updated,
                inspirations = inspirationsData.toList(),
                selectedLabel = if (it.selectedLabel == oldLabel) newLabel else it.selectedLabel
            )
        }
        _editState.update { state ->
            state.copy(
                availableLabels = updated,
                labels = state.labels.map { if (it == oldLabel) newLabel else it }
            )
        }
    }

    fun deleteLabel(label: String) {
        labelsData.remove(label)
        val updatedInspirations = inspirationsData.map { inspiration ->
            inspiration.copy(labels = inspiration.labels.filter { it != label })
        }
        inspirationsData.clear()
        inspirationsData.addAll(updatedInspirations)
        val updated = labelsData.toList()
        _listState.update {
            it.copy(
                availableLabels = updated,
                inspirations = inspirationsData.toList(),
                selectedLabel = if (it.selectedLabel == label) null else it.selectedLabel
            )
        }
        _editState.update { state ->
            state.copy(
                availableLabels = updated,
                labels = state.labels.filter { it != label }
            )
        }
    }

    fun deleteInspiration(id: String, onDeleted: () -> Unit) {
        inspirationsData.removeAll { it.id == id }
        _listState.update { it.copy(inspirations = inspirationsData.toList()) }
        onDeleted()
    }

    fun saveInspiration(onSaved: () -> Unit) {
        val edit = _editState.value
        val inspiration = Inspiration(
            id = edit.id ?: UUID.randomUUID().toString(),
            name = edit.name,
            description = edit.description,
            labels = edit.labels,
            attributes = edit.attributes,
            imageUri = edit.imageUri
        )
        val existingIdx = inspirationsData.indexOfFirst { it.id == inspiration.id }
        if (existingIdx >= 0) {
            inspirationsData[existingIdx] = inspiration
        } else {
            inspirationsData.add(0, inspiration)
        }
        _listState.update { it.copy(inspirations = inspirationsData.toList()) }
        onSaved()
    }
}
