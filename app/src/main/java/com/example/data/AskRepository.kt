package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AskRepository(private val dao: AskDao) {
    val allPartners: Flow<List<CscPartner>> = dao.getAllPartners()
    val allAppointments: Flow<List<Appointment>> = dao.getAllAppointments()
    val allPayments: Flow<List<CommissionPayment>> = dao.getAllPayments()

    init {
        // Run database seeding asynchronously on launch if database is empty
        CoroutineScope(Dispatchers.IO).launch {
            seedIfNeeded()
        }
    }

    fun getAppointmentsForPartner(partnerId: String): Flow<List<Appointment>> {
        return dao.getAppointmentsByPartner(partnerId)
    }

    fun getPaymentsForPartner(partnerId: String): Flow<List<CommissionPayment>> {
        return dao.getPaymentsForPartner(partnerId)
    }

    suspend fun getPartnerById(partnerId: String): CscPartner? {
        return dao.getPartnerById(partnerId)
    }

    suspend fun insertPartner(partner: CscPartner) {
        dao.insertPartner(partner)
    }

    suspend fun updatePartner(partner: CscPartner) {
        dao.updatePartner(partner)
    }

    suspend fun deletePartner(partnerId: String) {
        dao.deletePartner(partnerId)
    }

    suspend fun getAppointmentById(id: Int): Appointment? {
        return dao.getAppointmentById(id)
    }

    suspend fun insertAppointment(appointment: Appointment): Long {
        return dao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        dao.updateAppointment(appointment)
    }

    suspend fun deleteAppointmentById(id: Int) {
        dao.deleteAppointmentById(id)
    }

    suspend fun insertPayment(payment: CommissionPayment) {
        dao.insertPayment(payment)
    }

    suspend fun getSetting(key: String): String? {
        return dao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        dao.insertSetting(AppSetting(key, value))
    }

    private suspend fun seedIfNeeded() {
        val seeded = dao.getSetting("seeded")
        if (seeded == null) {
            // Seed Settings
            dao.insertSetting(AppSetting("commission_amount", "100"))
            dao.insertSetting(AppSetting("admin_username", "admin"))
            dao.insertSetting(AppSetting("admin_password", "admin123"))
            dao.insertSetting(AppSetting("centre_name", "ASK Aadhaar Seva Kendra, Indore"))
            dao.insertSetting(AppSetting("centre_address", "88, Mahatma Gandhi Road, Indore, MP"))
            dao.insertSetting(AppSetting("centre_phone", "+91 9988776655"))
            dao.insertSetting(AppSetting("centre_email", "ask.indore@uidai.gov.in"))
            dao.insertSetting(AppSetting("seeded", "true"))

            // Seed Partners
            val partner1 = CscPartner(partnerId = "CSC1001", name = "Rohit Sharma", password = "csc123", isActive = true)
            val partner2 = CscPartner(partnerId = "CSC1002", name = "Ananya Patel", password = "csc123", isActive = true)
            val partner3 = CscPartner(partnerId = "CSC1003", name = "Vikram Singh", password = "csc123", isActive = false) // Suspended

            dao.insertPartner(partner1)
            dao.insertPartner(partner2)
            dao.insertPartner(partner3)

            // Seed Appointments
            val app1 = Appointment(
                appointmentId = "ASK-2026-1001",
                tokenNumber = "T-001",
                customerName = "Amit Kumar",
                mobileNumber = "9876543210",
                aadhaarMasked = "XXXX-XXXX-9876",
                fatherHusbandName = "Sohan Lal",
                dob = "1990-05-15",
                gender = "Male",
                village = "Aland",
                address = "Main Road, Aland",
                pinCode = "485001",
                serviceType = "Mobile Update",
                status = "APPROVED",
                partnerId = "CSC1001",
                appointmentDate = "2026-07-02",
                appointmentTime = "10:00 AM",
                qrCodeData = "ASK-2026-1001|Amit Kumar|Mobile Update|T-001",
                remarks = "Please process quickly.",
                cscStampPath = "simulated_stamp_path"
            )

            val app2 = Appointment(
                appointmentId = "ASK-2026-1002",
                tokenNumber = "T-002",
                customerName = "Priya Sharma",
                mobileNumber = "9123456789",
                aadhaarMasked = "XXXX-XXXX-4321",
                fatherHusbandName = "Rajesh Sharma",
                dob = "1995-12-22",
                gender = "Female",
                village = "Katni",
                address = "Ward 4, Katni",
                pinCode = "483501",
                serviceType = "Photograph Update",
                status = "REJECTED",
                rejectionReason = "CSC Stamp unclear and supporting documents are incomplete.",
                partnerId = "CSC1002",
                appointmentDate = "2026-07-01",
                appointmentTime = "11:30 AM",
                remarks = "Urgent biometric card reprint.",
                cscStampPath = "simulated_stamp_path"
            )

            val app3 = Appointment(
                appointmentId = "",
                tokenNumber = "",
                customerName = "Siddharth Jain",
                mobileNumber = "9898989898",
                aadhaarMasked = "XXXX-XXXX-5555",
                fatherHusbandName = "Vikas Jain",
                dob = "1988-08-08",
                gender = "Male",
                village = "Vijay Nagar",
                address = "123, Vijay Nagar, Indore",
                pinCode = "452010",
                serviceType = "New Aadhaar Enrollment",
                status = "PENDING",
                partnerId = "CSC1001",
                appointmentDate = "2026-07-03",
                appointmentTime = "02:00 PM",
                remarks = "First time enrollment.",
                cscStampPath = "simulated_stamp_path"
            )

            val app4 = Appointment(
                appointmentId = "",
                tokenNumber = "",
                customerName = "Komal Verma",
                mobileNumber = "9765432109",
                aadhaarMasked = "XXXX-XXXX-1111",
                fatherHusbandName = "Sunil Verma",
                dob = "2002-04-14",
                gender = "Female",
                village = "Pipariya",
                address = "Near Station, Pipariya",
                pinCode = "461775",
                serviceType = "Biometric Update",
                status = "PENDING",
                partnerId = "CSC1002",
                appointmentDate = "2026-07-03",
                appointmentTime = "04:30 PM",
                remarks = "Fingerprint update required.",
                cscStampPath = "simulated_stamp_path"
            )

            val app5 = Appointment(
                appointmentId = "ASK-2026-1005",
                tokenNumber = "T-003",
                customerName = "Rakesh Mishra",
                mobileNumber = "9445566778",
                aadhaarMasked = "XXXX-XXXX-7777",
                fatherHusbandName = "Jagdish Mishra",
                dob = "1985-02-10",
                gender = "Male",
                village = "Indore",
                address = "45, Palasia, Indore",
                pinCode = "452001",
                serviceType = "Address Update",
                status = "APPROVED",
                partnerId = "CSC1001",
                appointmentDate = "2026-07-02",
                appointmentTime = "12:15 PM",
                qrCodeData = "ASK-2026-1005|Rakesh Mishra|Address Update|T-003",
                remarks = "Updated utility bill attached.",
                cscStampPath = "simulated_stamp_path"
            )

            dao.insertAppointment(app1)
            dao.insertAppointment(app2)
            dao.insertAppointment(app3)
            dao.insertAppointment(app4)
            dao.insertAppointment(app5)

            // Seed Commission Payments
            dao.insertPayment(CommissionPayment(partnerId = "CSC1001", amount = 100.0, referenceId = "TXN998811", remarks = "June Commission"))
            dao.insertPayment(CommissionPayment(partnerId = "CSC1002", amount = 100.0, referenceId = "TXN998812", remarks = "June Commission"))
        }
    }
}
