package pt.ipcb.mywallet.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.data.local.AppDatabase
import pt.ipcb.mywallet.data.local.entity.UserEntity
import pt.ipcb.mywallet.data.repository.UserRepository
import java.io.File

class EditProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserRepository(AppDatabase.getInstance(app).userDao())
    private val session = SessionManager(app)

    val user: StateFlow<UserEntity?> = repo.getById(session.userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(
        firstName: String,
        lastName: String,
        email: String,
        newPassword: String,
        currency: String,
        profilePhotoUri: Uri?,
        currentUser: UserEntity,
    ) {
        viewModelScope.launch {
            val photoPath = if (profilePhotoUri != null) copyPhotoToInternalStorage(profilePhotoUri)
                            else currentUser.profilePhotoPath
            repo.update(
                currentUser.copy(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim(),
                    password = if (newPassword.isBlank()) currentUser.password else newPassword,
                    currency = currency,
                    profilePhotoPath = photoPath,
                )
            )
            _saved.value = true
        }
    }

    fun resetSaved() { _saved.value = false }

    private suspend fun copyPhotoToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(getApplication<Application>().filesDir, "profile_${System.currentTimeMillis()}.jpg")
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }.getOrNull()
    }
}
