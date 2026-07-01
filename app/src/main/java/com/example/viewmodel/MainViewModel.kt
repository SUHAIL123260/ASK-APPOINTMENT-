package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AskRepository(database.askDao())

    // === UI States ===
    val partners: StateFlow<List<CscPartner>> = repository.allPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<CommissionPayment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Logged In Session ===
    private val _currentRole = MutableStateFlow<String?>(null) // "ADMIN", "PARTNER", null
    val currentRole: StateFlow<String?> = _currentRole.asStateFlow()

    private val _currentPartnerId = MutableStateFlow<String?>(null)
    val currentPartnerId: StateFlow<String?> = _currentPartnerId.asStateFlow()

    private val _currentPartnerName = MutableStateFlow<String?>(null)
    val currentPartnerName: StateFlow<String?> = _currentPartnerName.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // === Live Settings ===
    private val _commissionAmount = MutableStateFlow(100.0)
    val commissionAmount: StateFlow<Double> = _commissionAmount.asStateFlow()

    private val _centreName = MutableStateFlow("Authorized Aadhaar Seva Kendra (ASK)")
    val centreName: StateFlow<String> = _centreName.asStateFlow()

    private val _centreAddress = MutableStateFlow("Update Centre Address in Admin Settings, India")
    val centreAddress: StateFlow<String> = _centreAddress.asStateFlow()

    private val _centrePhone = MutableStateFlow("+91 9988776655")
    val centrePhone: StateFlow<String> = _centrePhone.asStateFlow()

    private val _centreEmail = MutableStateFlow("support.ask@uidai.gov.in")
    val centreEmail: StateFlow<String> = _centreEmail.asStateFlow()

    private val _centreLat = MutableStateFlow(22.7196)
    val centreLat: StateFlow<Double> = _centreLat.asStateFlow()

    private val _centreLng = MutableStateFlow(75.8577)
    val centreLng: StateFlow<Double> = _centreLng.asStateFlow()

    // Filter properties for Partner and Admin dashboards
    private val _appointmentSearchQuery = MutableStateFlow("")
    val appointmentSearchQuery: StateFlow<String> = _appointmentSearchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL") // ALL, PENDING, APPROVED, REJECTED
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSetting("commission_amount")?.toDoubleOrNull()?.let {
                _commissionAmount.value = it
            }
            repository.getSetting("centre_name")?.let { _centreName.value = it }
            repository.getSetting("centre_address")?.let { _centreAddress.value = it }
            repository.getSetting("centre_phone")?.let { _centrePhone.value = it }
            repository.getSetting("centre_email")?.let { _centreEmail.value = it }
            repository.getSetting("centre_lat")?.toDoubleOrNull()?.let { _centreLat.value = it }
            repository.getSetting("centre_lng")?.toDoubleOrNull()?.let { _centreLng.value = it }
        }
    }

    // === Authentication Operations ===
    fun loginAdmin(username: String, pword: String): Boolean {
        _loginError.value = null
        if (username.lowercase() == "admin" && pword == "admin123") {
            _currentRole.value = "ADMIN"
            _currentPartnerId.value = null
            _currentPartnerName.value = "Admin ASK Centre"
            return true
        } else {
            _loginError.value = "Invalid Admin Username or Password"
            return false
        }
    }

    fun loginPartner(partnerId: String, pword: String) {
        viewModelScope.launch {
            _loginError.value = null
            val partner = repository.getPartnerById(partnerId.uppercase())
            if (partner == null) {
                _loginError.value = "CSC Partner ID not found"
            } else if (!partner.isActive) {
                _loginError.value = "Your CSC Partner account has been Suspended/Deactivated. Contact Admin."
            } else if (partner.password != pword) {
                _loginError.value = "Incorrect Password"
            } else {
                _currentRole.value = "PARTNER"
                _currentPartnerId.value = partner.partnerId
                _currentPartnerName.value = partner.name
            }
        }
    }

    fun logout() {
        _currentRole.value = null
        _currentPartnerId.value = null
        _currentPartnerName.value = null
        _loginError.value = null
    }

    // === CSC Partner Management ===
    fun createCscPartner(partnerId: String, name: String, pword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val upperId = partnerId.uppercase().trim()
            if (upperId.isEmpty() || name.trim().isEmpty() || pword.trim().isEmpty()) {
                onError("All fields are mandatory")
                return@launch
            }
            val existing = repository.getPartnerById(upperId)
            if (existing != null) {
                onError("CSC Partner ID already exists")
                return@launch
            }
            val newPartner = CscPartner(
                partnerId = upperId,
                name = name.trim(),
                password = pword,
                isActive = true
            )
            repository.insertPartner(newPartner)
            onSuccess()
        }
    }

    fun deletePartner(partnerId: String) {
        viewModelScope.launch {
            repository.deletePartner(partnerId)
        }
    }

    fun togglePartnerStatus(partnerId: String) {
        viewModelScope.launch {
            val partner = repository.getPartnerById(partnerId) ?: return@launch
            val updated = partner.copy(isActive = !partner.isActive)
            repository.updatePartner(updated)
        }
    }

    fun resetPartnerPassword(partnerId: String, newPword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val partner = repository.getPartnerById(partnerId) ?: return@launch
            val updated = partner.copy(password = newPword)
            repository.updatePartner(updated)
            onSuccess()
        }
    }

    // === Appointment Management ===
    fun createAppointment(
        customerName: String,
        mobileNumber: String,
        aadhaarNumber: String,
        fatherHusbandName: String,
        dob: String,
        gender: String,
        village: String,
        address: String,
        pinCode: String,
        serviceType: String,
        photographPath: String?,
        aadhaarCopyPath: String?,
        supportingDocPath: String?,
        cscStampPath: String?,
        remarks: String?,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val partnerId = _currentPartnerId.value
            if (partnerId == null) {
                onError("No logged-in partner session")
                return@launch
            }
            if (customerName.trim().isEmpty() || mobileNumber.trim().isEmpty() || aadhaarNumber.trim().isEmpty() ||
                fatherHusbandName.trim().isEmpty() || dob.trim().isEmpty() || gender.trim().isEmpty() ||
                village.trim().isEmpty() || address.trim().isEmpty() || pinCode.trim().isEmpty() || serviceType.isEmpty()
            ) {
                onError("All fields except remarks are required")
                return@launch
            }
            if (cscStampPath == null) {
                onError("CSC Stamp upload is mandatory for Partners")
                return@launch
            }

            // Clean Aadhaar format (e.g. extract last 4 digits and mask)
            val cleanAadhaar = aadhaarNumber.replace(" ", "").replace("-", "")
            if (cleanAadhaar.length != 12) {
                onError("Aadhaar Number must be 12 digits")
                return@launch
            }
            val maskedAadhaar = "XXXX-XXXX-${cleanAadhaar.takeLast(4)}"

            val tempApp = Appointment(
                appointmentId = "", // Pending approval
                tokenNumber = "",   // Pending approval
                customerName = customerName.trim(),
                mobileNumber = mobileNumber.trim(),
                aadhaarMasked = maskedAadhaar,
                fatherHusbandName = fatherHusbandName.trim(),
                dob = dob,
                gender = gender,
                village = village.trim(),
                address = address.trim(),
                pinCode = pinCode.trim(),
                serviceType = serviceType,
                photographPath = photographPath ?: "simulated_photo",
                aadhaarCopyPath = aadhaarCopyPath ?: "simulated_aadhaar_copy",
                supportingDocPath = supportingDocPath ?: "simulated_supporting_doc",
                cscStampPath = cscStampPath,
                remarks = remarks?.trim(),
                status = "PENDING",
                partnerId = partnerId,
                appointmentDate = "", // Set on approval
                appointmentTime = ""  // Set on approval
            )

            val insertedId = repository.insertAppointment(tempApp).toInt()
            onSuccess(insertedId)
        }
    }

    fun approveAppointment(appointmentId: Int, selectedDate: String, selectedTime: String) {
        viewModelScope.launch {
            val app = repository.getAppointmentById(appointmentId) ?: return@launch
            val formatId = String.format("%04d", appointmentId)
            val generatedAppId = "ASK-${Calendar.getInstance().get(Calendar.YEAR)}-$formatId"
            
            // Calculate a token number for the day
            val todayApps = appointments.value.filter { 
                it.status == "APPROVED" && it.appointmentDate == selectedDate 
            }
            val generatedToken = "T-${String.format("%03d", todayApps.size + 1)}"
            
            val qrCodeContent = "$generatedAppId|$generatedToken|${app.customerName}|${app.serviceType}|$selectedDate|$selectedTime"

            val updated = app.copy(
                status = "APPROVED",
                appointmentId = generatedAppId,
                tokenNumber = generatedToken,
                appointmentDate = selectedDate,
                appointmentTime = selectedTime,
                qrCodeData = qrCodeContent,
                rejectionReason = null
            )
            repository.updateAppointment(updated)
        }
    }

    fun rejectAppointment(appointmentId: Int, reason: String) {
        viewModelScope.launch {
            val app = repository.getAppointmentById(appointmentId) ?: return@launch
            val updated = app.copy(
                status = "REJECTED",
                rejectionReason = reason.trim().ifEmpty { "Documents incomplete or invalid" }
            )
            repository.updateAppointment(updated)
        }
    }

    fun rescheduleAppointment(appointmentId: Int, date: String, time: String) {
        viewModelScope.launch {
            val app = repository.getAppointmentById(appointmentId) ?: return@launch
            val updated = app.copy(
                appointmentDate = date,
                appointmentTime = time
            )
            repository.updateAppointment(updated)
        }
    }

    fun deleteAppointment(id: Int) {
        viewModelScope.launch {
            repository.deleteAppointmentById(id)
        }
    }

    // === Commission Operations ===
    fun payCommission(partnerId: String, amount: Double, referenceId: String?, remarks: String?) {
        viewModelScope.launch {
            val newPayment = CommissionPayment(
                partnerId = partnerId,
                amount = amount,
                referenceId = referenceId?.trim() ?: "TXN${System.currentTimeMillis().toString().takeLast(6)}",
                remarks = remarks?.trim() ?: "Commission Paid"
            )
            repository.insertPayment(newPayment)
        }
    }

    fun updateApplicationSettings(commission: Double, name: String, address: String, phone: String, email: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.saveSetting("commission_amount", commission.toString())
            repository.saveSetting("centre_name", name)
            repository.saveSetting("centre_address", address)
            repository.saveSetting("centre_phone", phone)
            repository.saveSetting("centre_email", email)
            repository.saveSetting("centre_lat", lat.toString())
            repository.saveSetting("centre_lng", lng.toString())

            _commissionAmount.value = commission
            _centreName.value = name
            _centreAddress.value = address
            _centrePhone.value = phone
            _centreEmail.value = email
            _centreLat.value = lat
            _centreLng.value = lng
        }
    }

    // === Filter & Helper Methods ===
    fun updateSearchQuery(query: String) {
        _appointmentSearchQuery.value = query
    }

    fun updateStatusFilter(filter: String) {
        _selectedStatusFilter.value = filter
    }

    // Get earned and paid commission for partners
    fun getPartnerCommissionStats(pId: String): PartnerCommissionStats {
        val partnerApps = appointments.value.filter { it.partnerId == pId && it.status == "APPROVED" }
        val rate = commissionAmount.value
        val totalEarned = partnerApps.size * rate
        
        val totalPaid = payments.value.filter { it.partnerId == pId }.sumOf { it.amount }
        val balanceDue = totalEarned - totalPaid

        return PartnerCommissionStats(
            totalAppointments = partnerApps.size,
            totalEarned = totalEarned,
            totalPaid = totalPaid,
            balanceDue = balanceDue,
            lastPaidDate = payments.value.filter { it.partnerId == pId }.maxOfOrNull { it.paymentDate }
        )
    }

    // Reports data models
    fun getDailyReport(): List<ReportRow> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val grouped = appointments.value.groupBy { 
            // Defaulting date format or parse from createdAt
            dateFormat.format(Date(it.createdAt))
        }
        return grouped.map { (date, list) ->
            ReportRow(
                timeframe = date,
                total = list.size,
                approved = list.count { it.status == "APPROVED" },
                rejected = list.count { it.status == "REJECTED" },
                pending = list.count { it.status == "PENDING" },
                commission = list.count { it.status == "APPROVED" } * commissionAmount.value
            )
        }.sortedByDescending { it.timeframe }
    }

    fun getMonthlyReport(): List<ReportRow> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val grouped = appointments.value.groupBy { 
            monthFormat.format(Date(it.createdAt))
        }
        return grouped.map { (month, list) ->
            ReportRow(
                timeframe = month,
                total = list.size,
                approved = list.count { it.status == "APPROVED" },
                rejected = list.count { it.status == "REJECTED" },
                pending = list.count { it.status == "PENDING" },
                commission = list.count { it.status == "APPROVED" } * commissionAmount.value
            )
        }.sortedByDescending { it.timeframe }
    }
}

data class PartnerCommissionStats(
    val totalAppointments: Int,
    val totalEarned: Double,
    val totalPaid: Double,
    val balanceDue: Double,
    val lastPaidDate: Long?
)

data class ReportRow(
    val timeframe: String,
    val total: Int,
    val approved: Int,
    val rejected: Int,
    val pending: Int,
    val commission: Double
)
