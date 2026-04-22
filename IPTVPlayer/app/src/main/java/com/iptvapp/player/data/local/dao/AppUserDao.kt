package com.iptvapp.player.data.local.dao

import androidx.room.*
import com.iptvapp.player.data.local.entity.AppUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserDao {

    @Query("SELECT * FROM app_users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<AppUserEntity>>

    @Query("SELECT * FROM app_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): AppUserEntity?

    @Query("SELECT * FROM app_users WHERE xtreamUsername = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AppUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUserEntity): Long

    @Update
    suspend fun updateUser(user: AppUserEntity)

    @Query("DELETE FROM app_users WHERE id = :id")
    suspend fun deleteUser(id: Int)

    @Query("SELECT COUNT(*) FROM app_users")
    suspend fun getUserCount(): Int
}
