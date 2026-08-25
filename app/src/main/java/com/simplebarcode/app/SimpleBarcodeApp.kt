package com.simplebarcode.app

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simplebarcode.app.barcode.BarcodeGenerator
import com.simplebarcode.app.barcode.shareBarcode
import com.simplebarcode.app.data.BarcodeItem
import com.simplebarcode.app.data.BarcodeType
import com.simplebarcode.app.data.DarkMode
import com.simplebarcode.app.ui.theme.SimpleBarcodeTheme
import com.simplebarcode.app.ui.theme.ThemeOptions
import com.simplebarcode.app.widget.BarcodeWidgetProvider

private sealed interface AppScreen {
    data object Home : AppScreen
    data class Edit(val id: Long? = null) : AppScreen
    data class Detail(val id: Long) : AppScreen
    data object Settings : AppScreen
}

@Composable
fun SimpleBarcodeApp(
    initialBarcodeId: Long?,
    viewModel: MainViewModel = viewModel(),
) {
    val allItems by viewModel.items.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var screen by remember {
        mutableStateOf<AppScreen>(initialBarcodeId?.let(AppScreen::Detail) ?: AppScreen.Home)
    }

    LaunchedEffect(allItems, screen) {
        val detail = screen as? AppScreen.Detail
        if (detail != null && allItems.none { it.id == detail.id }) screen = AppScreen.Home
    }

    SimpleBarcodeTheme(settings.themeIndex, settings.darkMode) { isDark ->
        SyncSystemBars(isDark)

        BackHandler(enabled = screen != AppScreen.Home) { screen = AppScreen.Home }

        when (val current = screen) {
            AppScreen.Home -> HomeScreen(
                items = allItems,
                onAdd = { screen = AppScreen.Edit() },
                onOpen = { screen = AppScreen.Detail(it) },
                onFavorite = viewModel::toggleFavorite,
                onSettings = { screen = AppScreen.Settings },
            )
            is AppScreen.Edit -> EditBarcodeScreen(
                existing = current.id?.let(viewModel::getItem),
                onBack = { screen = AppScreen.Home },
                onSave = { id, name, content, type, showText ->
                    val savedId = viewModel.save(id, name, content, type, showText)
                    screen = AppScreen.Detail(savedId)
                },
            )
            is AppScreen.Detail -> viewModel.getItem(current.id)?.let { item ->
                BarcodeDetailScreen(
                    item = item,
                    onBack = { screen = AppScreen.Home },
                    onEdit = { screen = AppScreen.Edit(item.id) },
                    onDelete = {
                        viewModel.delete(item.id)
                        screen = AppScreen.Home
                    },
                )
            }
            AppScreen.Settings -> SettingsScreen(
                themeIndex = settings.themeIndex,
                darkMode = settings.darkMode,
                onThemeChange = viewModel::setTheme,
                onDarkModeChange = viewModel::setDarkMode,
                onBack = { screen = AppScreen.Home },
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun SyncSystemBars(isDark: Boolean) {
    val view = LocalView.current
    val surface = MaterialTheme.colorScheme.surface
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = surface.toArgb()
            window.navigationBarColor = surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    items: List<BarcodeItem>,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onFavorite: (Long) -> Unit,
    onSettings: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(items, query) {
        items.filter { item ->
            query.isBlank() || item.name.contains(query, ignoreCase = true) ||
                item.content.contains(query, ignoreCase = true)
        }.sortedWith(compareByDescending<BarcodeItem> { it.isFavorite }.thenByDescending { it.createdAt })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("簡單條碼", fontWeight = FontWeight.Black)
                        Text("常用條碼，一點就開", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("建立條碼", fontWeight = FontWeight.Bold) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            if (items.isNotEmpty()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜尋名稱或內容") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                )
                Spacer(Modifier.height(12.dp))
            }

            if (items.isEmpty()) {
                EmptyHome(onAdd)
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("找不到符合的條碼", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filtered, key = { it.id }) { item ->
                        BarcodeCard(item, onOpen, onFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHome(onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.size(112.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(58.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("建立第一張條碼卡", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("會員卡、產品碼與門禁條碼都能放在這裡", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("開始建立")
            }
        }
    }
}

@Composable
private fun BarcodeCard(
    item: BarcodeItem,
    onOpen: (Long) -> Unit,
    onFavorite: (Long) -> Unit,
) {
    Card(
        onClick = { onOpen(item.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(item.type.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onFavorite(item.id) }) {
                    Icon(
                        if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (item.isFavorite) "取消最愛" else "加入最愛",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            BarcodeImage(item, Modifier.fillMaxWidth().height(82.dp))
            if (item.showText) {
                Text(
                    item.content,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditBarcodeScreen(
    existing: BarcodeItem?,
    onBack: () -> Unit,
    onSave: (Long?, String, String, BarcodeType, Boolean) -> Unit,
) {
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var content by rememberSaveable(existing?.id) { mutableStateOf(existing?.content.orEmpty()) }
    var typeName by rememberSaveable(existing?.id) { mutableStateOf(existing?.type?.name ?: BarcodeType.CODE_128.name) }
    var showText by rememberSaveable(existing?.id) { mutableStateOf(existing?.showText ?: true) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val type = BarcodeType.valueOf(typeName)
    val contentError = if (submitted) BarcodeGenerator.validate(content.trim(), type) else null
    val nameError = submitted && name.isBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (existing == null) "建立條碼" else "編輯條碼", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("條碼類型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BarcodeType.entries.forEach { option ->
                    val isSelected = type == option
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            typeName = option.name
                            submitted = false
                        },
                        label = {
                            Text(
                                option.displayName,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "已選擇",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("名稱") },
                placeholder = { Text("例如：超市會員卡") },
                supportingText = { if (nameError) Text("請輸入條碼名稱") },
                isError = nameError,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    submitted = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("條碼內容") },
                placeholder = { Text(type.inputHint) },
                supportingText = { Text(contentError ?: type.inputHint) },
                isError = contentError != null,
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text("即時預覽", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val valid = content.isNotBlank() && BarcodeGenerator.validate(content.trim(), type) == null
                    if (valid) {
                        BarcodeImage(
                            BarcodeItem(0, name, content.trim(), type, showText),
                            Modifier.fillMaxWidth().height(130.dp),
                        )
                        if (showText) Text(content, color = Color(0xFF1D1B20), fontSize = 13.sp)
                    } else {
                        Box(Modifier.fillMaxWidth().height(130.dp), contentAlignment = Alignment.Center) {
                            Text("輸入有效內容後顯示預覽", color = Color(0xFF66666F))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("顯示條碼文字", fontWeight = FontWeight.Bold)
                    Text("在條碼下方顯示原始內容", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = showText, onCheckedChange = { showText = it })
            }
            Button(
                onClick = {
                    submitted = true
                    if (name.isNotBlank() && BarcodeGenerator.validate(content.trim(), type) == null) {
                        onSave(existing?.id, name, content, type, showText)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(if (existing == null) "建立條碼" else "儲存變更", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeDetailScreen(
    item: BarcodeItem,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    KeepScreenBright()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "編輯") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, contentDescription = "刪除") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.5f))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 3.dp,
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    BarcodeImage(item, Modifier.fillMaxWidth().aspectRatio(2.8f))
                    if (item.showText) {
                        Text(
                            item.content,
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color(0xFF1D1B20),
                            fontSize = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            AssistChip(onClick = {}, label = { Text(item.type.displayName) })
            Text(
                "螢幕亮度已暫時提高，方便掃描",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { shareBarcode(context, item) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("分享")
                }
                Button(onClick = { requestBarcodeWidget(context) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Widgets, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("桌面小工具")
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("刪除條碼？") },
            text = { Text("「${item.name}」將會從 APP 與相關桌面小工具中移除。") },
            confirmButton = {
                TextButton(onClick = onDelete) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun KeepScreenBright() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val oldBrightness = window?.attributes?.screenBrightness
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.attributes = window?.attributes?.apply { screenBrightness = 1f }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (oldBrightness != null) {
                window.attributes = window.attributes.apply { screenBrightness = oldBrightness }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    themeIndex: Int,
    darkMode: DarkMode,
    onThemeChange: (Int) -> Unit,
    onDarkModeChange: (DarkMode) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("設定", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
        ) {
            Text("主題色彩", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text("點一下立即套用", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            ThemeOptions.chunked(3).forEachIndexed { rowIndex, rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    rowItems.forEachIndexed { columnIndex, option ->
                        val index = rowIndex * 3 + columnIndex
                        Column(
                            modifier = Modifier.width(92.dp).clip(RoundedCornerShape(16.dp)).clickable { onThemeChange(index) }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(option.primary)
                                    .then(
                                        if (themeIndex == index) Modifier.border(4.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (themeIndex == index) Text("✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(option.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (rowIndex == 0) Spacer(Modifier.height(8.dp))
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))
            Text("外觀模式", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            DarkMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onDarkModeChange(mode) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = darkMode == mode, onClick = { onDarkModeChange(mode) })
                    Column {
                        Text(mode.displayName, fontWeight = FontWeight.Bold)
                        if (mode == DarkMode.SYSTEM) {
                            Text("隨手機的深色模式自動切換", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 24.dp))
            Text("關於", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Text("簡單條碼 1.0.0", fontWeight = FontWeight.Bold)
            Text("條碼只儲存在這部手機，不會上傳到網路。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun BarcodeImage(item: BarcodeItem, modifier: Modifier = Modifier) {
    val bitmap = remember(item.content, item.type) {
        runCatching { BarcodeGenerator.createBitmap(item.content, item.type, 1200, 360) }.getOrNull()
    }
    Box(modifier.background(Color.White).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "${item.name} 條碼",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Text("無法產生條碼", color = Color(0xFF66666F))
        }
    }
}

private fun requestBarcodeWidget(context: Context) {
    val manager = AppWidgetManager.getInstance(context)
    val provider = ComponentName(context, BarcodeWidgetProvider::class.java)
    if (manager.isRequestPinAppWidgetSupported) {
        manager.requestPinAppWidget(provider, null, null)
        return
    }
    Toast.makeText(context, "請從桌面長按，選擇「小工具」加入簡單條碼", Toast.LENGTH_LONG).show()
}
