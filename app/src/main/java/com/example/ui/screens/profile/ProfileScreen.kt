package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToSettings: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val isDevMode by authViewModel.isDevMode.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.refreshDevModeState()
    }

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var selectedPlanToBuy by remember { mutableStateOf("") }
    var showPaymentScannerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Monthly, 1 = Annual (save 20%)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Subscription", fontWeight = FontWeight.Black, color = Color.White) },
                actions = {
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("profile_settings_button")) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("profile_screen_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // User Avatar Banner
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF140D2A))
                    .border(2.dp, Color(0xFFF9D142), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_surmaya_icon_1783113555260),
                    contentDescription = "User Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = currentUser?.displayName ?: "SurMaya Creator",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = currentUser?.email ?: "creator@surmaya.ai",
                color = Color(0xFF9E93B3),
                fontSize = 13.sp
            )

            // Dynamic Plan Status Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val activePlan = currentUser?.subscriptionPlan?.replaceFirstChar { it.uppercase() } ?: "Free"
                        Text(
                            text = "Active Plan: $activePlan",
                            color = Color(0xFFF9D142),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Credits remaining: ${currentUser?.creditsRemaining ?: 0}",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = { showUpgradeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Plans", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Developer Testing Control Panel (Access to Free, Pro, Premier plans) - Only shown in Developer Mode
            if (isDevMode) {
                var showDevPanel by remember { mutableStateOf(false) }
                
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .testTag("developer_testing_panel")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.DeveloperMode,
                                    contentDescription = "Developer Settings",
                                    tint = Color(0xFFF9D142),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Developer Testing Suite",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Full subscription bypass & credit controls",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Switch(
                                checked = showDevPanel,
                                onCheckedChange = { showDevPanel = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFF9D142),
                                    checkedTrackColor = Color(0xFFF9D142).copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color(0xFF9E93B3),
                                    uncheckedTrackColor = Color(0xFF140D2A)
                                ),
                                modifier = Modifier.testTag("dev_mode_switch")
                            )
                        }
                        
                        if (showDevPanel) {
                            HorizontalDivider(color = Color(0xFF2E244E), thickness = 1.dp)
                            
                            Text(
                                text = "Instantly switch subscription tier for testing app features:",
                                color = Color(0xFF9E93B3),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Free Tier Selector Button
                                Button(
                                    onClick = {
                                        authViewModel.upgradeSubscription("free")
                                        Toast.makeText(context, "Testing Plan: Activated Free tier (50 credits)", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentUser?.subscriptionPlan == "free") Color(0xFFF9D142) else Color(0xFF1E1738)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("dev_plan_free")
                                ) {
                                    Text(
                                        "Free Plan",
                                        color = if (currentUser?.subscriptionPlan == "free") Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Pro Tier Selector Button
                                Button(
                                    onClick = {
                                        authViewModel.upgradeSubscription("pro")
                                        Toast.makeText(context, "Testing Plan: Activated Pro tier (2500 credits)", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentUser?.subscriptionPlan == "pro") Color(0xFFF9D142) else Color(0xFF1E1738)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("dev_plan_pro")
                                ) {
                                    Text(
                                        "Pro Plan",
                                        color = if (currentUser?.subscriptionPlan == "pro") Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Premier Tier Selector Button
                                Button(
                                    onClick = {
                                        authViewModel.upgradeSubscription("premier")
                                        Toast.makeText(context, "Testing Plan: Activated Premier tier (10000 credits)", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentUser?.subscriptionPlan == "premier") Color(0xFFF9D142) else Color(0xFF1E1738)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("dev_plan_premier")
                                ) {
                                    Text(
                                        "Premier Plan",
                                        color = if (currentUser?.subscriptionPlan == "premier") Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Infinite Credit Injector
                                OutlinedButton(
                                    onClick = {
                                        authViewModel.updateUserCredits(999999)
                                        Toast.makeText(context, "Credits boosted to 999,999! Go wild!", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1.2f).height(38.dp).testTag("dev_inject_credits")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = "Boost Credits",
                                            tint = Color(0xFF9F75FF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Inject Credits", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                // Absolute God Mode (Max Tier + Max Credits + Custom Name)
                                Button(
                                    onClick = {
                                        authViewModel.upgradeSubscription("premier")
                                        authViewModel.updateUserCredits(1000000)
                                        Toast.makeText(context, "Developer God Mode Activated: VIP Premier & 1 Million Credits!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp).testTag("dev_god_mode")
                                ) {
                                    Text("Full Access", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // Selection Options: Monthly & Annual (save 20%)
            Text(
                text = "Premium Plans Available",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            // Custom Segmented Control Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF160F30))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (selectedTab == 0) Color(0xFF9F75FF) else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 12.dp)
                        .testTag("monthly_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Monthly",
                        color = if (selectedTab == 0) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (selectedTab == 1) Color(0xFF9F75FF) else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 12.dp)
                        .testTag("annual_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Annual",
                            color = if (selectedTab == 1) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedTab == 1) Color.Black else Color(0xFFF9D142))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SAVE 20%",
                                color = if (selectedTab == 1) Color(0xFFF9D142) else Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Billing Cards based on Selected Tab
            if (selectedTab == 0) {
                // (1) Monthly Tab Option
                
                // Card 1: Free Plan (Our starter plan.)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Free Plan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Our starter plan.", color = Color(0xFF9E93B3), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Button(
                                onClick = {
                                    authViewModel.upgradeSubscription("free")
                                    Toast.makeText(context, "Free Plan Activated (50 credits)", Toast.LENGTH_SHORT).show()
                                },
                                enabled = (currentUser?.subscriptionPlan != "free"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9F75FF).copy(alpha = 0.2f),
                                    disabledContainerColor = Color(0xFF1E1738)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("plan_free_button")
                            ) {
                                Text(
                                    text = if (currentUser?.subscriptionPlan == "free") "Active" else "Activate",
                                    color = if (currentUser?.subscriptionPlan == "free") Color(0xFF9E93B3) else Color(0xFF9F75FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "50 complimentary generation credits",
                            "Explore standard voice styles",
                            "Access to fundamental AI music synthesis studio"
                        ).forEach { feat ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.Check, "check", tint = Color(0xFF9F75FF), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(feat, color = Color(0xFF9E93B3), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Card 2: Monthly Pro Plan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                    border = BorderStroke(1.dp, Color(0xFF9F75FF).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Monthly Pro Plan", color = Color(0xFFF9D142), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("₹850", color = Color(0xFFF9D142), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" /month", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    selectedPlanToBuy = "Pro"
                                    showUpgradeDialog = false
                                    showPaymentScannerDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("plan_monthly_pro_button")
                            ) {
                                Text("Upgrade", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Save by paying annually! Taxes calculated at checkout",
                            color = Color(0xFF9F75FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "2,500 credits, refreshes monthly",
                            "Commercial use rights for new songs",
                            "Standard + Pro vocal styles (Sidhu, Lata, etc.)",
                            "Record, upload and clone your own voice",
                            "Priority composition queue (up to 10 songs)"
                        ).forEach { feat ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.Check, "check", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(feat, color = Color(0xFF9E93B3), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Card 3: Annual Subscription charges
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                    border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Annual Subscription charges", color = Color(0xFFF9D142), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("₹2,500", color = Color(0xFFF9D142), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" /month", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    selectedPlanToBuy = "Premier"
                                    showUpgradeDialog = false
                                    showPaymentScannerDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("plan_monthly_premier_button")
                            ) {
                                Text("Subscribe", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Save by paying annually! Taxes calculated at checkout",
                            color = Color(0xFFF9D142),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "10,000 credits, refreshes monthly",
                            "Exclusive access to SurMaya AI",
                            "Full commercial distribution licensing",
                            "Infinite voice clone presets",
                            "Ultimate priority VIP queue (immediate render)"
                        ).forEach { feat ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.Check, "check", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(feat, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // (2) Annual Tab Option
                
                // Card 1: Annual save 20%- Pro Plan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                    border = BorderStroke(1.dp, Color(0xFF9F75FF).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Annual save 20%- Pro Plan", color = Color(0xFFF9D142), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("₹680", color = Color(0xFFF9D142), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" /month", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    selectedPlanToBuy = "Pro"
                                    showUpgradeDialog = false
                                    showPaymentScannerDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("plan_annual_pro_button")
                            ) {
                                Text("Upgrade", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Saves ₹2,040 by billing yearly!\nTaxes calculated at checkout",
                            color = Color(0xFF39FF14),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "2,500 credits, refreshes monthly",
                            "Commercial use rights for new songs",
                            "Standard + Pro vocal styles (Sidhu, Lata, etc.)",
                            "Record, upload and clone your own voice",
                            "Priority composition queue (up to 10 songs)"
                        ).forEach { feat ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.Check, "check", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(feat, color = Color(0xFF9E93B3), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Card 2: Annual save 20%- Premier Plan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                    border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Annual save 20%- Premier Plan", color = Color(0xFFF9D142), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("₹2,000", color = Color(0xFFF9D142), fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" /month", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    selectedPlanToBuy = "Premier"
                                    showUpgradeDialog = false
                                    showPaymentScannerDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("plan_annual_premier_button")
                            ) {
                                Text("Subscribe", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Saves ₹6,000 by billing yearly!\nTaxes calculated at checkout",
                            color = Color(0xFF39FF14),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "10,000 credits, refreshes monthly",
                            "Exclusive access to SurMaya AI",
                            "Full commercial distribution licensing",
                            "Infinite voice clone presets",
                            "Ultimate priority VIP queue (immediate render)"
                        ).forEach { feat ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.Check, "check", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(feat, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout button
            OutlinedButton(
                onClick = {
                    authViewModel.logout {
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        onLogoutSuccess()
                    }
                },
                border = BorderStroke(1.dp, Color(0xFFEFB8C8)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Text("Sign Out Session", color = Color(0xFFEFB8C8), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Comprehensive Plans comparison Dialog
    if (showUpgradeDialog) {
        Dialog(onDismissRequest = { showUpgradeDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF140D2A),
                border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select Premium Tier", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    val dialogPlans = if (selectedTab == 0) {
                        listOf(
                            Triple("Free Plan", "free", "Free"),
                            Triple("Monthly Pro Plan", "pro", "₹850 /mo"),
                            Triple("Annual Subscription charges", "premier", "₹2,500 /mo")
                        )
                    } else {
                        listOf(
                            Triple("Annual save 20%- Pro Plan", "pro", "₹680 /mo"),
                            Triple("Annual save 20%- Premier Plan", "premier", "₹2,000 /mo")
                        )
                    }

                    dialogPlans.forEach { (name, type, price) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33000000))
                                .clickable {
                                    if (type == "free") {
                                        authViewModel.upgradeSubscription("free")
                                        showUpgradeDialog = false
                                    } else {
                                        selectedPlanToBuy = if (type == "pro") "Pro" else "Premier"
                                        showUpgradeDialog = false
                                        showPaymentScannerDialog = true
                                    }
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Click to select plan option", color = Color(0xFF9E93B3), fontSize = 11.sp)
                            }
                            Text(price, color = Color(0xFFF9D142), fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Payment scan dialogue (matching UPI / PhonePe specifications)
    if (showPaymentScannerDialog) {
        Dialog(onDismissRequest = { showPaymentScannerDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF140D2A),
                border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Pay via Indian UPI Scanner", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Subscription: $selectedPlanToBuy Plan", color = Color(0xFFF9D142), fontSize = 14.sp, fontWeight = FontWeight.Black)

                    // Simulated QR scan image placeholder
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFF9D142), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // QR scan simulation vector
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "QR Scanner placeholder",
                            tint = Color.Black,
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    Text(
                        text = "Scan this QR with PhonePe, GooglePay, or any BHIM UPI app to pay instantly.",
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPaymentScannerDialog = false },
                            border = BorderStroke(1.dp, Color(0xFF3E3556)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                authViewModel.upgradeSubscription(selectedPlanToBuy.lowercase())
                                Toast.makeText(context, "Payment successful! Activated $selectedPlanToBuy Plan.", Toast.LENGTH_LONG).show()
                                showPaymentScannerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.weight(1.2f).testTag("payment_success_button")
                        ) {
                            Text("Simulate Pay", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
