package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "csc_partners")
data class CscPartner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerId: String, // e.g. "CSC1001"
    val name: String,
    val password: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appointmentId: String, // generated on approval, e.g. "ASK-2026-1002"
    val tokenNumber: String,   // generated on approval, e.g. "T-102"
    val customerName: String,
    val mobileNumber: String,
    val aadhaarMasked: String, // e.g. "XXXX-XXXX-1234"
    val fatherHusbandName: String,
    val dob: String,
    val gender: String,
    val village: String,
    val address: String,
    val pinCode: String,
    val serviceType: String,
    val photographPath: String? = null,
    val aadhaarCopyPath: String? = null,
    val supportingDocPath: String? = null,
    val cscStampPath: String? = null, // Mandatory
    val remarks: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String? = null,
    val partnerId: String, // CSC Partner ID who created it
    val appointmentDate: String, // "YYYY-MM-DD" or similar
    val appointmentTime: String, // "HH:MM AM/PM"
    val qrCodeData: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commission_payments")
data class CommissionPayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerId: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val referenceId: String?,
    val remarks: String?
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
