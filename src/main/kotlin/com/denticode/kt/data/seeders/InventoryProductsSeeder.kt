package com.denticode.kt.data.seeders

import com.denticode.kt.data.InventoryProductMovementsTable
import com.denticode.kt.data.InventoryProductsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.random.Random

object InventoryProductsSeeder {

    data class SeedProduct(
        val name: String,
        val code: String,
        val categoryName: String,
        val unit: String,
        val purchasePrice: Double,
        val sellingPrice: Double,
        val minStock: Int,
        val maxStock: Int,
    )

    fun seed(
        config: DemoDataConfig,
        categories: List<InventoryCategoriesSeeder.SeedInventoryCategory>,
        suppliers: List<SuppliersSeeder.SeedSupplier>,
    ) {
        if (InventoryProductsTable.selectAll().count() > 0) return

        val rng = Random(42)
        val now = System.currentTimeMillis()

        val products = listOf(
            SeedProduct("Resina composite A2", "MAT-001", "Material restaurativo", "unid", 35.0, 55.0, 10, 100),
            SeedProduct("Resina composite A3", "MAT-002", "Material restaurativo", "unid", 35.0, 55.0, 10, 100),
            SeedProduct("Amalgama dental", "MAT-003", "Material restaurativo", "kg", 120.0, 200.0, 2, 20),
            SeedProduct("Cemento de ionómero", "MAT-004", "Material restaurativo", "unid", 45.0, 75.0, 5, 50),
            SeedProduct("Flúor gel 10%", "PRE-001", "Material preventivo", "tubo", 15.0, 30.0, 10, 80),
            SeedProduct("Sellante dental", "PRE-002", "Material preventivo", "jeringa", 80.0, 130.0, 5, 40),
            SeedProduct("Jeringa dental desc.", "ANE-001", "Anestesia", "unid", 2.5, 5.0, 50, 500),
            SeedProduct("Aguja 27G 0.4x25mm", "ANE-002", "Anestesia", "unid", 0.8, 1.5, 100, 1000),
            SeedProduct("Lidocaína 2% con epinefrina", "ANE-003", "Anestesia", "cartucho", 3.5, 7.0, 30, 300),
            SeedProduct("Alginato de impresión", "IMP-001", "Material de impresión", "bolsa", 25.0, 45.0, 10, 60),
            SeedProduct("Silicona de impresión", "IMP-002", "Material de impresión", "kit", 150.0, 250.0, 3, 20),
            SeedProduct("Cera para/modelar", "IMP-003", "Material de impresión", "hoja", 5.0, 10.0, 20, 100),
            SeedProduct("Excavador dental #23/24", "INS-001", "Instrumental", "unid", 45.0, 80.0, 5, 30),
            SeedProduct("Sonda periodontal", "INS-002", "Instrumental", "unid", 35.0, 60.0, 5, 25),
            SeedProduct("Espejo dental #4", "INS-003", "Instrumental", "unid", 15.0, 28.0, 10, 50),
            SeedProduct("Pinza portaguantes", "INS-004", "Instrumental", "unid", 80.0, 140.0, 3, 15),
            SeedProduct("Hilo de sutura 3-0", "SUT-001", "Material de sutura", "rollo", 25.0, 45.0, 10, 50),
            SeedProduct("Hilo de sutura 4-0", "SUT-002", "Material de sutura", "rollo", 28.0, 50.0, 10, 50),
            SeedProduct("Guantes nitrilo S", "EPP-001", "EPP", "caja", 35.0, 55.0, 20, 200),
            SeedProduct("Guantes nitrilo M", "EPP-002", "EPP", "caja", 35.0, 55.0, 20, 200),
            SeedProduct("Guantes nitrilo L", "EPP-003", "EPP", "caja", 35.0, 55.0, 20, 200),
            SeedProduct("Mascarilla quirúrgica", "EPP-004", "EPP", "caja", 25.0, 42.0, 15, 150),
            SeedProduct("Gorro desechable", "EPP-005", "EPP", "bolsa", 8.0, 14.0, 10, 80),
            SeedProduct("Desinfectante instrumental", "LIM-001", "Limpieza y desinfección", "litro", 30.0, 50.0, 5, 40),
            SeedProduct("Gel lavado de manos", "LIM-002", "Limpieza y desinfección", "litro", 15.0, 28.0, 5, 30),
            SeedProduct("Conos de gutapercha #25", "END-001", "Endodoncia", "tubo", 20.0, 35.0, 10, 60),
            SeedProduct("Lima Niti #25 25mm", "END-002", "Endodoncia", "unid", 15.0, 28.0, 10, 50),
            SeedProduct("Sellador endodóntico", "END-003", "Endodoncia", "unid", 60.0, 100.0, 5, 25),
            SeedProduct("Brackets metálicos", "ORT-001", "Ortodoncia", "kit", 200.0, 350.0, 3, 15),
            SeedProduct("Alambre ortodóntico .016", "ORT-002", "Ortodoncia", "rollo", 40.0, 70.0, 5, 30),
            SeedProduct("Ligaduras ortodónticas", "ORT-003", "Ortodoncia", "bolsa", 25.0, 45.0, 10, 50),
            SeedProduct("Acrílico autopolimerizable", "PRO-001", "Prótesis", "kit", 80.0, 140.0, 3, 15),
            SeedProduct("Yeso dental", "GEN-001", "General", "kg", 8.0, 15.0, 10, 80),
            SeedProduct("Papel de articulación", "GEN-002", "General", "rollo", 12.0, 22.0, 5, 30),
        )

        val categoryMap = categories.associateBy { it.name }
        val supplierIds = suppliers.map { it.id }

        for (p in products) {
            val catId = categoryMap[p.categoryName]?.id
            val supId = supplierIds[rng.nextInt(supplierIds.size)]
            val stock = rng.nextInt(p.minStock, p.maxStock + 1)

            val productId = InventoryProductsTable.insert {
                it[name] = p.name
                it[code] = p.code
                it[categoryId] = catId
                it[unit] = p.unit
                it[purchasePrice] = p.purchasePrice
                it[sellingPrice] = p.sellingPrice
                it[currentStock] = stock
                it[minStock] = p.minStock
                it[maxStock] = p.maxStock
                it[supplierId] = supId
                it[isActive] = true
                it[createdAtEpochMs] = now - rng.nextLong(0, 30L * 86_400_000)
            } get InventoryProductsTable.id

            InventoryProductMovementsTable.insert {
                it[InventoryProductMovementsTable.productId] = productId
                it[quantityChange] = stock
                it[type] = "INITIAL"
                it[InventoryProductMovementsTable.note] = "Stock inicial"
                it[createdAtEpochMs] = now - rng.nextLong(0, 14L * 86_400_000)
            }

            val adjustments = rng.nextInt(0, 3)
            for (adj in 0 until adjustments) {
                val change = -rng.nextInt(1, 5)
                InventoryProductMovementsTable.insert {
                    it[InventoryProductMovementsTable.productId] = productId
                    it[quantityChange] = change
                    it[type] = "CONSUMPTION"
                    it[InventoryProductMovementsTable.note] = "Uso en tratamiento"
                    it[createdAtEpochMs] = now - rng.nextLong(0, 7L * 86_400_000)
                }
            }
        }
    }
}
