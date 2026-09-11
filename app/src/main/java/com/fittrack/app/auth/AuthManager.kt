package com.fittrack.app.auth

import android.content.Context
import com.fittrack.app.data.UserDao
import com.fittrack.app.data.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Gestiona la autenticación localmente usando Room y SharedPreferences.
 */
class AuthManager(context: Context, private val userDao: UserDao) {
    
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableStateFlow<String?>(prefs.getString("logged_in_email", null))
    val currentUser: StateFlow<String?> = _currentUser

    private val _currentUserId = MutableStateFlow<Long>(prefs.getLong("logged_in_id", -1L))
    val currentUserId: StateFlow<Long> = _currentUserId

    init {
        // Si hay email pero no ID (por migración), lo buscamos
        val email = prefs.getString("logged_in_email", null)
        val id = prefs.getLong("logged_in_id", -1L)
        if (email != null && id == -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                userDao.getUserByEmail(email)?.let {
                    prefs.edit().putLong("logged_in_id", it.id).apply()
                    _currentUserId.value = it.id
                }
            }
        }
    }

    fun signUp(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(false, "El email y la contraseña no pueden estar vacíos")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    onResult(false, "El usuario ya existe")
                }
                return@launch
            }

            val userId = userDao.insertUser(UserEntity(email = email, password = pass))
            withContext(Dispatchers.Main) {
                onResult(true, null)
            }
        }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(false, "El email y la contraseña no pueden estar vacíos")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val user = userDao.getUserByEmail(email)
            withContext(Dispatchers.Main) {
                if (user != null && user.password == pass) {
                    loginLocally(email, user.id)
                    onResult(true, null)
                } else {
                    onResult(false, "Email o contraseña incorrectos")
                }
            }
        }
    }

    private fun loginLocally(email: String, id: Long) {
        prefs.edit()
            .putString("logged_in_email", email)
            .putLong("logged_in_id", id)
            .apply()
        _currentUser.value = email
        _currentUserId.value = id
    }

    fun signOut() {
        prefs.edit()
            .remove("logged_in_email")
            .remove("logged_in_id")
            .apply()
        _currentUser.value = null
        _currentUserId.value = -1L
    }
}
