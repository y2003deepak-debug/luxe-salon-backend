package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalonViewModel(application: Application) : AndroidViewModel(application) {
    private val database = SalonDatabase.getDatabase(application)
    private val repository = SalonRepository(
        database.bookingDao(),
        database.stylistDao(),
        database.adminProfileDao(),
        database.salonServiceDao()
    )

    // Setup secure Retrofit connection mapped to internal high-speed mock network server
    private val apiService = RetrofitClient.clientFactory {
        // Comment out MockServerInterceptor to connect to the live Render backend directly
        // addInterceptor(MockServerInterceptor())
    }

    private val _syncState = MutableStateFlow("IDLE") // IDLE, SYNCING, SUCCESS, ERROR
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    private val _serverLogsFlow = MutableStateFlow<List<String>>(emptyList())
    val serverLogsFlow: StateFlow<List<String>> = _serverLogsFlow.asStateFlow()

    fun updateLogsFromInterceptor() {
        _serverLogsFlow.value = MockServerInterceptor.logs.toList()
    }

    fun syncDatabaseWithServer() {
        viewModelScope.launch {
            _syncState.value = "SYNCING"
            updateLogsFromInterceptor()
            try {
                // Fetch dynamic services from Remote server database using pure HTTP over Retrofit Client
                val serviceDtos = apiService.getServices()
                
                // Fetch dynamic stylists from Remote server using Retrofit
                val stylistDtos = apiService.getStylists()

                // Fetch dynamic bookings from Remote server
                val bookingDtos = apiService.getBookings()

                // Blow database tables local structures clean to merge with live Remote State
                repository.clearAllData()

                // Insert into local DB
                serviceDtos.forEach { dto ->
                     repository.insertService(dto.toRoomEntity())
                }
                stylistDtos.forEach { dto ->
                     repository.insertStylist(dto.toRoomEntity())
                }
                bookingDtos.forEach { dto ->
                     repository.insertBooking(dto.toRoomEntity())
                }

                _syncState.value = "SUCCESS"
                updateLogsFromInterceptor()
            } catch (e: Exception) {
                _syncState.value = "ERROR"
                updateLogsFromInterceptor()
            }
        }
    }

    fun clearServerDatabase() {
        viewModelScope.launch {
            MockServerInterceptor.remoteServices.clear()
            MockServerInterceptor.remoteStylists.clear()
            MockServerInterceptor.remoteBookings.clear()
            MockServerInterceptor.addLog("DELETE", "/api/all-tables", 204)
            updateLogsFromInterceptor()
            repository.clearAllData()
        }
    }

    // UI state flows
    val allStylists: StateFlow<List<Stylist>> = repository.allStylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminProfile: StateFlow<AdminProfile?> = repository.adminProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<SalonService>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated fields based on bookings
    val revenueToday: StateFlow<Double> = repository.allBookings
        .map { bookings ->
            bookings.filter { it.status == "Active" }.sumOf { it.priceEstimate }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activeBookingsCount: StateFlow<Int> = repository.allBookings
        .map { bookings ->
            bookings.count { it.status == "Active" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current navigation tab state (0: Home, 1: Booking Wizard, 2: Lookup, 3: Admin Dashboard)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Wizard wizard step state
    private val _wizardStep = MutableStateFlow(1)
    val wizardStep: StateFlow<Int> = _wizardStep.asStateFlow()

    // Booking Wizard Choices
    private val _selectedServices = MutableStateFlow<Set<String>>(emptySet())
    val selectedServices: StateFlow<Set<String>> = _selectedServices.asStateFlow()

    private val _selectedStylist = MutableStateFlow<String?>(null)
    val selectedStylist: StateFlow<String?> = _selectedStylist.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow<String?>(null)
    val selectedTime: StateFlow<String?> = _selectedTime.asStateFlow()

    // Personal info
    val nameInput = MutableStateFlow("")
    val phoneInput = MutableStateFlow("")
    val emailInput = MutableStateFlow("")
    val selectedCuttingStyleSubtype = MutableStateFlow("")

    // Lookup Screen states
    val lookupSearchText = MutableStateFlow("")
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val lookupSearchResults: StateFlow<List<Booking>> = lookupSearchText
        .debounce { 300 }
        .flatMapLatest { text ->
            if (text.isBlank()) flowOf(emptyList())
            else repository.getBookingsByPhone(text.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication States
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _isShowingSignUp = MutableStateFlow(false)
    val isShowingSignUp: StateFlow<Boolean> = _isShowingSignUp.asStateFlow()

    // SECURITY FIX (VULN-01): Never pre-populate credentials with hardcoded defaults.
    // Leaving these as empty strings forces the user to always type their credentials.
    val authEmail = MutableStateFlow("")
    val authPassword = MutableStateFlow("")
    val authName = MutableStateFlow("")
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val activeAdminSubTab = MutableStateFlow(0) // 0: Dashboard/Analytics, 1: Stylist Roster, 2: Bookings Master, 3: Portal Settings

    // Language Preference State
    private val sharedPrefs = application.getSharedPreferences("luxe_salon_prefs", Context.MODE_PRIVATE)
    private val _selectedLanguage = MutableStateFlow<String?>(sharedPrefs.getString("selected_language", null))
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
        sharedPrefs.edit().putString("selected_language", lang).apply()
    }

    private val _latestBookingReceipt = MutableStateFlow<Booking?>(null)
    val latestBookingReceipt: StateFlow<Booking?> = _latestBookingReceipt.asStateFlow()

    init {
        // Retain cached database contents on startup to support offline availability.
        // Syncing with the Render cloud database will refresh the local database cleanly if successful.
        viewModelScope.launch {
            // SECURITY FIX (VULN-01 / VULN-05): Do NOT auto-seed admin credentials into the local
            // database on startup. Admin profile should only be populated after a successful
            // server-side authentication. This prevents credential harvesting from the local DB.
            // Automatically sync with the Render cloud database on app startup
            syncDatabaseWithServer()
        }
    }

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
        _authError.value = null
    }

    fun setWizardStep(step: Int) {
        if (step in 1..5) {
            _wizardStep.value = step
        }
    }

    fun toggleService(serviceName: String) {
        val current = _selectedServices.value.toMutableSet()
        if (current.contains(serviceName)) {
            current.remove(serviceName)
        } else {
            current.add(serviceName)
        }
        _selectedServices.value = current
    }

    fun setStylist(stylistName: String) {
        _selectedStylist.value = stylistName
    }

    fun setDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setTimeSlot(timeSlot: String) {
        _selectedTime.value = timeSlot
    }

    // Clear Booking inputs
    fun resetBookingFlow() {
        _selectedServices.value = emptySet()
        _selectedStylist.value = null
        _selectedDate.value = null
        _selectedTime.value = null
        nameInput.value = ""
        phoneInput.value = ""
        emailInput.value = ""
        selectedCuttingStyleSubtype.value = ""
        _latestBookingReceipt.value = null
        _wizardStep.value = 1
    }

    private val _bookingError = MutableStateFlow<String?>(null)
    val bookingError: StateFlow<String?> = _bookingError.asStateFlow()

    fun clearBookingError() {
        _bookingError.value = null
    }

    // Submit Booking to Room database
    fun submitBooking() {
        var servicesList = _selectedServices.value.joinToString(", ")
        val cutSubtype = selectedCuttingStyleSubtype.value
        if (cutSubtype.isNotBlank() && _selectedServices.value.contains("Sculpted Cut")) {
            servicesList = servicesList.replace("Sculpted Cut", "Sculpted Cut ($cutSubtype)")
        }
        val stylist = _selectedStylist.value ?: "Mayank Sharma"
        val defaultDate = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.ENGLISH).run {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            format(cal.time)
        }
        val date = _selectedDate.value ?: defaultDate
        val time = _selectedTime.value ?: "02:30 PM"
        val phone = phoneInput.value.trim()
        val name = nameInput.value.trim()

        if (phone.isNotBlank() && name.isNotBlank()) {
            // --- Duplicate booking conflict check ---
            val timesCodeDefault = listOf("09:00 AM", "10:30 AM", "12:00 PM", "02:30 PM", "04:00 PM", "05:30 PM")
            val timesCode = getDynamicSlotsForStylistDate(
                stylistName = stylist,
                date = date,
                allSlots = timesCodeDefault
            )
            val newDuration = calculateTotalServiceDuration()
            val blockedSlots = getBlockedSlotsForStylistDate(
                stylistName = stylist,
                date = date,
                allSlots = timesCode,
                newServiceDurationMin = newDuration
            )
            if (blockedSlots.contains(time)) {
                _bookingError.value = "This time slot is already booked for $stylist on $date. Please choose a different time."
                return
            }
            // --- End conflict check ---

            val estimatedPrice = calculateEstimatePrice()
            viewModelScope.launch {
                val newBooking = Booking(
                    phoneNumber = phone,
                    clientName = name,
                    services = servicesList.ifBlank { "Signature Stylist Care" },
                    stylistName = stylist,
                    date = date,
                    timeSlot = time,
                    priceEstimate = estimatedPrice
                )
                repository.insertBooking(newBooking)
                try {
                    apiService.createBooking(BookingDto.fromRoomEntity(newBooking))
                } catch (e: Exception) {
                    // Soft fallback
                }
                updateLogsFromInterceptor()
                _bookingError.value = null
                _latestBookingReceipt.value = newBooking
                _wizardStep.value = 5 // Go to receipt confirmation
            }
        }
    }

    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            repository.updateBookingStatus(booking.id, "Cancelled")
            try {
                apiService.updateBookingStatus(booking.id, StatusUpdateDto("Cancelled"))
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    fun toggleStylistAvailability(stylist: Stylist) {
        viewModelScope.launch {
            repository.updateStylistAvailability(stylist.id, !stylist.isAvailable)
        }
    }

    fun saveAdminIdentity(newDisplayName: String, newGreeting: String) {
        viewModelScope.launch {
            val currentProfile = repository.adminProfile.first() ?: AdminProfile(id = 1, email = "admin@luxesalon.com", displayName = "", customGreeting = "")
            repository.saveAdminProfile(
                currentProfile.copy(
                    displayName = newDisplayName,
                    customGreeting = newGreeting
                )
            )
        }
    }

    // Simulating authentication
    fun setShowingSignUp(signUp: Boolean) {
        _isShowingSignUp.value = signUp
        _authError.value = null
    }

    fun loginAdmin() {
        val email = authEmail.value.trim()
        val password = authPassword.value.trim()

        if (email.isBlank() || password.isBlank()) {
            _authError.value = "Credentials cannot be blank"
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.loginAdmin(AdminLoginDto(email, password))
                if (response.success) {
                    _isAdminLoggedIn.value = true
                    _authError.value = null
                    
                    // Save or update AdminProfile locally
                    repository.saveAdminProfile(
                        AdminProfile(
                            id = 1,
                            email = response.email,
                            displayName = response.displayName,
                            customGreeting = response.customGreeting
                        )
                    )
                } else {
                    _authError.value = "Invalid ID or Password"
                }
            } catch (e: Exception) {
                // SECURITY FIX (VULN-15): Never expose raw exception messages to the UI —
                // they may contain internal server details, stack traces, or connection strings.
                _authError.value = "Login failed. Please check your credentials and try again."
            }
            updateLogsFromInterceptor()
        }
    }

    fun registerAdmin() {
        val email = authEmail.value.trim()
        val password = authPassword.value.trim()
        val name = authName.value.trim()

        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authError.value = "All fields are required"
            return
        }

        // SECURITY FIX (VULN-05): Admin registration MUST be verified server-side.
        // Never grant _isAdminLoggedIn = true based purely on local input with no server check.
        // The correct implementation requires the server to validate the registration request
        // and return a JWT that confirms the new admin account was created successfully.
        // For now: reject self-registration without a server-validated flow.
        // TODO: Implement server-side admin registration endpoint with email verification.
        _authError.value = "Self-registration is disabled. Contact the system administrator."
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        authEmail.value = ""
        authPassword.value = ""
        authName.value = ""
        _authError.value = null
        activeAdminSubTab.value = 0
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking)
            try {
                apiService.deleteBooking(booking.id)
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    fun updateBookingStatus(bookingId: Int, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
            try {
                apiService.updateBookingStatus(bookingId, StatusUpdateDto(status))
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    fun createStylist(name: String, specialty: String, isAvailable: Boolean, avatarColorIndex: Int, imageUrl: String? = null, awayUntilDate: String? = null, awayUntilTime: String? = null) {
        viewModelScope.launch {
            val stylistObj = Stylist(
                name = name,
                specialty = specialty,
                isAvailable = isAvailable,
                avatarColorIndex = avatarColorIndex,
                imageUrl = imageUrl,
                awayUntilDate = awayUntilDate,
                awayUntilTime = awayUntilTime
            )
            repository.insertStylist(stylistObj)
            try {
                apiService.createStylist(StylistDto.fromRoomEntity(stylistObj))
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    fun updateStylistAvailabilityWithTime(stylistId: Int, isAvailable: Boolean, awayUntilDate: String?, awayUntilTime: String?) {
        viewModelScope.launch {
            repository.updateStylistAvailabilityWithTime(stylistId, isAvailable, awayUntilDate, awayUntilTime)
        }
    }

    fun updateStylist(stylist: Stylist) {
        viewModelScope.launch {
            repository.insertStylist(stylist)
        }
    }

    fun deleteStylist(stylist: Stylist) {
        viewModelScope.launch {
            repository.deleteStylist(stylist)
            try {
                apiService.deleteStylist(stylist.id)
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    fun calculateEstimatePrice(): Double {
        var base = 0.0
        val selected = _selectedServices.value
        val servicesList = allServices.value
        for (sel in selected) {
            val matching = servicesList.find { it.name == sel }
            if (matching != null) {
                base += matching.price
            } else {
                // fallback for default/untracked names or subtypes
                if (sel.startsWith("Sculpted Cut")) {
                    base += 120.0
                } else if (sel.contains("Artisan Color")) {
                    base += 250.0
                } else if (sel.contains("Deep Hydration")) {
                    base += 85.0
                } else if (sel.contains("Signature Blowout")) {
                    base += 75.0
                }
            }
        }
        if (base == 0.0) base = 120.0 // Default package
        return base
    }

    fun createService(name: String, price: Double, description: String, durationMin: Int, nameHindi: String = "", suitability: String = "", isPremium: Boolean = false) {
        viewModelScope.launch {
            val serviceObj = SalonService(
                name = name, 
                price = price, 
                description = description, 
                durationMin = durationMin,
                nameHindi = nameHindi,
                suitability = suitability,
                isPremium = isPremium
            )
            repository.insertService(serviceObj)
            try {
                apiService.createService(SalonServiceDto.fromRoomEntity(serviceObj))
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    fun updateService(service: SalonService) {
        viewModelScope.launch {
            repository.updateService(service)
        }
    }

    fun deleteService(service: SalonService) {
        viewModelScope.launch {
            repository.deleteService(service)
            try {
                apiService.deleteService(service.id)
            } catch (e: Exception) {
                // Soft backend fallback
            }
            updateLogsFromInterceptor()
        }
    }

    // Clear all data from the database securely
    fun loadDemoWork() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    // ==========================================
    // SLOT CONFLICT DETECTION
    // Returns list of time slots that are already booked (Active) for a given stylist + date.
    // Also accounts for duration: if a slot starts at 9:00 AM for 20 min,
    // then 9:00 AM slot is blocked for ANY new booking that overlaps.
    // ==========================================
    fun getBlockedSlotsForStylistDate(
        stylistName: String,
        date: String,
        allSlots: List<String>,
        newServiceDurationMin: Int
    ): Set<String> {
        val bookings = allBookings.value
        val activeBookings = bookings.filter {
            it.stylistName == stylistName &&
            it.date == date &&
            it.status == "Active"
        }

        if (activeBookings.isEmpty()) return emptySet()

        val blocked = mutableSetOf<String>()

        // Parse time string like "09:00 AM" to minutes-since-midnight
        fun parseTimeToMinutes(timeStr: String): Int {
            return try {
                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
                val date2 = sdf.parse(timeStr.trim()) ?: return 0
                val cal = java.util.Calendar.getInstance().apply { time = date2 }
                cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
            } catch (e: Exception) { 0 }
        }

        // For each existing active booking: find its booked slot + duration
        for (booking in activeBookings) {
            val bookedStartMin = parseTimeToMinutes(booking.timeSlot)
            // Estimate duration of the booked services from our services list
            val bookedDuration = estimateDurationForServicesString(booking.services)
            val bookedEndMin = bookedStartMin + bookedDuration

            // Block any slot from allSlots that overlaps with this booking's time range
            for (slot in allSlots) {
                val slotStartMin = parseTimeToMinutes(slot)
                val slotEndMin = slotStartMin + newServiceDurationMin

                // A slot is blocked if:
                // - It exactly matches the booked slot, OR
                // - The new booking's time range [slotStart, slotEnd) overlaps with booked range [bookedStart, bookedEnd)
                val overlaps = slotStartMin < bookedEndMin && slotEndMin > bookedStartMin
                if (overlaps) {
                    blocked.add(slot)
                }
            }
        }
        return blocked
    }

    // Estimate total duration in minutes from a booking's services string
    // by matching against known services in the database
    private fun estimateDurationForServicesString(servicesStr: String): Int {
        val servicesList = allServices.value
        var totalMin = 0
        for (service in servicesList) {
            if (servicesStr.contains(service.name, ignoreCase = true)) {
                totalMin += service.durationMin
            }
        }
        return if (totalMin == 0) 30 else totalMin // fallback 30 min
    }

    // Calculate total duration (minutes) of currently selected services
    fun calculateTotalServiceDuration(): Int {
        val selected = _selectedServices.value
        val servicesList = allServices.value
        var total = 0
        for (sel in selected) {
            val match = servicesList.find { it.name == sel }
            total += match?.durationMin ?: 20 // fallback 20 min per service
        }
        return if (total == 0) 20 else total
    }

    fun getDynamicSlotsForStylistDate(
        stylistName: String,
        date: String,
        allSlots: List<String>
    ): List<String> {
        val bookings = allBookings.value
        val activeBookings = bookings.filter {
            it.stylistName == stylistName &&
            it.date == date &&
            it.status == "Active"
        }
        if (activeBookings.isEmpty()) return allSlots

        fun parseTimeToMinutes(timeStr: String): Int {
            return try {
                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
                val date2 = sdf.parse(timeStr.trim()) ?: return 0
                val cal = java.util.Calendar.getInstance().apply { time = date2 }
                cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
            } catch (e: Exception) { 0 }
        }

        fun formatMinutesToTime(totalMins: Int): String {
            val hrs = (totalMins / 60) % 24
            val mins = totalMins % 60
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hrs)
                set(java.util.Calendar.MINUTE, mins)
            }
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
            return sdf.format(cal.time)
        }

        return allSlots.mapIndexed { idx, slot ->
            val startMins = parseTimeToMinutes(slot)
            val nextSlotStartMins = if (idx < allSlots.size - 1) {
                parseTimeToMinutes(allSlots[idx + 1])
            } else {
                24 * 60
            }

            val bookingsInSlot = activeBookings.filter {
                val bookMin = parseTimeToMinutes(it.timeSlot)
                bookMin >= startMins && bookMin < nextSlotStartMins
            }

            if (bookingsInSlot.isNotEmpty()) {
                val maxEndMins = bookingsInSlot.maxOf { b ->
                    val bookStartMin = parseTimeToMinutes(b.timeSlot)
                    bookStartMin + estimateDurationForServicesString(b.services)
                }
                formatMinutesToTime(maxEndMins)
            } else {
                slot
            }
        }
    }
}
