package com.msa.core.base



//class UserRepository(networkHandler: NetworkHandler) : BaseRepository(networkHandler) {
//
//    suspend fun getUsers(): NetworkResult<List<User>> {
//        return safeGetRequest("https://api.example.com/users")
//    }
//
//    suspend fun createUser(user: User): NetworkResult<User> {
//        return safePostRequest("https://api.example.com/users", user)
//    }
//}
//
//class UserViewModel(private val userRepository: UserRepository) : BaseViewModel() {
//
//    init {
//        fetchUsers()
//    }
//
//    private fun fetchUsers() {
//        viewModelScope.launch {
//            setLoading()
//            when (val result = userRepository.getUsers()) {
//                is NetworkResult.Success -> setSuccess(result.data)
//                is NetworkResult.Error -> setError(result.message)
//            }
//        }
//    }
//}
//
//
//
//
//class MainActivity : BaseActivity() {
//
//    private val userViewModel: UserViewModel by viewModel()
//
//    override fun SetContent() {
//        MaterialTheme {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//                color = MaterialTheme.colorScheme.background
//            ) {
//                UserScreen(userViewModel)
//            }
//        }
//    }
//}


//class UserRepository(
//    private val networkHandler: NetworkHandler,
//    private val databaseService: DatabaseService,
//    private val fileService: FileService,
//    private val context: Context
//) : BaseRepository(networkHandler) {
//
//    suspend fun getUsers(): NetworkResult<List<User>> {
//        return try {
//            val networkResult = safeGetRequest<List<User>>("https://api.example.com/users")
//            if (networkResult is NetworkResult.Success) {
//                networkResult.data.forEach { user ->
//                    databaseService.saveUserToDatabase(
//                        UserEntity(id = user.id, name = user.name, email = user.email)
//                    )
//                }
//            }
//            networkResult
//        } catch (e: Exception) {
//            NetworkResult.Error(e, "Error fetching users from the network")
//        }
//    }
//}
//
//class UserViewModel(private val userRepository: UserRepository) : BaseViewModel() {
//
//    init {
//        fetchUsers()
//    }
//
//    private fun fetchUsers() {
//        viewModelScope.launch {
//            setLoading()
//            when (val result = userRepository.getUsers()) {
//                is NetworkResult.Success -> setSuccess(result.data)
//                is NetworkResult.Error -> setError(result.message)
//            }
//        }
//    }
//}
//
//@Composable
//fun UserScreen(viewModel: UserViewModel = viewModel()) {
//    val userState by viewModel.uiState.collectAsState()
//
//    when (userState) {
//        is UIState.Loading -> LoadingScreen()
//        is UIState.Success -> UserList((userState as UIState.Success).data)
//        is UIState.Error -> ErrorScreen((userState as UIState.Error).message)
//    }
//}
//
//@Composable
//fun LoadingScreen() {
//    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//        CircularProgressIndicator()
//    }
//}
//
//@Composable
//fun UserList(users: List<User>) {
//    LazyColumn {
//        items(users) { user ->
//            Text(text = "${user.name} - ${user.email}")
//        }
//    }
//}
//
//@Composable
//fun ErrorScreen(errorMessage: String) {
//    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//        Text(text = errorMessage)
//    }
//}