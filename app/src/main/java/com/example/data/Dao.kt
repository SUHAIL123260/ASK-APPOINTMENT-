package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AskDao {
    // === CSC Partners ===
    @Query("SELECT * FROM csc_partners ORDER BY name ASC")
    fun getAllPartners(): Flow<List<CscPartner>>

    @Query("SELECT * FROM csc_partners WHERE partnerId = :partnerId LIMIT 1")
    suspend fun getPartnerById(partnerId: String): CscPartner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: CscPartner)

    @Update
    suspend fun updatePartner(partner: CscPartner)

    @Query("DELETE FROM csc_partners WHERE partnerId = :partnerId")
    suspend fun deletePartner(partnerId: String)

    // === Appointments ===
    @Query("SELECT * FROM appointments ORDER BY createdAt DESC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE partnerId = :partnerId ORDER BY createdAt DESC")
    fun getAppointmentsByPartner(partnerId: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Int): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Int)

    // === Commission Payments ===
    @Query("SELECT * FROM commission_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<CommissionPayment>>

    @Query("SELECT * FROM commission_payments WHERE partnerId = :partnerId ORDER BY paymentDate DESC")
    fun getPaymentsForPartner(partnerId: String): Flow<List<CommissionPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CommissionPayment)

    // === App Settings ===
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSetting)
}
