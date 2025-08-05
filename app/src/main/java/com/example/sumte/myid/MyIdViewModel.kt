package com.example.sumte.myid

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sumte.ApiClient
import com.example.sumte.login.AuthService
import kotlinx.coroutines.launch
class MyIdViewModel : ViewModel() {

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    fun updateNickname(nickname: String) {
        viewModelScope.launch {
            try {
                Log.d("MyIdViewModel", "닉네임 변경 요청 시도: $nickname")
                val response = ApiClient.authService.updateNickname(
                    token = "",
                    request = AuthService.UpdateNicknameRequest(nickname)
                )
                Log.d("MyIdViewModel", "응답 코드: ${response.code()}")
                _updateResult.value = response.isSuccessful
            } catch (e: Exception) {
                Log.e("MyIdViewModel", "닉네임 변경 중 에러", e)
                _updateResult.value = false
            }
        }
    }

//    fun updateNickname(nickname: String) {
//        viewModelScope.launch {
//            try {
//                val response = ApiClient.authService.updateNickname(
//                    token = "", // Interceptor가 자동으로 Authorization 추가
//                    request = AuthService.UpdateNicknameRequest(nickname)
//                )
//                _updateResult.value = response.isSuccessful
//            } catch (e: Exception) {
//                _updateResult.value = false
//            }
//        }
//    }
}
