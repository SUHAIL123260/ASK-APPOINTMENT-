package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.PdfAadhaarGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AadhaarRepository(database.aadhaarDao())

    // === Database State Flows ===
    val profile: StateFlow<AadhaarProfile> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AadhaarProfile())

    val allUpdateRequests: StateFlow<List<UpdateRequest>> = repository.allUpdateRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentUpdateRequests: StateFlow<List<UpdateRequest>> = repository.recentUpdateRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPvcOrders: StateFlow<List<PvcOrder>> = repository.allPvcOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val setting: StateFlow<AadhaarSetting> = repository.settingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AadhaarSetting())

    // === Search & Filtering ===
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredUpdateRequests: StateFlow<List<UpdateRequest>> = combine(allUpdateRequests, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.urnNumber.contains(query, ignoreCase = true) ||
            it.updateType.contains(query, ignoreCase = true) ||
            it.updateTypeHindi.contains(query, ignoreCase = true) ||
            it.newValue.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Interactive UI State ===
    private val _isCardBackSide = MutableStateFlow(false)
    val isCardBackSide: StateFlow<Boolean> = _isCardBackSide.asStateFlow()

    private val _isNumberMasked = MutableStateFlow(true)
    val isNumberMasked: StateFlow<Boolean> = _isNumberMasked.asStateFlow()

    private val _isBiometricAuthenticating = MutableStateFlow(false)
    val isBiometricAuthenticating: StateFlow<Boolean> = _isBiometricAuthenticating.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastActionMessage = MutableStateFlow<String?>(null)
    val lastActionMessage: StateFlow<String?> = _lastActionMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearActionMessage() {
        _lastActionMessage.value = null
    }

    fun toggleCardSide() {
        _isCardBackSide.value = !_isCardBackSide.value
    }

    fun toggleNumberMask() {
        _isNumberMasked.value = !_isNumberMasked.value
    }

    // === Profile & Demographics Update ===
    fun updateProfileDemographics(
        nameEng: String,
        nameHin: String,
        dob: String,
        gender: String,
        genderHin: String,
        addrEng: String,
        addrHin: String,
        mobile: String,
        email: String
    ) {
        viewModelScope.launch {
            val current = profile.value
            val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
            val urn = "URN-" + (1000..9999).random() + "-" + (100..999).random() + "-" + (1000..9999).random()

            // Determine what changed for update history
            val changes = mutableListOf<String>()
            val changesHin = mutableListOf<String>()
            if (current.nameEnglish != nameEng) { changes.add("Name"); changesHin.add("नाम") }
            if (current.dob != dob) { changes.add("DOB"); changesHin.add("जन्मतिथि") }
            if (current.addressEnglish != addrEng) { changes.add("Address"); changesHin.add("पता") }
            if (current.mobileNumber != mobile) { changes.add("Mobile"); changesHin.add("मोबाइल") }

            val updateTypeStr = if (changes.isNotEmpty()) changes.joinToString(", ") + " Update" else "Demographics Update"
            val updateTypeHinStr = if (changesHin.isNotEmpty()) changesHin.joinToString(", ") + " अपडेट" else "जनसांख्यिकी अपडेट"

            val req = UpdateRequest(
                urnNumber = urn,
                updateType = updateTypeStr,
                updateTypeHindi = updateTypeHinStr,
                oldValue = "Previous Profile Details",
                newValue = "$nameEng ($dob, $addrEng)",
                requestDate = nowStr,
                status = "APPROVED",
                stepProgress = 4,
                remarks = "UIDAI Demo Simulator auto-validated documents and applied changes instantly!"
            )
            repository.saveUpdateRequest(req)

            // Update profile
            val updatedProfile = current.copy(
                nameEnglish = nameEng.trim(),
                nameHindi = nameHin.trim().ifEmpty { nameEng },
                dob = dob.trim(),
                gender = gender.trim(),
                genderHindi = genderHin.trim().ifEmpty { gender },
                addressEnglish = addrEng.trim(),
                addressHindi = addrHin.trim().ifEmpty { addrEng },
                mobileNumber = mobile.trim(),
                email = email.trim()
            )
            repository.saveProfile(updatedProfile)

            _lastActionMessage.value = "✅ Demographics Updated Successfully • URN: $urn"
        }
    }

    fun updateAvatar(avatarId: Int) {
        viewModelScope.launch {
            val updated = profile.value.copy(avatarId = avatarId)
            repository.saveProfile(updated)
            _lastActionMessage.value = "📸 Profile Avatar Changed Successfully"
        }
    }

    fun toggleBiometricLock(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isBiometricAuthenticating.value = true
            _lastActionMessage.value = "🔍 Simulating Biometric & UIDAI Server Verification..."
            delay(1500)
            _isBiometricAuthenticating.value = false

            val current = profile.value
            val newLockedState = !current.isBiometricLocked
            val updated = current.copy(isBiometricLocked = newLockedState)
            repository.saveProfile(updated)

            val nowStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date())
            val urn = "URN-" + (1000..9999).random() + "-" + (100..999).random() + "-" + (1000..9999).random()
            val req = UpdateRequest(
                urnNumber = urn,
                updateType = if (newLockedState) "Biometric Lock Demo" else "Biometric Unlock Demo",
                updateTypeHindi = if (newLockedState) "बायोमैट्रिक लॉक" else "बायोमैट्रिक अनलॉक",
                oldValue = if (newLockedState) "Unlocked" else "Locked",
                newValue = if (newLockedState) "Locked" else "Unlocked",
                requestDate = nowStr,
                status = "APPROVED",
                stepProgress = 4,
                remarks = if (newLockedState) "Biometrics locked to prevent unauthorized SIM & OTP access." else "Biometrics temporarily unlocked for 10 minutes."
            )
            repository.saveUpdateRequest(req)

            _lastActionMessage.value = if (newLockedState) {
                "🔐 Biometrics LOCKED • Your Aadhaar is protected from SIM spoofing!"
            } else {
                "🔓 Biometrics UNLOCKED • Authentication enabled for practice mode!"
            }
            onSuccess(newLockedState)
        }
    }

    fun orderPvcCard(deliveryAddress: String) {
        viewModelScope.launch {
            val nowStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
            val order = PvcOrder(
                orderId = "SRN" + (10000000..99999999).random(),
                speedPostTracking = "IN" + (100000000..999999999).random() + "IN",
                orderDate = nowStr,
                amountPaid = 50.0,
                deliveryAddress = deliveryAddress.ifBlank { profile.value.addressEnglish },
                orderStatus = "DISPATCHED",
                estimatedDelivery = "5 Days via Speed Post"
            )
            repository.savePvcOrder(order)
            _lastActionMessage.value = "💳 PVC Card Ordered! Tracking: ${order.speedPostTracking}"
        }
    }

    fun deleteUpdateRequest(req: UpdateRequest) {
        viewModelScope.launch {
            repository.deleteUpdateRequestById(req.id)
            _lastActionMessage.value = "🗑️ Deleted Request: ${req.urnNumber}"
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllUpdateRequests()
            _lastActionMessage.value = "✨ Update history cleared"
        }
    }

    fun updateSettings(newSetting: AadhaarSetting) {
        viewModelScope.launch {
            repository.saveSetting(newSetting)
            _lastActionMessage.value = "⚙️ Portal Settings Updated"
        }
    }

    fun simulateCloudSync() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            _lastActionMessage.value = "☁️ Synchronizing with MeriPehchaan Simulation Server..."
            delay(1600)
            _isCloudSyncing.value = false
            _lastActionMessage.value = "✅ Cloud Sync Complete • Digital Profile Verified!"
        }
    }

    // === PDF & Share Helpers ===
    fun shareEaadhaarPdf(whatsappOnly: Boolean = false) {
        val file = PdfAadhaarGenerator.generateEaadhaarPdf(getApplication(), profile.value, setting.value)
        PdfAadhaarGenerator.sharePdf(getApplication(), file, whatsappOnly)
        _lastActionMessage.value = "📤 Sharing e-Aadhaar PDF Demo (Password: ${profile.value.getPasswordHint()})"
    }

    fun printEaadhaar() {
        val file = PdfAadhaarGenerator.generateEaadhaarPdf(getApplication(), profile.value, setting.value)
        PdfAadhaarGenerator.printPdf(getApplication(), file, "e-Aadhaar_${profile.value.aadhaarNumber}")
        _lastActionMessage.value = "🖨️ Sent e-Aadhaar to Print Spooler"
    }

    // UI compatibility aliases & states
    val currentProfile: StateFlow<AadhaarProfile> = profile
    val portalSetting: StateFlow<AadhaarSetting> = setting
    val updateRequests: StateFlow<List<UpdateRequest>> = allUpdateRequests
    val pvcOrders: StateFlow<List<PvcOrder>> = allPvcOrders
    val isBiometricLocked: StateFlow<Boolean> = profile.map { it.isBiometricLocked }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isSyncing: StateFlow<Boolean> = isCloudSyncing
    val actionMessage: StateFlow<String?> = lastActionMessage

    private val _showCardModal = MutableStateFlow(false)
    val showCardModal: StateFlow<Boolean> = _showCardModal.asStateFlow()

    private val _selectedAvatar = MutableStateFlow("default_male")
    val selectedAvatar: StateFlow<String> = _selectedAvatar.asStateFlow()

    fun openCardModal() { _showCardModal.value = true }
    fun closeCardModal() { _showCardModal.value = false }

    fun syncWithUidai() = simulateCloudSync()
    fun downloadEAadhaar() = printEaadhaar()
    fun shareProfileDetails() = shareEaadhaarPdf(false)

    fun submitDemographicsUpdate(field: String, oldVal: String, newVal: String, proofDoc: String) {
        val curr = profile.value
        val nameEng = if (field == "Name") newVal else curr.nameEnglish
        val dob = if (field == "DOB/Gender") newVal.substringBefore(" (") else curr.dob
        val gender = if (field == "DOB/Gender") newVal.substringAfter(" (").removeSuffix(")") else curr.gender
        val addrEng = if (field == "Residential Address") newVal else curr.addressEnglish
        val mobile = if (field == "Mobile Number") newVal else curr.mobileNumber
        updateProfileDemographics(nameEng, curr.nameHindi, dob, gender, curr.genderHindi, addrEng, curr.addressHindi, mobile, curr.email)
    }

    fun toggleBiometricLock(newState: Boolean) {
        toggleBiometricLock { }
    }

    fun generateNewVirtualId() {
        viewModelScope.launch {
            val newVid = (1000..9999).random().toString() + " " + (1000..9999).random() + " " + (1000..9999).random() + " " + (1000..9999).random()
            val updated = profile.value.copy(virtualId = newVid)
            repository.saveProfile(updated)
            _lastActionMessage.value = "🔄 Generated New 16-Digit Virtual ID (VID)"
        }
    }

    fun updatePortalSetting(newSetting: AadhaarSetting) = updateSettings(newSetting)

    fun resetDemoData() {
        viewModelScope.launch {
            repository.clearAllUpdateRequests()
            repository.saveProfile(AadhaarProfile())
            _lastActionMessage.value = "✨ Simulator Reset to Default Profile (Rajesh Kumar)"
        }
    }

    fun updateProfileAvatar(avatarIdStr: String) {
        _selectedAvatar.value = avatarIdStr
        val idx = when(avatarIdStr) {
            "default_female" -> 2
            "student_boy" -> 3
            "student_girl" -> 4
            "senior_male" -> 5
            "senior_female" -> 6
            else -> 1
        }
        updateAvatar(idx)
    }
}

