# Backlog — Post-Milestone Improvements

Improvements / nice-to-haves que quedan **fuera del alcance de los 10 hitos** y deben implementarse **después de completar todos los milestones**. Cada ítem se prioriza cuando se termine la hoja de ruta principal.

| # | Área | Mejora | Origen |
|---|------|--------|--------|
| 1 | Reports / Export | **Exportación PDF real** — generar un `.pdf` vectorial vía `java.awt.print` en un stream PDF (sin librerías externas) como alternativa a imprimir desde el navegador; y/o **gráficos SVG embebidos** (self-contained) en el reporte HTML. | Milestone 5, TASK-005 (provisional TASK-006) |
| 2 | Auth / Seguridad | Cifrado de contraseñas real (bcrypt/Argon2) reemplazando el SHA-256 provisional de `registerUser`; login con sesión y RBAC aplicado a las pantallas. | Milestone 6, TASK-001 |
| 3 | Export | Soporte Excel real (`.xlsx`) además de CSV/HTML; importación de datos (pacientes, catálogo). | STATUS §4.B |
| 4 | Documentos | Exportación de historial de pagos y exportación de pacientes (hoy simulada). | STATUS §4.E |
| 5 | Impresión | Impresión directa a impresora de recibos y tarjetas de cita (sin pasar por navegador). | STATUS §4.I |
| 6 | Integridad | Copias de seguridad automáticas programadas y backups en la nube. | Milestone 7 / 10 |
