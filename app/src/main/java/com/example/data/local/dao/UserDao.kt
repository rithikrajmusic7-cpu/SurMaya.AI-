package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0 WHERE id = :userId")
    suspend fun logoutUser(userId: String)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}
