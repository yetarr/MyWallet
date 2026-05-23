package pt.ipcb.mywallet.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.local.entity.UserEntity
import pt.ipcb.mywallet.data.repository.UserRepository
import java.io.File

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Int) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserRepository(AppDatabase.getInstance(app).userDao())
    private val session = SessionManager(app)

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            val user = repo.findByEmailAndPassword(email.trim(), password)
            _state.value = if (user != null) {
                session.userId = user.id
                AuthState.Success(user.id)
            } else {
                AuthState.Error("Email ou palavra-passe incorretos")
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        currency: String,
        profilePhotoUri: Uri? = null,
    ) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            if (repo.findByEmail(email.trim()) != null) {
                _state.value = AuthState.Error("Este email já está registado")
                return@launch
            }
            val photoPath = profilePhotoUri?.let { copyPhotoToInternalStorage(it) }
            val id = repo.insert(
                UserEntity(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    password = password,
                    currency = currency,
                    profilePhotoPath = photoPath,
                )
            )
            session.userId = id.toInt()
            _state.value = AuthState.Success(id.toInt())
        }
    }

    private suspend fun copyPhotoToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(getApplication<Application>().filesDir, "profile_${System.currentTimeMillis()}.jpg")
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }.getOrNull()
    }

    fun resetState() { _state.value = AuthState.Idle }
}
