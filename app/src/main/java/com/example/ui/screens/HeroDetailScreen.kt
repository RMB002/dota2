package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.HeroRepository
import com.example.model.Ability
import com.example.model.Hero
import com.example.model.RoleBuild
import com.example.ui.theme.PrimaryPurple

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.HeroState
import com.example.ui.viewmodels.HeroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDetailScreen(heroId: String?, onBackClick: () -> Unit, viewModel: HeroViewModel = viewModel()) {
    val heroState by viewModel.heroState.collectAsState()

    LaunchedEffect(heroId) {
        if (heroId != null) {
            viewModel.loadHero(heroId)
        }
    }

    when (val state = heroState) {
        is HeroState.Loading, is HeroState.GeneratingBuild -> {
            val loadingText = if (state is HeroState.GeneratingBuild) "درحال ساخت بهترین بیلد با توجه به مچ‌آپ توسط Gemini..." else "در حال بارگذاری..."
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (state is HeroState.GeneratingBuild) "تحلیل هوش مصنوعی..." else "در حال بارگذاری...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(loadingText, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
        is HeroState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("خطا", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("خطا در دریافت اطلاعات: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is HeroState.Success -> {
            val hero = state.hero
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(hero.name, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                if (hero.abilities.isEmpty() && hero.roles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        MatchupForm(
                            onGenerate = { allies, enemies ->
                                viewModel.generateGuide(hero, allies, enemies)
                            }
                        )
                    }
                } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AsyncImage(
                    model = hero.imageUrl,
                    contentDescription = hero.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text("لاین پیشنهادی", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(hero.lanes.firstOrNull() ?: "-", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text("سختی هیرو", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.width(16.dp).height(6.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
                            Box(modifier = Modifier.width(16.dp).height(6.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
                            Box(modifier = Modifier.width(16.dp).height(6.dp).background(MaterialTheme.colorScheme.outline, RoundedCornerShape(50)))
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "نکات تاکتیکی",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .drawBehind { 
                                drawLine(color = PrimaryPurple, start = Offset(size.width, 0f), end = Offset(size.width, size.height), strokeWidth = 6f)
                            }
                            .padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    hero.tacticalTips.forEach { tip ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text("•", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp))
                            Text(tip, fontSize = 11.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            item {
                SectionTitle("قابلیت‌ها (Abilities)")
                hero.abilities.forEach { ability ->
                    AbilityItem(ability)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (hero.roles.isNotEmpty()) {
                item {
                    RoleSwitcher(hero.roles)
                }
            }
        }
    }
    }
    }
}
}

@Composable
fun MatchupForm(onGenerate: (String, String) -> Unit) {
    var allies by remember { mutableStateOf("") }
    var enemies by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("تحلیل هوشمند بیلد", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("هیروهای هم‌تیمی و حریف را وارد کنید تا بهترین بیلد با توجه به مچ‌آپ پیشنهاد شود.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = allies,
            onValueChange = { allies = it },
            label = { Text("هیروهای هم‌تیمی (اختیاری)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = enemies,
            onValueChange = { enemies = it },
            label = { Text("هیروهای حریف (اختیاری)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onGenerate(allies, enemies) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("ساخت بیلد اختصاصی", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun AbilityItem(ability: Ability) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val formattedAbilityName = ability.name.lowercase().replace(" ", "_").replace("'", "")
            val imageUrl = "https://cdn.cloudflare.steamstatic.com/apps/dota2/images/dota_react/abilities/${formattedAbilityName}.png"
            
            AsyncImage(
                model = imageUrl,
                contentDescription = ability.name,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(ability.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(ability.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(8.dp)) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("نکته حرفه‌ای: ${ability.proTip}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RoleSwitcher(roles: List<RoleBuild>) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (roles.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                roles.forEachIndexed { index, role ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTabIndex = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role.roleName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            // If only one role, just show its name as a header
            Text(
                text = roles.first().roleName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        val currentRole = roles[selectedTabIndex]
        RoleBuildDetails(currentRole)
    }
}

@Composable
fun RoleBuildDetails(roleBuild: RoleBuild) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("آیتم‌های پیشنهادی", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        ItemsRow(title = "Early", items = roleBuild.earlyGameItems)
        ItemsRow(title = "Core", items = roleBuild.coreTimings, isCore = true)
        ItemsRow(title = "Sit.", items = roleBuild.situationalItems)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            NeutralItemBox("T1", roleBuild.neutralItems.tier1.firstOrNull() ?: "")
            NeutralItemBox("T2", roleBuild.neutralItems.tier2.firstOrNull() ?: "")
            NeutralItemBox("T3", roleBuild.neutralItems.tier3.firstOrNull() ?: "")
            NeutralItemBox("T4", roleBuild.neutralItems.tier4.firstOrNull() ?: "")
            NeutralItemBox("T5", roleBuild.neutralItems.tier5.firstOrNull() ?: "")
        }
    }
}

@Composable
fun ItemsRow(title: String, items: List<String>, isCore: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isCore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items.take(3).forEach { item ->
                val formattedName = item.lowercase().replace(" ", "_").replace("'", "").replace("-", "_")
                val imageUrl = "https://cdn.cloudflare.steamstatic.com/apps/dota2/images/dota_react/items/${formattedName}.png"

                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCore) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.background)
                        .border(1.dp, if (isCore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.4f
                    )
                    Text(item, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.padding(2.dp))
                }
            }
        }
    }
}

@Composable
fun RowScope.NeutralItemBox(tier: String, item: String) {
    val formattedName = item.lowercase().replace(" ", "_").replace("'", "").replace("-", "_")
    val imageUrl = "https://cdn.cloudflare.steamstatic.com/apps/dota2/images/dota_react/items/${formattedName}.png"

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = item,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.4f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tier, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(item, fontSize = 7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 2.dp), fontWeight = FontWeight.Bold)
        }
    }
}
