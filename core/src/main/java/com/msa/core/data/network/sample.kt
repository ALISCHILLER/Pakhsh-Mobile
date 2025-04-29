//package com.msa.core.network
//
//import com.msa.core.network.handler.NetworkHandler
//import com.msa.core.network.handler.NetworkResult
//import com.msa.core.network.model.User
//
//class UserRepository(private val networkHandler: NetworkHandler) {
//
//    suspend fun getUsers(): NetworkResult<List<User>> {
//        return networkHandler.getRequest("https://api.example.com/users")
//    }
//
//    suspend fun createUser(user: User): NetworkResult<User> {
//        return networkHandler.postRequest("https://api.example.com/users", user)
//    }
//}
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.msa.core.network.handler.NetworkResult
//import com.msa.core.network.repository.UserRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
//
//    // StateFlow برای مدیریت وضعیت داده‌ها
//    private val _userState = MutableStateFlow<NetworkResult<List<User>>>(NetworkResult.Loading)
//    val userState: StateFlow<NetworkResult<List<User>>> = _userState
//
//    init {
//        fetchUsers()
//    }
//
//    // درخواست کاربران از API
//    private fun fetchUsers() {
//        viewModelScope.launch {
//            _userState.value = NetworkResult.Loading
//            _userState.value = userRepository.getUsers()
//        }
//    }
//}
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.msa.core.network.handler.NetworkResult
//import com.msa.core.network.model.User
//import com.msa.core.ui.viewmodel.UserViewModel
//
//@Composable
//fun UserScreen(viewModel: UserViewModel = viewModel()) {
//    // دریافت وضعیت از ViewModel
//    val userState by viewModel.userState.collectAsState()
//
//    // نمایش وضعیت
//    when (userState) {
//        is NetworkResult.Loading -> {
//            // نمایش انیمیشن بارگذاری
//            LoadingScreen()
//        }
//        is NetworkResult.Success -> {
//            // نمایش لیست کاربران
//            val users = (userState as NetworkResult.Success).data
//            UserList(users)
//        }
//        is NetworkResult.Error -> {
//            // نمایش خطای شبکه
//            val errorMessage = (userState as NetworkResult.Error).message
//            ErrorScreen(errorMessage ?: "An unknown error occurred")
//        }
//    }
//}
//
//@Composable
//fun LoadingScreen() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = androidx.compose.ui.Alignment.Center
//    ) {
//        CircularProgressIndicator()
//    }
//}
//
//@Composable
//fun UserList(users: List<User>) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        items(users) { user ->
//            UserItem(user)
//        }
//    }
//}
//
//@Composable
//fun UserItem(user: User) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodyMedium)
//            Text(text = "Name: ${user.name}", style = MaterialTheme.typography.bodyMedium)
//            Text(text = "Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
//        }
//    }
//}
//
//@Composable
//fun ErrorScreen(errorMessage: String) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = androidx.compose.ui.Alignment.Center
//    ) {
//        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
//            Text(text = "Error", style = MaterialTheme.typography.headlineSmall)
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
//        }
//    }
//}