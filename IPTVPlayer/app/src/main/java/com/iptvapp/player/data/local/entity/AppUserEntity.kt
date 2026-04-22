package com.iptvapp.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_users")
data class AppUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val displayName: String,
    val serverUrl: String,
    val xtreamUsername: String,
    val xtreamPassword: String,
    val accessPin: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
