package com.example.week2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val page = Color(0xFF0B1321)
private val panel = Color(0xFF142035)
private val panelSoft = Color(0xFF1B2A40)
private val line = Color(0xFF304059)
private val mint = Color(0xFF9BE5CB)
private val ink = Color(0xFFF0F6FF)
private val secondary = Color(0xFFA6B4CB)
private val amber = Color(0xFFFFC77A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = mint,
                    background = page,
                    surface = panel,
                    onSurface = ink,
                    onBackground = ink
                )
            ) {
                DevClusterScreen()
            }
        }
    }
}

/** Ekranın tek state sahibi; alt bileşenler yalnızca veri ve olay alır. */
@Composable
private fun DevClusterScreen() {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Tümü") }

    var hardwareItems by remember { mutableStateOf(HardwareCatalog.initialItems) }
    var totalSpent by remember { mutableDoubleStateOf(0.0) }
    var reservedCount by remember { mutableIntStateOf(0) }

    // Sabit katalogda arama, yalnızca arama metni değiştiğinde tekrar hesaplanır.
    val matchingSearchIds = remember(searchQuery) {
        filterHardware(HardwareCatalog.initialItems, searchQuery).map { it.id }.toSet()
    }
    val matchingCategoryIds = remember(selectedCategory) {
        filterCategory(HardwareCatalog.initialItems, selectedCategory).map { it.id }.toSet()
    }
    // Stok güncellemesi arama algoritmasını yeniden çalıştırmadan ekrana yansır.
    val visibleItems = remember(hardwareItems, matchingSearchIds, matchingCategoryIds) {
        hardwareItems.filter { it.id in matchingSearchIds && it.id in matchingCategoryIds }
    }
    val totalStock = remember(hardwareItems) { hardwareItems.sumOf { it.stockCount } }
    val categories = remember {
        listOf("Tümü") + HardwareCatalog.initialItems.map { it.category }.distinct()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(page),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 30.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeaderBlock()
        }
        item {
            DashboardSummary(
                totalSpent = totalSpent,
                reservedCount = reservedCount,
                availableUnits = totalStock
            )
        }
        item {
            Column {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Kaynak kataloğu", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = ink)
                    Text("${visibleItems.size} kayıt", fontSize = 12.sp, color = secondary)
                }
                Spacer(Modifier.height(12.dp))
                HardwareSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it }
                )
            }
        }
        item {
            CategoryFilters(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
        }
        if (visibleItems.isEmpty()) {
            item {
                EmptyCatalog(
                    onReset = {
                        searchQuery = ""
                        selectedCategory = "Tümü"
                    }
                )
            }
        } else {
            items(items = visibleItems, key = { it.id }) { item ->
                HardwareCard(item = item, onAllocate = { chosen ->
                    // Aynı anda hızlı tıklamalarda güncel stoku kontrol et.
                    val availableItem = hardwareItems.firstOrNull { it.id == chosen.id }
                    if (availableItem != null && availableItem.stockCount > 0) {
                        // Mevcut nesne mutasyona uğramaz; güncellenmiş kopyası yazılır.
                        hardwareItems = hardwareItems.map { current ->
                            if (current.id == chosen.id) {
                                current.copy(stockCount = current.stockCount - 1)
                            } else {
                                current
                            }
                        }
                        totalSpent += availableItem.cost
                        reservedCount += 1
                    }
                })
            }
        }
        item {
            Text(
                text = "DEVCLUSTER  ·  YEREL SİMÜLASYON / VERİLER SUNUCUYA GÖNDERİLMEZ",
                color = secondary.copy(alpha = 0.66f),
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun HeaderBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(mint, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("SİSTEM ÇEVRİMİÇİ", color = mint, fontSize = 11.sp, letterSpacing = 1.7.sp)
        }
        Text(
            "DevCluster",
            fontSize = 35.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ink,
            letterSpacing = (-1.2).sp
        )
        Text(
            "Geliştirme ortamındaki cihazları tek bir havuzdan ayır, stoku ve maliyeti takip et.",
            color = secondary,
            lineHeight = 21.sp,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DashboardSummary(totalSpent: Double, reservedCount: Int, availableUnits: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = panel,
        border = BorderStroke(1.dp, line.copy(alpha = 0.7f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("DAĞITIM ÖZETİ", color = secondary, fontSize = 11.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                money(totalSpent),
                color = mint,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text("Toplam ayrılan kaynak maliyeti", color = secondary, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(line))
            Spacer(Modifier.height(14.dp))
            Row {
                SummaryValue("AYRILAN", "$reservedCount adet", Modifier.weight(1f))
                SummaryValue("KALAN STOK", "$availableUnits adet", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, color = secondary, fontSize = 10.sp, letterSpacing = 0.9.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Stateless: arama bilgisinin sahibi üst ekrandır. */
@Composable
private fun HardwareSearchBar(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        placeholder = {
            Text("Ad, kategori veya cihaz kodu ara", fontSize = 13.sp, color = secondary)
        },
        leadingIcon = { Text("⌕", fontSize = 25.sp, color = mint) },
        trailingIcon = {
            if (value.isNotBlank()) {
                Text(
                    "Temizle",
                    modifier = Modifier.clickable { onValueChange("") }.padding(8.dp),
                    color = mint,
                    fontSize = 11.sp
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = panel,
            unfocusedContainerColor = panel,
            focusedTextColor = ink,
            unfocusedTextColor = ink,
            cursorColor = mint,
            focusedIndicatorColor = mint,
            unfocusedIndicatorColor = line
        )
    )
}

/** Stateless: seçimi değiştirme olayı yalnızca yukarı taşınır. */
@Composable
private fun CategoryFilters(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = categories, key = { it }) { category ->
            val active = category == selected
            Surface(
                modifier = Modifier.clickable { onSelect(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (active) mint else panelSoft,
                border = BorderStroke(1.dp, if (active) mint else line)
            ) {
                Text(
                    category,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    color = if (active) page else secondary,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/** Stateless: tıklama/yazma durumu tutulmaz, yalnızca verilen item gösterilir. */
@Composable
private fun HardwareCard(item: HardwareItem, onAllocate: (HardwareItem) -> Unit) {
    val available = item.stockCount > 0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = available) { onAllocate(item) }
            .padding(vertical = 2.dp),
        color = panel,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, line.copy(alpha = 0.7f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = panelSoft,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = categoryCode(item.category),
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 13.dp),
                        color = mint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        color = ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(item.category, color = secondary, fontSize = 12.sp)
                }
                Text("#${item.id}", color = secondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(line.copy(alpha = 0.7f)))
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("BİRİM MALİYET", fontSize = 10.sp, color = secondary, letterSpacing = 0.8.sp)
                    Text(money(item.cost), fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ink)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(7.dp).background(
                                if (available) mint else amber, CircleShape
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (available) "${item.stockCount} stokta" else "Stok tükendi",
                            fontSize = 11.sp,
                            color = if (available) mint else amber
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onAllocate(item) },
                        enabled = available,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mint,
                            contentColor = page,
                            disabledContainerColor = line,
                            disabledContentColor = secondary
                        ),
                        modifier = Modifier.height(37.dp)
                    ) {
                        Text(if (available) "Ayır +" else "Kapalı", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCatalog(onReset: () -> Unit) {
    Surface(
        color = panel,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp)) {
            Text("Sonuç bulunamadı", color = ink, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Arama metnini ya da kategoriyi değiştirebilirsin.", color = secondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Filtreleri sıfırla →",
                modifier = Modifier.clickable { onReset() }.padding(vertical = 7.dp),
                color = mint,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

private fun categoryCode(category: String) = when (category) {
    "Geliştirici Bilgisayarı" -> "DEV"
    "Yapay Zeka / GPU" -> "GPU"
    "Mobil Test Cihazı" -> "MOB"
    "Monitör" -> "DSP"
    "Odaklanma Ekipmanı" -> "AUD"
    "IoT Geliştirme Kiti" -> "IOT"
    "Ergonomik Klavye" -> "KEY"
    else -> "SYS"
}

private fun money(value: Double): String = "$" + String.format(Locale.US, "%,.2f", value)
