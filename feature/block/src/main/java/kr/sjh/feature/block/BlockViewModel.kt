package kr.sjh.feature.block

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.sjh.core.model.BlockUser
import kr.sjh.data.repository.BlockRepository
import kr.sjh.feature.navigation.Block
import kr.sjh.feature.navigation.BlockEvent
import javax.inject.Inject

data class BlockUiState(
    val isLoading: Boolean = false,
    val blockUsers: List<BlockUser> = emptyList(),
    val selectedUser: BlockUser? = null
)

@HiltViewModel
class BlockViewModel @Inject constructor(
    private val blockRepository: BlockRepository, savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val params = savedStateHandle.toRoute<Block>()

    private val _uiState = MutableStateFlow(BlockUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getBlockUsers()
    }

    private fun getBlockUsers() {
        viewModelScope.launch {
            blockRepository.getBlockUsers(params.userId, { list ->
                _uiState.update {
                    it.copy(blockUsers = list)
                }
            }, { e ->
                e.printStackTrace()
            })
        }
    }

    private fun deleteBlockedUser() {
        val user = _uiState.value.selectedUser ?: return
        viewModelScope.launch {
            blockRepository.deleteBlockedUser(user.blockerUser, user.blockedUser, {
                _uiState.update {
                    it.copy(blockUsers = it.blockUsers.toMutableList().apply {
                        removeIf {
                            it.blockerUser == user.blockerUser && it.blockedUser == user.blockedUser
                        }
                    })
                }
            }, { e ->
                e.printStackTrace()
            })
        }
    }

    fun onEvent(event: BlockEvent) {
        when (event) {
            BlockEvent.DeleteBlockUser -> {
                deleteBlockedUser()
            }

            is BlockEvent.SelectedBlockedUser -> {
                selectedBlockUser(event.blockedUser)
            }
        }
    }

    private fun selectedBlockUser(user: BlockUser) {
        _uiState.value = _uiState.value.copy(selectedUser = user)
    }
}