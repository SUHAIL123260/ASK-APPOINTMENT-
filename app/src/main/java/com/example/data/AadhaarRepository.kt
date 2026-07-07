package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AadhaarRepository(private val dao: AadhaarDao) {

    val profileFlow: Flow<AadhaarProfile> = dao.getProfileFlow().map { it ?: AadhaarProfile() }
    val allUpdateRequests: Flow<List<UpdateRequest>> = dao.getAllUpdateRequests()
    val recentUpdateRequests: Flow<List<UpdateRequest>> = dao.getRecentUpdateRequests(5)
    val allPvcOrders: Flow<List<PvcOrder>> = dao.getAllPvcOrders()
    
    val settingFlow: Flow<AadhaarSetting> = dao.getSettingFlow().map { it ?: AadhaarSetting() }

    fun searchUpdateRequests(query: String): Flow<List<UpdateRequest>> = dao.searchUpdateRequests(query)

    suspend fun saveProfile(profile: AadhaarProfile) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun saveUpdateRequest(req: UpdateRequest): Long {
        return dao.insertUpdateRequest(req)
    }

    suspend fun deleteUpdateRequestById(id: Int) = dao.deleteUpdateRequestById(id)

    suspend fun clearAllUpdateRequests() = dao.clearAllUpdateRequests()

    suspend fun savePvcOrder(order: PvcOrder): Long {
        return dao.insertPvcOrder(order)
    }

    suspend fun deletePvcOrderById(id: Int) = dao.deletePvcOrderById(id)

    suspend fun saveSetting(setting: AadhaarSetting) {
        dao.insertOrUpdateSetting(setting)
    }

    suspend fun seedDefaultDataIfEmpty() {
        val currentProfile = dao.getProfileSync()
        if (currentProfile == null) {
            dao.insertOrUpdateProfile(AadhaarProfile())
        }

        val currentReqs = dao.getAllUpdateRequests().first()
        if (currentReqs.isEmpty()) {
            val defaultReqs = listOf(
                UpdateRequest(
                    urnNumber = "URN-8472-0192-3847",
                    updateType = "Address Update",
                    updateTypeHindi = "पता अपडेट",
                    oldValue = "Old Delhi Market, Chandni Chowk, Delhi - 110006",
                    newValue = "H.No 42, Green Park, Commercial Road, New Delhi - 110001",
                    requestDate = "02 Jul 2026, 04:15 PM",
                    status = "APPROVED",
                    stepProgress = 4,
                    remarks = "Address verification completed via electricity bill document simulation."
                ),
                UpdateRequest(
                    urnNumber = "URN-9182-4451-0091",
                    updateType = "Mobile Link Update",
                    updateTypeHindi = "मोबाइल नंबर लिंक",
                    oldValue = "+91 98111 00000",
                    newValue = "+91 98765 43210",
                    requestDate = "18 Jun 2026, 11:30 AM",
                    status = "APPROVED",
                    stepProgress = 4,
                    remarks = "OTP authentication simulation verified successfully."
                ),
                UpdateRequest(
                    urnNumber = "URN-7362-9918-2231",
                    updateType = "Biometric Unlock Demo",
                    updateTypeHindi = "बायोमैट्रिक अनलॉक",
                    oldValue = "Locked",
                    newValue = "Unlocked",
                    requestDate = "05 Jul 2026, 09:00 AM",
                    status = "APPROVED",
                    stepProgress = 4,
                    remarks = "Fingerprint sensor simulation authenticated."
                )
            )
            defaultReqs.forEach { dao.insertUpdateRequest(it) }
        }

        val currentOrders = dao.getAllPvcOrders().first()
        if (currentOrders.isEmpty()) {
            dao.insertPvcOrder(
                PvcOrder(
                    orderId = "SRN83920192",
                    speedPostTracking = "IN847291837IN",
                    orderDate = "04 Jul 2026",
                    amountPaid = 50.0,
                    deliveryAddress = "H.No 42, Green Park, Commercial Road, New Delhi - 110001",
                    orderStatus = "DISPATCHED",
                    estimatedDelivery = "10 Jul 2026"
                )
            )
        }

        val setting = dao.getSettingSync()
        if (setting == null) {
            dao.insertOrUpdateSetting(AadhaarSetting())
        }
    }
}

