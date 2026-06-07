package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Booking
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.data.Stylist
import com.example.data.SalonService
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.SalonViewModel

// Core theme values in scope
val GoldPrimary = Color(0xFFC5A059)
val GoldBackground = Color(0xFFFAFAF9)
val SoftObsidian = Color(0xFF1A1A1A)
val CharcoalGray = Color(0xFF2C2C2C)
val LightContainerGold = Color(0xFFF9F5EC)
val SoftWhite = Color(0xFFFFFFFF)
val SleekBorder = Color(0xFFE5E7EB)

class MainActivity : ComponentActivity() {
    private val viewModel: SalonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LuxeSalonApp(viewModel)
            }
        }
    }
}

@Composable
fun LuxeSalonApp(viewModel: SalonViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GoldBackground,
        topBar = {
            LuxeTopBar()
        },
        bottomBar = {
            LuxeBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    viewModel.selectTab(tab)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "MainFlowTransitions"
            ) { targetTab ->
                when (targetTab) {
                    0 -> LandingScreen(
                        viewModel = viewModel,
                        onNavigateToBooking = {
                            viewModel.resetBookingFlow()
                            viewModel.selectTab(1)
                        },
                        onBookStyle = { styleName ->
                            viewModel.resetBookingFlow()
                            viewModel.toggleService(styleName) // Pre-select the premium service directly!
                            viewModel.selectTab(1)
                            viewModel.setWizardStep(2) // Skip selecting services, move directly to stylist step!
                        }
                    )
                    1 -> BookingWizardScreen(viewModel)
                    2 -> BookingLookupScreen(viewModel)
                    3 -> AdminPortalScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun LuxeTopBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .drawBehind {
                // Thin delicate bottom line in modern gray border
                drawLine(
                    color = SleekBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        color = SoftWhite,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SoftObsidian, shape = RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Row {
                    Text(
                        text = "LUXE ",
                        color = SoftObsidian,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "SALON",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GoldPrimary.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.2f), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(SoftObsidian, GoldPrimary, SoftObsidian)
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun LuxeBottomNavigationBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = SoftWhite,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .drawBehind {
                // Top border line
                drawLine(
                    color = SleekBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            val tabs = listOf(
                SalonNavigationItem(0, "Home", Icons.Default.Home, "home_tab"),
                SalonNavigationItem(1, "Booking", Icons.Default.DateRange, "booking_tab"),
                SalonNavigationItem(2, "Lookup", Icons.Default.Search, "lookup_tab"),
                SalonNavigationItem(3, "Admin", Icons.Default.Person, "admin_tab")
            )

            tabs.forEach { item ->
                val selected = currentTab == item.index
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(item.index) },
                    modifier = Modifier.testTag(item.testTag),
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) GoldPrimary else SoftObsidian.copy(alpha = 0.4f)
                        )
                    },
                    label = {
                        Text(
                            text = item.label.uppercase(),
                            color = if (selected) GoldPrimary else SoftObsidian.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = GoldPrimary.copy(alpha = 0.08f)
                    )
                )
            }
        }
    }
}

data class SalonNavigationItem(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

// ==========================================
// SCREEN 0: PREMIUM LANDING PAGE (HOME)
// ==========================================
@Composable
fun LandingScreen(viewModel: SalonViewModel, onNavigateToBooking: () -> Unit, onBookStyle: (String) -> Unit) {
    val dynamicServices by viewModel.allServices.collectAsStateWithLifecycle(initialValue = emptyList())
    val dynamicStylists by viewModel.allStylists.collectAsStateWithLifecycle(initialValue = emptyList())
    val standardServices = remember(dynamicServices) { dynamicServices.filter { !it.isPremium } }
    val premiumServices = remember(dynamicServices) { dynamicServices.filter { it.isPremium } }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // High fidelity Hero Banner using custom drawing overlay to look gorgeous
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SoftObsidian, CharcoalGray)
                        )
                    )
            ) {
                // Background artistic lines representing luxury threads
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Golden strands representing premium salon styling art
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.08f),
                        radius = canvasHeight * 0.8f,
                        center = Offset(canvasWidth * 0.9f, canvasHeight * 0.2f)
                    )
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.04f),
                        radius = canvasHeight * 0.5f,
                        center = Offset(canvasWidth * 0.1f, canvasHeight * 0.8f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EXCLUSIVITY DEFINED",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "The Art of Refined Beauty",
                        color = SoftWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Experience a meticulous, high-end sanctuary tailored exclusively to your signature appearance.",
                        color = SoftWhite.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToBooking,
                        modifier = Modifier
                            .testTag("book_now_hero_btn")
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(
                            text = "BOOK NOW",
                            color = SoftWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }

        // Curated Services horizontally scrolled list (Dynamic from database)
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Manage Salon Services",
                            color = SoftObsidian,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Meticulously crafted treatments loaded from database",
                            color = CharcoalGray.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (standardServices.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(180.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No services loaded from database.", color = CharcoalGray.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(standardServices) { service ->
                            Card(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(180.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(LightContainerGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = service.name,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${service.durationMin} MIN",
                                                color = SoftWhite,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = service.name,
                                            color = SoftObsidian,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = service.description.ifBlank { "Premium treatment" },
                                            color = CharcoalGray.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₹${String.format("%.0f", service.price)}",
                                            color = GoldPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Button(
                                            onClick = {
                                                viewModel.resetBookingFlow()
                                                viewModel.toggleService(service.name)
                                                viewModel.selectTab(1)
                                                viewModel.setWizardStep(2) // Move directly to stylist step
                                            },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            modifier = Modifier.height(26.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                            shape = RoundedCornerShape(13.dp)
                                        ) {
                                            Text("BOOK", color = SoftWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Signature Cutting Styles visual showcase
        item {
            Column(modifier = Modifier.padding(top = 28.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Premium Hair Sculptures",
                            color = SoftObsidian,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "सिग्नेचर हेयरकट डिज़ाइन - Select & book details",
                            color = CharcoalGray.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (premiumServices.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(260.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                    Text("No premium hair sculptures configured.", color = CharcoalGray.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(premiumServices) { service ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(260.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        // Custom Vector drawing of scissors_cutting!
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .background(LightContainerGold, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_scissors_cutting),
                                                contentDescription = "Cutting Style Icon",
                                                modifier = Modifier.size(52.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = service.name,
                                            color = SoftObsidian,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (service.nameHindi.isNotBlank()) {
                                            Text(
                                                text = service.nameHindi,
                                                color = GoldPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = service.description,
                                            color = CharcoalGray.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (service.suitability.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = service.suitability,
                                                color = CharcoalGray.copy(alpha = 0.5f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₹${service.price.toInt()}",
                                            color = SoftObsidian,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )

                                        Button(
                                            onClick = { onBookStyle(service.name) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            modifier = Modifier
                                                .height(28.dp)
                                                .testTag("book_style_${service.name.lowercase().replace(" ", "_")}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(
                                                text = "BOOK",
                                                color = SoftWhite,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stylist Team visual highlights (Dynamic from database)
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Stylist Operational Roster",
                    color = SoftObsidian,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Masters of premium elegance loaded from database",
                    color = CharcoalGray.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (dynamicStylists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftWhite, RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No stylists loaded from database.", color = CharcoalGray.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                } else {
                    dynamicStylists.forEachIndexed { index, stylist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(SoftWhite, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decorative Avatar representing premium stylist profile picture
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = getGradientForIndex(stylist.avatarColorIndex)
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.5.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stylist.name.split(" ").map { it.take(1) }.joinToString(""),
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stylist.name,
                                        color = SoftObsidian,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!stylist.isAvailable) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .border(0.5.dp, Color(0xFFEF5350), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("AWAY", color = Color(0xFFC62828), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = stylist.specialty,
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Premium artisan specializing in ${stylist.specialty}.",
                                    color = CharcoalGray.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " 5.0",
                                    color = SoftObsidian,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Footer block as standard footer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftObsidian)
                    .padding(vertical = 24.dp, horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "LUXE SALON",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(text = "Instagram", color = SoftWhite.copy(0.7f), fontSize = 11.sp, modifier = Modifier.clickable {})
                        Text(text = "Facebook", color = SoftWhite.copy(0.7f), fontSize = 11.sp, modifier = Modifier.clickable {})
                        Text(text = "Contact", color = SoftWhite.copy(0.7f), fontSize = 11.sp, modifier = Modifier.clickable {})
                    }
                    HorizontalDivider(color = SoftWhite.copy(0.1f), thickness = 0.5.dp)
                    Text(
                        text = "© 2024 Luxe Salon Management. All Rights Reserved.",
                        color = SoftWhite.copy(0.4f),
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: CLIENT BOOKING WIZARD
// ==========================================
@Composable
fun BookingWizardScreen(viewModel: SalonViewModel) {
    val step by viewModel.wizardStep.collectAsStateWithLifecycle()
    val selectedServices by viewModel.selectedServices.collectAsStateWithLifecycle()
    val stylistSelection by viewModel.selectedStylist.collectAsStateWithLifecycle()
    val dateSelection by viewModel.selectedDate.collectAsStateWithLifecycle()
    val timeSelection by viewModel.selectedTime.collectAsStateWithLifecycle()
    val stylists by viewModel.allStylists.collectAsStateWithLifecycle(initialValue = emptyList())
    val dynamicServices by viewModel.allServices.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // Form Inputs
    val nameVal by viewModel.nameInput.collectAsStateWithLifecycle()
    val phoneVal by viewModel.phoneInput.collectAsStateWithLifecycle()
    val emailVal by viewModel.emailInput.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step Indicator Progress Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getStepLabelText(step),
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$step of 5",
                    color = SoftObsidian.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = (step.toFloat() / 5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = GoldPrimary,
                trackColor = GoldPrimary.copy(alpha = 0.15f)
            )
        }

        // Step Content dynamically selected
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (step) {
                1 -> StepServicesSelection(dynamicServices, selectedServices) { service ->
                    viewModel.toggleService(service)
                }
                2 -> StepStylistsSelection(viewModel, stylistSelection) { stylist ->
                    viewModel.setStylist(stylist)
                }
                3 -> {
                    val matchingStylist = stylists.find { it.name == stylistSelection }
                    StepTimeSlotSelection(
                        selectedDate = dateSelection,
                        selectedTime = timeSelection,
                        selectedStylist = matchingStylist,
                        onChooseDate = { d -> viewModel.setDate(d) },
                        onChooseTime = { t -> viewModel.setTimeSlot(t) }
                    )
                }
                4 -> StepPersonalInfoForm(nameVal, phoneVal, emailVal, viewModel)
                5 -> StepReceiptConfirmation(viewModel)
            }
        }

        // Stepper Navigation Buttons (Hide on confirmation step)
        if (step < 5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.setWizardStep(step - 1) },
                    enabled = step > 1,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                    border = BorderStroke(1.dp, if (step > 1) GoldPrimary else Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .testTag("wizard_back_btn")
                        .width(100.dp)
                        .height(44.dp)
                ) {
                    Text(text = "BACK", fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }

                Button(
                    onClick = {
                        if (step == 1 && selectedServices.isEmpty()) {
                            Toast.makeText(context, "Please select at least one service", Toast.LENGTH_SHORT).show()
                        } else if (step == 2 && stylistSelection == null) {
                            Toast.makeText(context, "Please select preferred stylist", Toast.LENGTH_SHORT).show()
                        } else if (step == 3 && (dateSelection == null || timeSelection == null)) {
                            Toast.makeText(context, "Please select slot date and time", Toast.LENGTH_SHORT).show()
                        } else if (step == 4) {
                            if (nameVal.isBlank() || phoneVal.isBlank()) {
                                Toast.makeText(context, "Name and Phone values are required", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.submitBooking()
                            }
                        } else {
                            viewModel.setWizardStep(step + 1)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .testTag("wizard_continue_btn")
                        .width(155.dp)
                        .height(44.dp)
                ) {
                    val label = if (step == 4) "BOOK NOW" else "CONTINUE"
                    Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: BOOKING LOOKUP
// ==========================================
@Composable
fun BookingLookupScreen(viewModel: SalonViewModel) {
    val searchPhone by viewModel.lookupSearchText.collectAsStateWithLifecycle()
    val searchResults by viewModel.lookupSearchResults.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Track Appointments",
            color = SoftObsidian,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Check appointment statuses or cancel bookings.",
            color = CharcoalGray.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SoftWhite),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchPhone,
                    onValueChange = { viewModel.lookupSearchText.value = it },
                    label = { Text("Secure Registered Phone Number") },
                    placeholder = { Text("e.g. +1234567890") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lookup_phone_search"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your registered number to view your real-time booking receipts. Admin users can build custom services and manage appointments from the admin login tab.",
                    color = CharcoalGray.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search placeholder",
                        tint = GoldPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No records queried yet",
                        color = CharcoalGray.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Type in your lookup phone or click 'Load Demo'",
                        color = CharcoalGray.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(searchResults) { booking ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_result_card"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftWhite),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val statusColor = if (booking.status == "Active") Color(0xFF4CAF50) else Color(0xFFE53935)
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = booking.status.uppercase(),
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = booking.services,
                                        color = SoftObsidian,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    text = "₹${String.format("%.2f", booking.priceEstimate)}",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Stylist",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = " Partner: ${booking.stylistName}",
                                    color = CharcoalGray.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }

                            Row(modifier = Modifier.padding(top = 2.dp)) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = " Schedule: ${booking.date} @ ${booking.timeSlot}",
                                    color = CharcoalGray.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }

                            if (booking.status == "Active") {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.cancelBooking(booking) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                    border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .testTag("cancel_booking_action_btn")
                                ) {
                                    Text(
                                        text = "CANCEL RESERVATION",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: ADMINISTRATOR ACCESS & SETTINGS
// ==========================================
@Composable
fun AdminPortalScreen(viewModel: SalonViewModel) {
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = isLoggedIn,
        transitionSpec = {
            slideInHorizontally(animationSpec = tween(300)) { it } togetherWith slideOutHorizontally(animationSpec = tween(300)) { -it }
        },
        label = "AdminPanelSlide"
    ) { logged ->
        if (logged) {
            AdminDashboardScreen(viewModel)
        } else {
            AdminAuthScreen(viewModel)
        }
    }
}

@Composable
fun AdminAuthScreen(viewModel: SalonViewModel) {
    val email by viewModel.authEmail.collectAsStateWithLifecycle()
    val password by viewModel.authPassword.collectAsStateWithLifecycle()
    val error by viewModel.authError.collectAsStateWithLifecycle()

    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AUTHENTICATION SANCTUARY",
            color = GoldPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = "Access Administration Portal",
            color = SoftObsidian,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SoftWhite),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.authEmail.value = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("admin_auth_email"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.authPassword.value = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("admin_auth_password"),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Info else Icons.Default.Lock,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.loginAdmin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftObsidian),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_auth_submit_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "LOGIN",
                        color = SoftWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: SalonViewModel) {
    val profile by viewModel.adminProfile.collectAsStateWithLifecycle()
    val stylists by viewModel.allStylists.collectAsStateWithLifecycle()
    val logs by viewModel.allBookings.collectAsStateWithLifecycle()
    val revenue by viewModel.revenueToday.collectAsStateWithLifecycle()
    val bookingCount by viewModel.activeBookingsCount.collectAsStateWithLifecycle()
    val activeSubTab by viewModel.activeAdminSubTab.collectAsStateWithLifecycle()

    var customName by remember { mutableStateOf("") }
    var customGreeting by remember { mutableStateOf("") }

    // Dialog state management
    var showAddStylistDialog by remember { mutableStateOf(false) }
    var showEditStylistDialog by remember { mutableStateOf<Stylist?>(null) }
    var showSetAwayDialog by remember { mutableStateOf<Stylist?>(null) }

    // Booking list filters
    var bookingPhoneSearch by remember { mutableStateOf("") }
    var bookingStatusFilter by remember { mutableStateOf("All") } // "All", "Active", "Cancelled"

    // Synchronize profile state once on first load
    LaunchedEffect(profile) {
        profile?.let {
            customName = it.displayName
            customGreeting = it.customGreeting
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Greeting Header Block
        Column {
            Text(
                text = customGreeting.ifBlank { "Welcome back, Executive" },
                color = SoftObsidian,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Luxe Salon Administrative Intelligence Console",
                color = CharcoalGray.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }

        // Sub-Navigation Tabs Switcher Bar
        val adminSubTabs = listOf(
            SubTabInfo(0, "Dashboard", Icons.Default.Star),
            SubTabInfo(1, "Stylists", Icons.Default.Face),
            SubTabInfo(2, "Registry", Icons.Default.List),
            SubTabInfo(3, "Settings", Icons.Default.Settings)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftWhite, RoundedCornerShape(10.dp))
                .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            adminSubTabs.forEach { tab ->
                val selected = activeSubTab == tab.index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selected) GoldPrimary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.activeAdminSubTab.value = tab.index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) SoftWhite else SoftObsidian.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label.uppercase(),
                            color = if (selected) SoftWhite else SoftObsidian.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Core Workspace Area based on the selected internal subtab
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                0 -> {
                    // Dashboard Analytics tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quick Stats row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftObsidian)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "REVENUE TODAY",
                                            color = GoldPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Column {
                                            Text(
                                                text = "₹${String.format("%.0f", revenue)}",
                                                color = SoftWhite,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "+12% from yesterday",
                                                color = GoldPrimary.copy(alpha = 0.8f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(110.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                    border = BorderStroke(1.dp, SleekBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "ACTIVE BOOKINGS",
                                            color = SoftObsidian.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Column {
                                            Text(
                                                text = "$bookingCount",
                                                color = SoftObsidian,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "System active and syncing",
                                                color = Color(0xFF059669),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Populate Demo Seed data Helper Trigger (Useful if empty)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Database Maintenance Reset",
                                            color = Color(0xFFC62828),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Wipe all bookings, stylists, and custom premium services.",
                                            color = CharcoalGray.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.loadDemoWork() // Clears all tables
                                            Toast.makeText(context, "Database Cleaned Successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(32.dp).testTag("wipe_seeds_btn")
                                    ) {
                                        Text("WIPE DATABASE", color = SoftWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Operational activity logs banner showing recent 5
                        item {
                            Column {
                                Text(
                                    text = "Recent Operational Logs",
                                    color = SoftObsidian,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (logs.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SoftWhite, RoundedCornerShape(8.dp))
                                            .border(1.dp, SleekBorder, RoundedCornerShape(8.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No booking records present. Click LOAD SEEDS or create standard customer bookings.",
                                            color = CharcoalGray.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                                    ) {
                                        Column {
                                            logs.take(5).forEachIndexed { i, log ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .background(LightContainerGold, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = if (log.phoneNumber.length > 5) log.phoneNumber.takeLast(2) else "CL",
                                                                color = GoldPrimary,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.ExtraBold
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(text = log.services, color = SoftObsidian, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                            Text(text = "${log.clientName} • ${log.stylistName} • ${log.date}", color = CharcoalGray.copy(0.7f), fontSize = 10.sp)
                                                        }
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(text = "₹${log.priceEstimate}", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        val c = if (log.status == "Active") Color(0xFF4CAF50) else Color(0xFFE53935)
                                                        Text(text = log.status.uppercase(), color = c, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                                    }
                                                }
                                                if (i < logs.take(5).size - 1) {
                                                    HorizontalDivider(color = GoldPrimary.copy(alpha = 0.08f), thickness = 0.5.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Stylist Team Management tab complete CRUD
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Stylist Operational Roster",
                                    color = SoftObsidian,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Button(
                                    onClick = { showAddStylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftObsidian),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp).testTag("add_stylist_trigger")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RECRUIT ARTISAN", color = SoftWhite, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                }
                            }
                        }

                        if (stylists.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SoftWhite, RoundedCornerShape(8.dp))
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No stylist records loaded. Tap the RECRUIT ARTISAN trigger.", color = CharcoalGray.copy(0.5f), fontSize = 11.sp)
                                }
                            }
                        } else {
                            items(stylists.size) { index ->
                                val stylist = stylists[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("stylist_card_${stylist.id}"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (!stylist.imageUrl.isNullOrBlank()) Color.LightGray else Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!stylist.imageUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = stylist.imageUrl,
                                                        contentDescription = stylist.name,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                brush = Brush.radialGradient(
                                                                    colors = getGradientForIndex(stylist.avatarColorIndex)
                                                                )
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = stylist.name.split(" ").map { it.take(1) }.joinToString(""),
                                                            color = SoftWhite,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = stylist.name, color = SoftObsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (!stylist.isAvailable && stylist.awayUntilDate != null && stylist.awayUntilTime != null) {
                                                    Text(
                                                        text = "Leave until ${stylist.awayUntilDate} @ ${stylist.awayUntilTime}",
                                                        color = Color(0xFFD32F2F),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(text = stylist.specialty, color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Availability Switch
                                            Switch(
                                                checked = stylist.isAvailable,
                                                onCheckedChange = { checked ->
                                                    if (!checked) {
                                                        showSetAwayDialog = stylist
                                                    } else {
                                                        viewModel.updateStylistAvailabilityWithTime(stylist.id, true, null, null)
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = SoftWhite, checkedTrackColor = GoldPrimary),
                                                modifier = Modifier.testTag("stylist_status_switch_${stylist.id}")
                                            )

                                            // Action Buttons Edit & Delete
                                            IconButton(
                                                onClick = { showEditStylistDialog = stylist },
                                                modifier = Modifier.size(32.dp).testTag("edit_stylist_btn_${stylist.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit parameters",
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteStylist(stylist)
                                                    Toast.makeText(context, "${stylist.name} Roster Removed", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp).testTag("delete_stylist_bin_${stylist.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Purge stylist",
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Appointment Master tab: complete list, search & status edits & deletion CRUD
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Filters row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = bookingPhoneSearch,
                                onValueChange = { bookingPhoneSearch = it },
                                label = { Text("Search parameters") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("admin_bookings_search_bar"),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = GoldPrimary,
                                    unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                                    focusedContainerColor = SoftWhite,
                                    unfocusedContainerColor = SoftWhite
                                )
                            )

                            // Status filters dropdown buttons
                            listOf("All", "Active", "Cancelled").forEach { status ->
                                val active = bookingStatusFilter == status
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (active) GoldPrimary else SoftWhite,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(1.dp, if (active) GoldPrimary else GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .clickable { bookingStatusFilter = status }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status.uppercase(),
                                        color = if (active) SoftWhite else SoftObsidian,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }

                        // Filtered bookings
                        val filteredBookings = logs.filter { b ->
                            val matchSearch = b.phoneNumber.contains(bookingPhoneSearch, ignoreCase = true) ||
                                              b.clientName.contains(bookingPhoneSearch, ignoreCase = true) ||
                                              b.services.contains(bookingPhoneSearch, ignoreCase = true)
                            val matchStatus = bookingStatusFilter == "All" || b.status.equals(bookingStatusFilter, ignoreCase = true)
                            matchSearch && matchStatus
                        }

                        if (filteredBookings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftWhite, RoundedCornerShape(8.dp))
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No appointments matching parameters found.", color = CharcoalGray.copy(0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredBookings.size) { i ->
                                    val booking = filteredBookings[i]
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_booking_card_${booking.id}"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.12f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(text = booking.clientName, color = SoftObsidian, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(text = "Mob: ${booking.phoneNumber}", color = CharcoalGray.copy(0.7f), fontSize = 10.sp)
                                                }

                                                val color = if (booking.status == "Active") Color(0xFF4CAF50) else Color(0xFFE53935)
                                                Text(
                                                    text = booking.status.uppercase(),
                                                    color = color,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = "SERVICES: ${booking.services}",
                                                color = SoftObsidian,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Artisan: ${booking.stylistName}", fontSize = 10.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                                Text(text = "Slot: ${booking.date} @ ${booking.timeSlot}", fontSize = 10.sp, color = CharcoalGray.copy(alpha = 0.6f))
                                            }

                                            HorizontalDivider(color = GoldPrimary.copy(alpha = 0.08f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Bill: ₹${booking.priceEstimate}",
                                                    color = SoftObsidian,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    // Action: Toggle Status Cancelled vs Active
                                                    Button(
                                                        onClick = {
                                                            val nextStatus = if (booking.status == "Active") "Cancelled" else "Active"
                                                            viewModel.updateBookingStatus(booking.id, nextStatus)
                                                            Toast.makeText(context, "Status Saved as $nextStatus", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (booking.status == "Active") Color(0xFFE53935).copy(alpha = 0.1f) else Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                        ),
                                                        shape = RoundedCornerShape(4.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                        modifier = Modifier.height(28.dp).testTag("cancel_booking_btn_${booking.id}")
                                                    ) {
                                                        Text(
                                                            text = if (booking.status == "Active") "CANCEL" else "RESTORE",
                                                            color = if (booking.status == "Active") Color(0xFFE53935) else Color(0xFF4CAF50),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }

                                                    // Action: Delete record permanently
                                                    Button(
                                                        onClick = {
                                                            viewModel.deleteBooking(booking)
                                                            Toast.makeText(context, "Appointment Purged From Registry", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                                        shape = RoundedCornerShape(4.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                        modifier = Modifier.height(28.dp).testTag("delete_booking_btn_${booking.id}")
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, tint = SoftWhite, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("DELETE", color = SoftWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Settings and Authentication Profile Config Area
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_settings_section"),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Customize Portal Identity",
                                        color = SoftObsidian,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = customName,
                                        onValueChange = { customName = it },
                                        label = { Text("Display Name") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_custom_name_input"),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = GoldPrimary,
                                            unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                                            focusedContainerColor = GoldBackground,
                                            unfocusedContainerColor = GoldBackground
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = customGreeting,
                                        onValueChange = { customGreeting = it },
                                        label = { Text("Custom Greeting") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_custom_greeting_input"),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = GoldPrimary,
                                            unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                                            focusedContainerColor = GoldBackground,
                                            unfocusedContainerColor = GoldBackground
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (customName.isNotBlank() && customGreeting.isNotBlank()) {
                                                viewModel.saveAdminIdentity(customName, customGreeting)
                                                Toast.makeText(context, "Identity Saved Securely", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .testTag("admin_save_settings_btn")
                                    ) {
                                        Text(text = "SAVE IDENTITY CHANGES", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        item {
                            val services by viewModel.allServices.collectAsStateWithLifecycle(initialValue = emptyList())
                            var showAddServiceDialog by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_services_settings_section"),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Manage Salon Services",
                                            color = SoftObsidian,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Button(
                                            onClick = { showAddServiceDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(28.dp).testTag("btn_add_service_dialog")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add New Service",
                                                tint = SoftWhite,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ADD NEW", color = SoftWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (services.isEmpty()) {
                                        Text(
                                            text = "No services configured. Add some above.",
                                            color = CharcoalGray.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            services.forEach { s ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(LightContainerGold, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = s.name,
                                                            color = SoftObsidian,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                        Text(
                                                            text = "₹${String.format("%.0f", s.price)} • ${s.durationMin} mins • ${s.description}",
                                                            color = CharcoalGray.copy(alpha = 0.8f),
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteService(s)
                                                            Toast.makeText(context, "Service ${s.name} deleted", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(24.dp).testTag("delete_service_btn_${s.name}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete service",
                                                            tint = Color(0xFFC62828),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showAddServiceDialog) {
                                AddServiceConfigDialog(
                                    onDismiss = { showAddServiceDialog = false },
                                    onConfirm = { name, price, desc, duration, nameHindi, suitability, isPremium ->
                                        viewModel.createService(name, price, desc, duration, nameHindi, suitability, isPremium)
                                        showAddServiceDialog = false
                                        Toast.makeText(context, "Service $name ($duration mins) added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }



                        item {
                            Button(
                                onClick = { viewModel.logoutAdmin() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("admin_logout_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "SIGN OUT OPERATIONAL SESSION", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog overlays
    if (showAddStylistDialog) {
        AddStylistDialog(
            onDismiss = { showAddStylistDialog = false },
            onConfirm = { n, s, index, imgUrl, isAvail, date, time ->
                viewModel.createStylist(n, s, isAvail, index, imgUrl, date, time)
                showAddStylistDialog = false
                Toast.makeText(context, "Artisan $n Recruited successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    showEditStylistDialog?.let { currentStylist ->
        EditStylistDialog(
            stylist = currentStylist,
            onDismiss = { showEditStylistDialog = null },
            onConfirm = { n, s, index, imgUrl, isAvail, date, time ->
                viewModel.updateStylist(
                    currentStylist.copy(
                        name = n,
                        specialty = s,
                        avatarColorIndex = index,
                        imageUrl = imgUrl,
                        isAvailable = isAvail,
                        awayUntilDate = date,
                        awayUntilTime = time
                    )
                )
                showEditStylistDialog = null
                Toast.makeText(context, "Changes Saved successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    showSetAwayDialog?.let { currentStylist ->
        SetAwayUntilDialog(
            stylist = currentStylist,
            onDismiss = { showSetAwayDialog = null },
            onConfirm = { date, time ->
                viewModel.updateStylistAvailabilityWithTime(currentStylist.id, false, date, time)
                showSetAwayDialog = null
                Toast.makeText(context, "${currentStylist.name} set away until $date @ $time", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SetAwayUntilDialog(
    stylist: Stylist,
    onDismiss: () -> Unit,
    onConfirm: (awayUntilDate: String, awayUntilTime: String) -> Unit
) {
    val dates = remember {
        val list = mutableListOf<String>()
        val sdf = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Start from tomorrow (meaning booking can only be done at least one day in advance)
        for (i in 0 until 6) {
            list.add(sdf.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    val timesCode = listOf("09:00 AM", "10:30 AM", "12:00 PM", "02:30 PM", "04:00 PM", "05:30 PM")

    var selectedDate by remember { mutableStateOf(dates[0]) }
    var selectedTime by remember { mutableStateOf(timesCode[3]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Schedule Leave for ${stylist.name}",
                color = SoftObsidian,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Specify when the artisan is expected to return to operational service:",
                    fontSize = 12.sp,
                    color = CharcoalGray.copy(alpha = 0.7f)
                )

                Text(
                    text = "AWAY UNTIL DATE",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dates.forEach { date ->
                        val isSel = selectedDate == date
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSel) GoldPrimary else LightContainerGold,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDate = date }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = date,
                                color = if (isSel) SoftWhite else SoftObsidian,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = "AWAY UNTIL TIME SLOT",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timesCode.forEach { time ->
                        val isSel = selectedTime == time
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSel) GoldPrimary else LightContainerGold,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTime = time }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = time,
                                color = if (isSel) SoftWhite else SoftObsidian,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, selectedTime) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("APPLY LEAVE ROSTER", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = SoftObsidian, fontSize = 11.sp)
            }
        },
        containerColor = SoftWhite,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AddServiceConfigDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, description: String, durationMin: Int, nameHindi: String, suitability: String, isPremium: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("30") }
    var nameHindi by remember { mutableStateOf("") }
    var suitability by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Salon Service",
                color = SoftObsidian,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_service_name"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = nameHindi,
                    onValueChange = { nameHindi = it },
                    label = { Text("Hindi Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_service_name_hindi"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price (INR / ₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_service_price"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = { Text("Duration (e.g. 10, 20 mins)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_service_duration"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = suitability,
                    onValueChange = { suitability = it },
                    label = { Text("Suitability (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_service_suitability"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Service Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_service_desc"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPremium = !isPremium }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isPremium,
                        onCheckedChange = { isPremium = it },
                        colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mark as Premium Hair Sculpture",
                        color = SoftObsidian,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 100.0
                    val duration = durationStr.toIntOrNull() ?: 30
                    if (name.isNotBlank() && description.isNotBlank()) {
                        onConfirm(name.trim(), price, description.trim(), duration, nameHindi.trim(), suitability.trim(), isPremium)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("dialog_add_service_submit")
            ) {
                Text("ADD SERVICE", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_add_service_cancel")
            ) {
                Text("CANCEL", color = SoftObsidian, fontSize = 11.sp)
            }
        },
        containerColor = SoftWhite,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun AddStylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, specialty: String, avatarColorVal: Int, imageUrl: String?, isAvailable: Boolean, awayUntilDate: String?, awayUntilTime: String?) -> Unit
) {
    val dates = remember {
        val list = mutableListOf<String>()
        val sdf = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Start from tomorrow
        for (i in 0 until 6) {
            list.add(sdf.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var imageUrl by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var awayUntilDate by remember { mutableStateOf(dates.firstOrNull() ?: "") }
    var awayUntilTime by remember { mutableStateOf("02:30 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recruit New Hair Artisan",
                color = SoftObsidian,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Artisan Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_stylist_name"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Specialty Designation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_stylist_specialty"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                Text(
                    text = "Artisan Portrait Image (Optional URL):",
                    fontSize = 11.sp,
                    color = CharcoalGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Custom Portrait Image URL") },
                    singleLine = true,
                    trailingIcon = {
                        if (imageUrl.isNotBlank()) {
                            IconButton(onClick = { imageUrl = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear URL", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_add_stylist_image_url"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                // Presets choice
                Text(
                    text = "Or Select Luxury Preset Portraits:",
                    fontSize = 10.sp,
                    color = CharcoalGray.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )

                val portraitPresets = listOf(
                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80",
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                    "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=150&q=80",
                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    portraitPresets.forEachIndexed { idx, url ->
                        val isSel = imageUrl == url
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .border(
                                    width = if (isSel) 3.dp else 1.dp,
                                    color = if (isSel) GoldPrimary else SleekBorder,
                                    shape = CircleShape
                                )
                                .clickable { imageUrl = url }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Preset Portrait ${idx + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Live preview if not empty
                if (imageUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoldPrimary.copy(0.04f), RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary.copy(0.12f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Live Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = "Artisan portrait image loaded.",
                            fontSize = 10.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Roster operational settings
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roster Availability",
                            color = SoftObsidian,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Is this stylist on active duty today?",
                            fontSize = 11.sp,
                            color = CharcoalGray.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SoftWhite, checkedTrackColor = GoldPrimary)
                    )
                }

                if (!isAvailable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specify Scheduled Return:",
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "RETURN DATE",
                        fontSize = 9.sp,
                        color = CharcoalGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dates.forEach { d ->
                            val isSel = awayUntilDate == d
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSel) GoldPrimary else LightContainerGold,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { awayUntilDate = d }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = d,
                                    color = if (isSel) SoftWhite else SoftObsidian,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RETURN TIME SLOT",
                        fontSize = 9.sp,
                        color = CharcoalGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("09:00 AM", "10:30 AM", "12:00 PM", "02:30 PM", "04:00 PM", "05:30 PM").forEach { t ->
                            val isSel = awayUntilTime == t
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSel) GoldPrimary else LightContainerGold,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { awayUntilTime = t }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = t,
                                    color = if (isSel) SoftWhite else SoftObsidian,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Fallback Accent Custom Theme:",
                    fontSize = 11.sp,
                    color = CharcoalGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..3).forEach { index ->
                        val isSelected = selectedColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.radialGradient(colors = getGradientForIndex(index)),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) GoldPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                                .testTag("color_choice_$index")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && specialty.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            specialty.trim(),
                            selectedColorIndex,
                            imageUrl.trim().ifEmpty { null },
                            isAvailable,
                            if (!isAvailable) awayUntilDate else null,
                            if (!isAvailable) awayUntilTime else null
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("dialog_add_stylist_submit")
            ) {
                Text("ADD TO SYSTEM", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_add_stylist_cancel")) {
                Text("CANCEL", color = SoftObsidian, fontSize = 11.sp)
            }
        },
        containerColor = SoftWhite,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun EditStylistDialog(
    stylist: Stylist,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newSpecialty: String, newColorIndex: Int, newImageUrl: String?, isAvailable: Boolean, awayUntilDate: String?, awayUntilTime: String?) -> Unit
) {
    val dates = remember {
        val list = mutableListOf<String>()
        val sdf = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Start from tomorrow
        for (i in 0 until 6) {
            list.add(sdf.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    var name by remember { mutableStateOf(stylist.name) }
    var specialty by remember { mutableStateOf(stylist.specialty) }
    var selectedColorIndex by remember { mutableStateOf(stylist.avatarColorIndex) }
    var imageUrl by remember { mutableStateOf(stylist.imageUrl ?: "") }
    var isAvailable by remember { mutableStateOf(stylist.isAvailable) }
    var awayUntilDate by remember { mutableStateOf(stylist.awayUntilDate ?: (dates.firstOrNull() ?: "")) }
    var awayUntilTime by remember { mutableStateOf(stylist.awayUntilTime ?: "02:30 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modify Artisan Parameters",
                color = SoftObsidian,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_edit_stylist_name"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Expert Specialty") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_edit_stylist_specialty"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                Text(
                    text = "Artisan Portrait Image (Optional URL):",
                    fontSize = 11.sp,
                    color = CharcoalGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Custom Portrait Image URL") },
                    singleLine = true,
                    trailingIcon = {
                        if (imageUrl.isNotBlank()) {
                            IconButton(onClick = { imageUrl = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear URL", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_edit_stylist_image_url"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.2f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                // Presets choice
                Text(
                    text = "Or Select Luxury Preset Portraits:",
                    fontSize = 10.sp,
                    color = CharcoalGray.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )

                val portraitPresets = listOf(
                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80",
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                    "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=150&q=80",
                    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    portraitPresets.forEachIndexed { idx, url ->
                        val isSel = imageUrl == url
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .border(
                                    width = if (isSel) 3.dp else 1.dp,
                                    color = if (isSel) GoldPrimary else SleekBorder,
                                    shape = CircleShape
                                )
                                .clickable { imageUrl = url }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Preset Portrait ${idx + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Live preview if not empty
                if (imageUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoldPrimary.copy(0.04f), RoundedCornerShape(8.dp))
                            .border(1.dp, GoldPrimary.copy(0.12f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Live Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(
                            text = "Artisan portrait image loaded.",
                            fontSize = 10.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Roster operational settings
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roster Availability",
                            color = SoftObsidian,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Is this stylist on active duty today?",
                            fontSize = 11.sp,
                            color = CharcoalGray.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SoftWhite, checkedTrackColor = GoldPrimary)
                    )
                }

                if (!isAvailable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specify Scheduled Return:",
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "RETURN DATE",
                        fontSize = 9.sp,
                        color = CharcoalGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dates.forEach { d ->
                            val isSel = awayUntilDate == d
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSel) GoldPrimary else LightContainerGold,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { awayUntilDate = d }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = d,
                                    color = if (isSel) SoftWhite else SoftObsidian,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RETURN TIME SLOT",
                        fontSize = 9.sp,
                        color = CharcoalGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("09:00 AM", "10:30 AM", "12:00 PM", "02:30 PM", "04:00 PM", "05:30 PM").forEach { t ->
                            val isSel = awayUntilTime == t
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isSel) GoldPrimary else LightContainerGold,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { awayUntilTime = t }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = t,
                                    color = if (isSel) SoftWhite else SoftObsidian,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Accent Theme Color:",
                    fontSize = 11.sp,
                    color = CharcoalGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..3).forEach { index ->
                        val isSelected = selectedColorIndex == index
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.radialGradient(colors = getGradientForIndex(index)),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) GoldPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorIndex = index }
                                .testTag("color_edit_choice_$index")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && specialty.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            specialty.trim(),
                            selectedColorIndex,
                            imageUrl.trim().ifEmpty { null },
                            isAvailable,
                            if (!isAvailable) awayUntilDate else null,
                            if (!isAvailable) awayUntilTime else null
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("dialog_edit_stylist_submit")
            ) {
                Text("SAVE APPLIED CHANGES", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_edit_stylist_cancel")) {
                Text("CANCEL", color = SoftObsidian, fontSize = 11.sp)
            }
        },
        containerColor = SoftWhite,
        shape = RoundedCornerShape(12.dp)
    )
}

data class SubTabInfo(
    val index: Int,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun StepPersonalInfoForm(
    name: String,
    phone: String,
    email: String,
    viewModel: SalonViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Client Details",
            color = SoftObsidian,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Please enter your contact parameters so we can reserve your custom experience.",
            color = CharcoalGray.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SoftWhite),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.nameInput.value = it },
                    label = { Text("Display / Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("wizard_name_input"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.phoneInput.value = it },
                    label = { Text("Contact Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("wizard_phone_input"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.emailInput.value = it },
                    label = { Text("Email Address (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("wizard_email_input"),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = GoldPrimary,
                        unfocusedIndicatorColor = GoldPrimary.copy(alpha = 0.15f),
                        focusedContainerColor = GoldBackground,
                        unfocusedContainerColor = GoldBackground
                    )
                )
            }
        }
    }
}

@Composable
fun StepReceiptConfirmation(viewModel: SalonViewModel) {
    val finalBooking by viewModel.latestBookingReceipt.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = GoldPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "RESERVATION CONFIRMED",
            color = GoldPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Your Aura Ritual is Secured",
            color = SoftObsidian,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        finalBooking?.let { booking ->
            // Visual premium Receipt ticket style
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SoftWhite),
                border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "LUXE SALON RITUAL TICKET",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "CLIENT", fontSize = 11.sp, color = CharcoalGray.copy(0.6f))
                        Text(text = booking.clientName.uppercase(), fontSize = 12.sp, color = SoftObsidian, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "PHONE REGISTERED", fontSize = 11.sp, color = CharcoalGray.copy(0.6f))
                        Text(text = booking.phoneNumber, fontSize = 11.sp, color = SoftObsidian)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ARTISAN / STYLIST", fontSize = 11.sp, color = CharcoalGray.copy(0.6f))
                        Text(text = booking.stylistName, fontSize = 12.sp, color = SoftObsidian, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "SCHEDULE HOUR", fontSize = 11.sp, color = CharcoalGray.copy(0.6f))
                        Text(text = "${booking.date} @ ${booking.timeSlot}", fontSize = 12.sp, color = SoftObsidian, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = GoldPrimary.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "CURATED SERVICES",
                        fontSize = 10.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = booking.services,
                        fontSize = 12.sp,
                        color = SoftObsidian,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ESTIMATED TOTAL DUE", fontSize = 11.sp, color = SoftObsidian, fontWeight = FontWeight.Bold)
                        Text(
                            text = "₹${String.format("%.2f", booking.priceEstimate)}",
                            fontSize = 18.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightContainerGold, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "To tracking / cancel: search phone ${booking.phoneNumber} in Lookup Tab.",
                            fontSize = 9.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Formulating receipt...", color = CharcoalGray.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.resetBookingFlow()
                viewModel.selectTab(0)
            },
            colors = ButtonDefaults.buttonColors(containerColor = SoftObsidian),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("wizard_done_back_to_home_btn")
        ) {
            Text(text = "RETURN TO HOME GALLERY", color = SoftWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

fun getStepLabelText(step: Int): String {
    return when (step) {
        1 -> "STEP 1: CURATE SERVICES"
        2 -> "STEP 2: CHOOSE THE STYLIST"
        3 -> "STEP 3: CHOOSE THE MOMENT"
        4 -> "STEP 4: CLIENT ACCOUNT INFO"
        else -> "STEP 5: RESERVED RECEIPT"
    }
}

data class ServiceCardInfo(
    val title: String,
    val desc: String,
    val price: String,
    val icon: ImageVector
)

data class StylistHighlight(
    val name: String,
    val role: String,
    val bio: String,
    val avatarIndex: Int
)

fun getGradientForIndex(index: Int): List<Color> {
    return when (index % 4) {
        0 -> listOf(Color(0xFFE5C17D), Color(0xFFC5A059))
        1 -> listOf(Color(0xFF1F1C18), Color(0xFF5D5E61))
        2 -> listOf(Color(0xFFDEC087), Color(0xFF775a19))
        else -> listOf(Color(0xFF8A7144), Color(0xFF2C2C2C))
    }
}

@Composable
fun StepServicesSelection(
    services: List<SalonService>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Select Premium Services",
            color = SoftObsidian,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Fine art visual transformation packages tailored. Select one or more.",
            color = CharcoalGray.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        services.forEach { serviceInfo ->
            val isChecked = selected.contains(serviceInfo.name)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(serviceInfo.name) }
                    .testTag("service_card_${serviceInfo.name}"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isChecked) GoldPrimary else GoldPrimary.copy(alpha = 0.1f)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) LightContainerGold else SoftWhite
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggle(serviceInfo.name) },
                        colors = CheckboxDefaults.colors(checkedColor = GoldPrimary),
                        modifier = Modifier.testTag("service_checkbox_${serviceInfo.name}")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = serviceInfo.name,
                            color = SoftObsidian,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${String.format("%.0f", serviceInfo.price)}  •  ${serviceInfo.durationMin} mins",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = serviceInfo.description,
                            color = CharcoalGray.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepStylistsSelection(
    viewModel: SalonViewModel,
    selected: String?,
    onSelect: (String) -> Unit
) {
    val stylists by viewModel.allStylists.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Our Artistic Elite",
            color = SoftObsidian,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Partner with curated artisans representing hair, luxury, and styling perfection.",
            color = CharcoalGray.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        stylists.forEachIndexed { index, stylist ->
            val isSelected = selected == stylist.name
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(stylist.name) },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.1f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) LightContainerGold else SoftWhite
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (!stylist.imageUrl.isNullOrBlank()) Color.LightGray else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!stylist.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = stylist.imageUrl,
                                contentDescription = stylist.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = getGradientForIndex(index)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stylist.name.split(" ").map { it.take(1) }.joinToString(""),
                                    color = SoftWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stylist.name,
                            color = SoftObsidian,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stylist.specialty,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (stylist.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (stylist.isAvailable) {
                                    "Available Today"
                                } else if (stylist.awayUntilDate != null && stylist.awayUntilTime != null) {
                                    "Away (Return: ${stylist.awayUntilDate} @ ${stylist.awayUntilTime})"
                                } else {
                                    "Away (Book for Alternate Dates)"
                                },
                                color = CharcoalGray.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(stylist.name) },
                        colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun StepTimeSlotSelection(
    selectedDate: String?,
    selectedTime: String?,
    selectedStylist: Stylist? = null,
    onChooseDate: (String) -> Unit,
    onChooseTime: (String) -> Unit
) {
    val dates = remember {
        val list = mutableListOf<String>()
        val sdf = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.ENGLISH)
        val calendar = java.util.Calendar.getInstance()
        // Start from tomorrow (meaning booking can only be done at least one day in advance, today's date is not visible)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        for (i in 0 until 6) {
            list.add(sdf.format(calendar.time))
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    val timesCode = listOf("09:00 AM", "10:30 AM", "12:00 PM", "02:30 PM", "04:00 PM", "05:30 PM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Schedule Appointment",
            color = SoftObsidian,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        
        if (selectedStylist != null && !selectedStylist.isAvailable && selectedStylist.awayUntilDate != null && selectedStylist.awayUntilTime != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFFBC02D), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "Roster Leave Notice:",
                        color = Color(0xFFF57F17),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${selectedStylist.name} is currently away until ${selectedStylist.awayUntilDate} at ${selectedStylist.awayUntilTime}. Relaunch dates and slots are tailored below.",
                        color = Color(0xFF5D4037),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Text(
                text = "Select date and preferred slot for deluxe treatment.",
                color = CharcoalGray.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(text = "AVAILABLE DATES", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        val context = androidx.compose.ui.platform.LocalContext.current

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dates) { date ->
                val isDateSel = selectedDate == date
                
                // Check if date is blocked
                var isDateBlocked = false
                if (selectedStylist != null && !selectedStylist.isAvailable && selectedStylist.awayUntilDate != null) {
                    val returnDateIndex = dates.indexOf(selectedStylist.awayUntilDate)
                    val dateIndex = dates.indexOf(date)
                    if (returnDateIndex != -1 && dateIndex != -1 && dateIndex < returnDateIndex) {
                        isDateBlocked = true
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                isDateBlocked -> LightContainerGold.copy(alpha = 0.3f)
                                isDateSel -> GoldPrimary
                                else -> LightContainerGold
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (isDateBlocked) {
                                android.widget.Toast.makeText(
                                    context,
                                    "${selectedStylist?.name} is on leave until ${selectedStylist?.awayUntilDate}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onChooseDate(date)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = date,
                        color = when {
                            isDateBlocked -> SoftObsidian.copy(alpha = 0.3f)
                            isDateSel -> SoftWhite
                            else -> SoftObsidian
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = if (isDateBlocked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "AVAILABLE HOURS", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

        timesCode.forEach { slot ->
            val isTimeSel = selectedTime == slot
            
            // Check if slot is blocked
            var isSlotBlocked = false
            if (selectedStylist != null && !selectedStylist.isAvailable && selectedStylist.awayUntilDate != null && selectedStylist.awayUntilTime != null && selectedDate != null) {
                val returnDateIndex = dates.indexOf(selectedStylist.awayUntilDate)
                val curDateIndex = dates.indexOf(selectedDate)
                if (curDateIndex != -1 && returnDateIndex != -1) {
                    if (curDateIndex < returnDateIndex) {
                        isSlotBlocked = true
                    } else if (curDateIndex == returnDateIndex) {
                        val returnTimeIndex = timesCode.indexOf(selectedStylist.awayUntilTime)
                        val slotIndex = timesCode.indexOf(slot)
                        if (returnTimeIndex != -1 && slotIndex != -1 && slotIndex < returnTimeIndex) {
                            isSlotBlocked = true
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSlotBlocked) {
                            android.widget.Toast.makeText(
                                context,
                                "Artisan is unavailable on leave during this slot",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onChooseTime(slot)
                        }
                    },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp, 
                    when {
                        isSlotBlocked -> GoldPrimary.copy(alpha = 0.05f)
                        isTimeSel -> GoldPrimary
                        else -> GoldPrimary.copy(alpha = 0.1f)
                    }
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSlotBlocked -> SoftWhite.copy(alpha = 0.4f)
                        isTimeSel -> LightContainerGold
                        else -> SoftWhite
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = slot,
                            color = if (isSlotBlocked) SoftObsidian.copy(alpha = 0.4f) else SoftObsidian,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (isSlotBlocked) {
                            Text(
                                text = "Stylist on Leave",
                                color = Color(0xFFD32F2F),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    RadioButton(
                        selected = isTimeSel,
                        enabled = !isSlotBlocked,
                        onClick = {
                            if (!isSlotBlocked) {
                                onChooseTime(slot)
                            }
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = GoldPrimary,
                            disabledUnselectedColor = Color.LightGray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

data class CuttingStyleInfo(
    val nameEnglish: String,
    val nameHindi: String,
    val desc: String,
    val suitability: String,
    val price: String
)
