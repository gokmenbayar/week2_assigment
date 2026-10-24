package com.example.week2

/** Ders için verilen sabit donanım havuzu. */
object HardwareCatalog {
    val initialItems = listOf(
        HardwareItem("101", "MacBook Pro M3 Max", "Geliştirici Bilgisayarı", 3499.0, 5),
        HardwareItem("102", "NVIDIA RTX 4090 Sunucu", "Yapay Zeka / GPU", 1599.0, 2),
        HardwareItem("103", "Google Pixel 8 Pro", "Mobil Test Cihazı", 999.0, 8),
        HardwareItem("104", "Dell UltraSharp 49\"", "Monitör", 1199.0, 4),
        HardwareItem("105", "Sony WH-1000XM5 Kulaklık", "Odaklanma Ekipmanı", 399.0, 12),
        HardwareItem("106", "Raspberry Pi 5 8GB", "IoT Geliştirme Kiti", 85.0, 25),
        HardwareItem("107", "HHKB Professional Keyboard", "Ergonomik Klavye", 299.0, 0),
        HardwareItem("108", "AMD Ryzen Threadripper", "Yapay Zeka / GPU", 4999.0, 1)
    )
}

/** Compose bağımlılığı olmayan filtreleme: kontrolü ve testi kolay. */
fun filterHardware(items: List<HardwareItem>, query: String): List<HardwareItem> {
    val term = query.trim()
    if (term.isEmpty()) return items
    return items.filter { hardware ->
        hardware.name.contains(term, ignoreCase = true) ||
            hardware.category.contains(term, ignoreCase = true) ||
            hardware.id.contains(term, ignoreCase = true)
    }
}

fun filterCategory(items: List<HardwareItem>, category: String): List<HardwareItem> =
    if (category == "Tümü") items else items.filter { it.category == category }
