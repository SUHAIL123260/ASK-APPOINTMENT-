package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AadhaarDao {
    // === Aadhaar Profile ===
    @Query("SELECT * FROM aadhaar_profiles WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<AadhaarProfile?>

    @Query("SELECT * FROM aadhaar_profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): AadhaarProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: AadhaarProfile)

    // === Update Requests ===
    @Query("SELECT * FROM update_requests ORDER BY requestDateMillis DESC")
    fun getAllUpdateRequests(): Flow<List<UpdateRequest>>

    @Query("SELECT * FROM update_requests ORDER BY requestDateMillis DESC LIMIT :limit")
    fun getRecentUpdateRequests(limit: Int = 5): Flow<List<UpdateRequest>>

    @Query("""
        SELECT * FROM update_requests 
        WHERE urnNumber LIKE '%' || :query || '%' 
           OR updateType LIKE '%' || :query || '%' 
           OR updateTypeHindi LIKE '%' || :query || '%' 
           OR newValue LIKE '%' || :query || '%' 
        ORDER BY requestDateMillis DESC
    """)
    fun searchUpdateRequests(query: String): Flow<List<UpdateRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdateRequest(req: UpdateRequest): Long

    @Query("DELETE FROM update_requests WHERE id = :id")
    suspend fun deleteUpdateRequestById(id: Int)

    @Query("DELETE FROM update_requests")
    suspend fun clearAllUpdateRequests()

    // === PVC Orders ===
    @Query("SELECT * FROM pvc_orders ORDER BY orderDateMillis DESC")
    fun getAllPvcOrders(): Flow<List<PvcOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPvcOrder(order: PvcOrder): Long

    @Query("DELETE FROM pvc_orders WHERE id = :id")
    suspend fun deletePvcOrderById(id: Int)

    // === Settings ===
    @Query("SELECT * FROM aadhaar_settings WHERE id = 1 LIMIT 1")
    fun getSettingFlow(): Flow<AadhaarSetting?>

    @Query("SELECT * FROM aadhaar_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingSync(): AadhaarSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: AadhaarSetting)
}

