package com.msa.zarpakhsh.data.local.dao

import androidx.room.*
import com.msa.zarpakhsh.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    @Query("DELETE FROM user")
    suspend fun clearUser()

    @Query("SELECT EXISTS(SELECT 1 FROM user)")
    fun observeIsLoggedIn(): Flow<Boolean>
}