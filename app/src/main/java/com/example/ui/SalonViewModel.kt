package com.example.ui

import android.app.Application
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
        addInterceptor(MockServerInterceptor())
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

                // Blow database tables local structures clean to merge with live Remote State
                repository.clearAllData()

                // Insert into local DB
                serviceDtos.forEach { dto ->
                     repository.insertService(dto.toRoomEntity())
                }
                stylistDtos.forEach { dto ->
                     repository.insertStylist(dto.toRoomEntity())
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

    val authEmail = MutableStateFlow("admin@luxesalon.com")
    val authPassword = MutableStateFlow("luxuryadmin123")
    val authName = MutableStateFlow("")
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val activeAdminSubTab = MutableStateFlow(0) // 0: Dashboard/Analytics, 1: Stylist Roster, 2: Bookings Master, 3: Portal Settings

    private val _latestBookingReceipt = MutableStateFlow<Booking?>(null)
    val latestBookingReceipt: StateFlow<Booking?> = _latestBookingReceipt.asStateFlow()

    init {
        // Clear all preexisting demo stylists, services, and bookings immediately on startup
        // to comply with your request for a pristine clean database.
        viewModelScope.launch {
            repository.clearAllData()

            repository.adminProfile.first().let { profile ->
                if (profile == null) {
                    repository.saveAdminProfile(
                        AdminProfile(
                            id = 1,
                            email = "raj.sharma@luxesalon.com",
                            displayName = "Raj Sharma",
                            customGreeting = "Welcome back, Executive Raj"
                        )
                    )
                }
            }
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

    // Submit Booking to Room database
    fun submitBooking() {
        var servicesList = _selectedServices.value.joinToString(", ")
        val cutSubtype = selectedCuttingStyleSubtype.value
        if (cutSubtype.isNotBlank() && _selectedServices.value.contains("Sculpted Cut")) {
            servicesList = servicesList.replace("Sculpted Cut", "Sculpted Cut ($cutSubtype)")
        }
        val stylist = _selectedStylist.value ?: "Aarav Sharma"
        val date = _selectedDate.value ?: "Oct 12"
        val time = _selectedTime.value ?: "02:30 PM"
        val phone = phoneInput.value.trim()
        val name = nameInput.value.trim()

        if (phone.isNotBlank() && name.isNotBlank()) {
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
                _latestBookingReceipt.value = newBooking
                _wizardStep.value = 5 // Go to receipt confirmation
            }
        }
    }

    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            repository.updateBookingStatus(booking.id, "Cancelled")
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

        // Extremely streamlined simulation: accepting any standard login for testing convenience
        viewModelScope.launch {
            _isAdminLoggedIn.value = true
            _authError.value = null
            
            // Check if profile exists, if not create
            val current = repository.adminProfile.first()
            if (current == null) {
                repository.saveAdminProfile(
                    AdminProfile(
                        id = 1,
                        email = email,
                        displayName = "Vance",
                        customGreeting = "Welcome back, Executive"
                    )
                )
            }
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

        viewModelScope.launch {
            repository.saveAdminProfile(
                AdminProfile(
                    id = 1,
                    email = email,
                    displayName = name,
                    customGreeting = "Welcome back, Executive $name"
                )
            )
            _isAdminLoggedIn.value = true
            _authError.value = null
        }
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
        }
    }

    fun updateBookingStatus(bookingId: Int, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
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

    fun createService(name: String, price: Double, description: String, durationMin: Int) {
        viewModelScope.launch {
            val serviceObj = SalonService(name = name, price = price, description = description, durationMin = durationMin)
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
}
