package com.aviator.hoc.ui.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aviator.hoc.data.GenerateResult
import com.aviator.hoc.data.SignalRepository
import com.aviator.hoc.domain.GeneratePreview
import com.aviator.hoc.domain.normalizePasted
import com.aviator.hoc.domain.previewFor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenerateUiState(
    val input: String = "",
    val preview: GeneratePreview? = null,
    val generating: Boolean = false,
    val error: String? = null,
    /** true for ~2 s after a signal was added */
    val success: Boolean = false,
)

class GenerateViewModel(private val repository: SignalRepository) : ViewModel() {

    private val _state = MutableStateFlow(GenerateUiState())
    val state: StateFlow<GenerateUiState> = _state.asStateFlow()

    private var errorJob: Job? = null
    private var successJob: Job? = null

    fun onInput(text: String) {
        errorJob?.cancel()
        _state.update { it.copy(input = text, error = null, preview = previewFor(text)) }
    }

    fun onPasted(raw: String) = onInput(normalizePasted(raw))

    fun onClipboardEmpty() = showError("Clipboard is empty or blocked. Long-press the field and paste instead.", 3_000)

    fun submit() {
        val s = _state.value
        if (s.generating || s.input.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(generating = true, error = null) }
            when (val r = repository.generate(s.input)) {
                is GenerateResult.Success -> {
                    _state.update { it.copy(generating = false, input = "", preview = null, success = true) }
                    successJob?.cancel()
                    successJob = launch {
                        delay(2_000)
                        _state.update { it.copy(success = false) }
                    }
                }
                is GenerateResult.Error -> {
                    _state.update { it.copy(generating = false) }
                    showError(r.message, 5_000)
                }
            }
        }
    }

    private fun showError(message: String, millis: Long) {
        errorJob?.cancel()
        _state.update { it.copy(error = message) }
        errorJob = viewModelScope.launch {
            delay(millis)
            _state.update { it.copy(error = null) }
        }
    }
}
