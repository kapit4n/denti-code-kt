package com.denticode.kt.export

/** Fila exportable del listado de stock (independiente de la capa UI). */
data class InventoryExportRow(
    val code: String,
    val name: String,
    val category: String,
    val consultory: String,
    val quantity: Int,
    val unit: String,
    val minQuantity: Int,
    val status: String,
    val updatedLabel: String,
)

private fun inventoryCsvField(value: String): String {
    val v = value.replace("\"", "\"\"")
    return if (v.contains(';') || v.contains('\n') || v.contains('"')) "\"$v\"" else v
}

/** Listado de stock en CSV (separador `;`, compatible con Excel en español). */
fun renderInventoryCsv(rows: List<InventoryExportRow>): String =
    buildString {
        appendLine("Código;Insumo;Categoría;Consultorio;Cantidad;Unidad;Stock mín.;Estado;Última actualización")
        rows.forEach { r ->
            appendLine(
                listOf(
                    inventoryCsvField(r.code),
                    inventoryCsvField(r.name),
                    inventoryCsvField(r.category),
                    inventoryCsvField(r.consultory),
                    r.quantity.toString(),
                    inventoryCsvField(r.unit),
                    r.minQuantity.toString(),
                    inventoryCsvField(r.status),
                    inventoryCsvField(r.updatedLabel),
                ).joinToString(";"),
            )
        }
    }
