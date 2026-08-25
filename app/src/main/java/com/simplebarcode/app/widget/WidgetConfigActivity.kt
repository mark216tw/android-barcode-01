package com.simplebarcode.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplebarcode.app.MainActivity
import com.simplebarcode.app.data.BarcodeItem
import com.simplebarcode.app.data.BarcodeRepository
import com.simplebarcode.app.data.SettingsRepository
import com.simplebarcode.app.ui.theme.SimpleBarcodeTheme

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val repository = BarcodeRepository(applicationContext)
        val settingsRepository = SettingsRepository(applicationContext)
        setContent {
            val items by repository.items.collectAsState()
            val settings by settingsRepository.settings.collectAsState()
            SimpleBarcodeTheme(settings.themeIndex, settings.darkMode) {
                WidgetConfigScreen(
                    items = items,
                    onSelect = ::complete,
                    onCreate = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }

    private fun complete(barcodeId: Long) {
        WidgetStore.save(this, appWidgetId, barcodeId)
        BarcodeWidgetProvider.update(this, AppWidgetManager.getInstance(this), appWidgetId)
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
        )
        finish()
    }
}

@Composable
private fun WidgetConfigScreen(
    items: List<BarcodeItem>,
    onSelect: (Long) -> Unit,
    onCreate: () -> Unit,
) {
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text("選擇桌面條碼", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("小工具會隨尺寸自動調整", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("還沒有可使用的條碼")
                        Button(onClick = onCreate, modifier = Modifier.padding(top = 16.dp)) {
                            Text("先建立條碼")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        Card(
                            onClick = { onSelect(item.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(item.name, fontWeight = FontWeight.ExtraBold)
                                Text(item.type.displayName, color = MaterialTheme.colorScheme.primary)
                                Text(item.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}
