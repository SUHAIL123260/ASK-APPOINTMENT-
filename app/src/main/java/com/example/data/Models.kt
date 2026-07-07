package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "aadhaar_profiles")
data class AadhaarProfile(
    @PrimaryKey val id: Int = 1,
    val aadhaarNumber: String = "8472 9183 7890",
    val virtualId: String = "9182 7364 5501 8293",
    val nameEnglish: String = "Rahul Kumar",
    val nameHindi: String = "राहुल कुमार",
    val dob: String = "15/08/1995",
    val gender: String = "Male", // Male, Female, Other
    val genderHindi: String = "पुरुष",
    val fatherName: String = "Rajesh Kumar",
    val fatherNameHindi: String = "राजेश कुमार",
    val addressEnglish: String = "H.No 42, Green Park, Commercial Road, New Delhi - 110001",
    val addressHindi: String = "मकान नंबर 42, ग्रीन पार्क, कमर्शियल रोड, नई दिल्ली - 110001",
    val pinCode: String = "110001",
    val mobileNumber: String = "+91 98765 43210",
    val email: String = "rahul.kumar@example.com",
    val avatarId: Int = 1, // 1 to 6 sample avatars
    val isBiometricLocked: Boolean = false,
    val linkedBankName: String = "State Bank of India",
    val bankLinkStatus: String = "Active",
    val bankLinkDate: String = "12/03/2023",
    val isCloudSynced: Boolean = true
) {
    fun getFormattedMaskedAadhaar(): String {
        return "XXXX XXXX " + (aadhaarNumber.takeLast(4).ifEmpty { "7890" })
    }

    fun getMaskedAadhaar(): String = getFormattedMaskedAadhaar()

    fun getPasswordHint(): String {
        val first4 = nameEnglish.filter { it.isLetter() }.take(4).uppercase().padEnd(4, 'X')
        val year = if (dob.length >= 4) dob.takeLast(4) else "1995"
        return "$first4$year"
    }
}

@Entity(tableName = "update_requests")
data class UpdateRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val urnNumber: String = "URN-" + (1000..9999).random() + "-" + (100..999).random() + "-" + (1000..9999).random(),
    val updateType: String = "Address Update", // Demographics (Name, DOB, Gender, Address), Mobile Link, Biometric Lock
    val updateTypeHindi: String = "पता अपडेट",
    val fieldUpdated: String = "Address",
    val oldValue: String = "",
    val newValue: String = "",
    val proofDocument: String = "Voter ID Card",
    val requestDate: String = "06 Jul 2026, 10:30 AM",
    val requestDateMillis: Long = System.currentTimeMillis(),
    val status: String = "APPROVED", // SUBMITTED, IN_REVIEW, APPROVED, REJECTED
    val stepProgress: Int = 4, // 1 (Doc Uploaded), 2 (Verification), 3 (Validation), 4 (Approved & Updated)
    val remarks: String = "Verification completed successfully by UIDAI simulation server."
)

@Entity(tableName = "pvc_orders")
data class PvcOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: String = "SRN" + (10000000..99999999).random(),
    val speedPostTracking: String = "IN" + (100000000..999999999).random() + "IN",
    val orderDate: String = "06 Jul 2026",
    val orderDateMillis: Long = System.currentTimeMillis(),
    val amountPaid: Double = 50.0,
    val deliveryAddress: String = "",
    val orderStatus: String = "DISPATCHED", // PRINTING, DISPATCHED, IN_TRANSIT, DELIVERED
    val estimatedDelivery: String = "12 Jul 2026"
)

@Entity(tableName = "aadhaar_settings")
data class AadhaarSetting(
    @PrimaryKey val id: Int = 1,
    val portalTitle: String = "MeriPehchaan • Aadhaar Studio",
    val portalName: String = "MeriPehchaan • Aadhaar Studio",
    val portalSubtitle: String = "UIDAI Digital Identity Demo & Update Simulator",
    val defaultLanguage: String = "English",
    val autoLockBiometrics: Boolean = false,
    val maskAadhaarByDefault: Boolean = false,
    val enableSmsAlerts: Boolean = true,
    val allowOfflineVerification: Boolean = true,
    val demoModeEnabled: Boolean = true,
    val autoApproveUpdates: Boolean = true, // When true, update requests get APPROVED immediately in demo mode!
    val enableBiometricVoicePrompt: Boolean = true,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val securityPin: String = "1234"
)

