# Pendiente: instalador Ubuntu 23 (alpha)

Estado al momento de dejar esta tarea. **Nada de esto está commiteado todavía**: los cinco
archivos modificados viven solo en el working tree, y el tag `v1.0.0-alpha.2` publicado apunta al
commit `35996d4`, que no incluye nada de lo que se describe abajo.

## Objetivo

Dejar un `.deb` instalable y ejecutable en **Ubuntu 23.10 (mantic)** — la versión del usuario —
construido por el workflow de GitHub Actions, para que la prueba final se haga sobre el artefacto
publicado y no sobre un build local.

## Bugs encontrados y su estado

### 1. `Depends` no portable (arreglado, sin commitear)

**Síntoma:** `apt install` fallaba con `unmet dependencies` en Ubuntu 23.10, y con
`E: Unsupported file ... given on commandline` cuando el archivo no estaba en el directorio actual.

**Causa:** jpackage deriva `Depends` ejecutando `ldd` en la máquina que construye. En el runner
(Ubuntu 24.04) eso produce `libasound2t64` y `libpng16-16t64`, los nombres con el sufijo `t64` de la
transición a `time_t` de 64 bits. Esos paquetes **no existen** fuera de 24.04, así que el `.deb`
solo instalaba en la versión del runner.

**Arreglo:** lista explícita de dependencias con alternativas Debian (`pkgA | pkgB`), de modo que un
solo `.deb` sirve para varias versiones.

- `build.gradle.kts`: `defaultLinuxPackageDependencies`, sobreescribible con
  `-Pdenti.linux.deps="..."`.
- `build.gradle.kts`: tarea `pinDebDependencies`, que reescribe el campo `Depends` del `.deb` ya
  construido (`dpkg-deb -R` → editar `control` → `dpkg-deb --build --root-owner-group`).
- `scripts/build-installer.sh` invoca `packageDeb pinDebDependencies` y falla si algún `Depends`
  termina en `t64` sin alternativa.

**Nota:** no se puede pasar `--linux-package-dependencies` por el DSL de Compose, y su equivalente
en `freeArgs` no sirve: Compose arma la línea de jpackage con el modo primero (`--type` va en la
línea 35 del args file), así que lo que se antepone se interpreta como modo y jpackage responde
`Invalid Option`.

### 2. `postinst` rompía la instalación (arreglado, sin commitear)

**Síntoma:** la instalación dejaba el paquete en `iF` (medio configurado) con
`installed denti-code package post-installation script subprocess returned error status 3`.

**Causa:** el `postinst` de jpackage ejecuta `xdg-desktop-menu install` sin proteger. Ese comando
falla donde no hay directorio de menú del sistema escribible. Como dpkg corre los scripts con
`set -e`, la instalación entera aborta aunque todos los archivos se desempaquetaran bien.

**Arreglo:** `pinDebDependencies` añade `|| true` a esa línea. El registro en el menú es cosmético.

### 3. Falta `java.sql` en el runtime empaquetado (arreglado, sin commitear) — **el más grave**

**Síntoma:** con el `.deb` instalado, la app moría al arrancar con
`NoClassDefFoundError: java/sql/Connection` en `Database.connect` (Exposed/SQLite).

**Causa:** la imagen de runtime por defecto de Compose incluye solo `java.base`, `java.datatransfer`,
`java.xml`, `java.prefs`, `java.desktop`, `java.logging` y `jdk.crypto.ec`. **Sin `java.sql`.** La app
funciona bien desde Gradle porque ahí se usa el JDK completo del toolchain; el defecto solo aparece
en el `.deb`, o sea, justo donde el usuario lo prueba.

**Arreglo:** `afterEvaluate { tasks.withType<AbstractJLinkTask>().configureEach { modules.set(...) } }`
con `java.sql` y `jdk.unsupported` añadidos. Verificado: el `release` del runtime ahora lista
`java.sql` y `java.transaction.xa`.

**Trampa documentada:** `tasks.named("createRuntimeImage")` falla con *Task with name not found*
(el plugin registra la tarea de forma perezosa), y un `configureEach` normal se ejecuta **demasiado
temprano**: el plugin pisa la lista de módulos después. Solo funciona dentro de `afterEvaluate`.
`modules.set(listOf(...))` necesita la `listOf` explícita; con argumentos sueltos no compila.

### 4. `Depends` incompleto para las librerías de la JRE (arreglado, sin commitear)

**Síntoma:** `UnsatisfiedLinkError: libharfbuzz.so.0: cannot open shared object file` desde
`runtime/lib/libfontmanager.so`.

**Causa:** el mismo problema de alcance que el bug 1. El `ldd` de jpackage solo mira el launcher,
nunca las librerías de la JRE. `libfontmanager.so` enlaza contra `libharfbuzz`, `libstdc++` y
`libgcc_s` directamente. Una lista escrita a mano se desincroniza en cuanto cambia el JDK.

**Arreglo:** comprobación en `scripts/build-installer.sh` que extrae el `.deb`, calcula el cierre
real de `NEEDED` con `readelf` sobre cada `.so` del payload y falla si algo no está cubierto por
`Depends`, con un mapa `SONAME_PKG` explícito. Añadidos `libharfbuzz0b`, `libstdc++6`, `libgcc-s1`,
`libgif7`, `libjpeg-turbo8`, `liblcms2-2`. Reporta `19 linked libraries covered by Depends`.

## Verificaciones hechas (todas locales, con Docker)

Éxito, contra el `.deb` recién construido:

- Ubuntu 24.04, 23.10 y 22.04: `apt-get install -s` resuelve; en 24.04 elige las variantes `t64` y en
  22.04/23.10 las antiguas. El `.deb` viejo, en cambio, da `Depends: libpng16-16t64 but it is not
  installable` en ambas.
- Ubuntu 23.10: instalación real → `ii denti-code 1.0.0-1 amd64`, layout en `/opt` correcto.

Falló, y es lo que queda por confirmar:

- **Lanzamiento real de la app.** Con `java.sql` ya en el runtime, el test de arranque bajo Xvfb se
  canceló antes de dar resultado. **Este es el punto abierto principal**: que la app abra su ventana y
  cree `~/.denti-code-kt/denti-clinic.db` en Ubuntu 23.10.
- Arranque headless da `HeadlessException`, lo cual es normal y esperado (Compose/AWT necesita X11).

## Pasos pendientes

1. Confirmar el arranque bajo Xvfb en Ubuntu 23.10 (o directamente en la máquina del usuario, que es
   la prueba que importa).
2. Commitear los cinco archivos. Sugerencia de mensaje:
   `release: make the .deb installable and runnable outside the build host`
3. Publicar con un tag nuevo. **No borrar ni recrear `v1.0.0-alpha.1` ni `v1.0.0-alpha.2`:** borrar
   un tag elimina su release de forma asíncrona y fue la causa de un fallo difícil de diagnosticar
   (ver más abajo).
4. Verificar el asset publicado con `curl -sIL` hasta `HTTP 200` y `dpkg-deb -f` sobre el `.deb`
   descargado.
5. Que el usuario pruebe en su Ubuntu 23.10 con la ruta absoluta (ver más abajo).

## Pendiente de documentación menor

`docs/UBUNTU_INSTALLER.md` explica ya la ruta absoluta frente a `./`, pero sigue sin:

- Una nota de que el paquete está probado en 22.04 / 23.10 / 24.04.
- Mencionar la tarea `pinDebDependencies` y el flag `-Pdenti.linux.deps`.
- La advertencia de que `Depends` y por tanto el `.deb` no es reproducible bit a bit.

## Gotchas que costaron tiempo (no repetir)

- **No borrar tags ya publicados.** Borrar un tag dispara la eliminación asíncrona de su release. Con
  `v1.0.0-alpha.1` el paso de publicación decía `success` y la release no existía; el workflow ahora
  verifica con la API y falla ruidosamente (`35996d4`), pero la causa raíz sigue siendo del lado de
  GitHub. Solución práctica: tag nuevo.
- **`apt` solo acepta rutas relativas con prefijo `./` y desde el directorio del archivo.** Con una
  ruta absoluta funciona siempre. El error `E: Unsupported file ... given on commandline` casi
  siempre significa "el archivo no está donde creo que está", no un problema de apt.
- **Binds de Docker a rutas bajo `/tmp` a veces no montan** con este daemon. Usar rutas bajo `$HOME`.
- **`dpkg-deb --build` necesita `--root-owner-group`** si no se corre como root, o el
  `.deb` queda con el uid del usuario.
- **Lectura del error de jpackage:** el log real está en
  `build/compose/logs/<task>/jpackage-*-err.txt`; `tail -5` con glob falla en este shell, usar
  `cat ... | head`.

## Contexto

- Repo: `/home/larce/Documents/proj/gi/denti-code-kt`, remoto `git@github.com:kapit4n/denti-code-kt.git`.
- Último commit en `main`: `35996d4` (sí está pusheado).
- Release viva: `v1.0.0-alpha.2` → `denti-code_1.0.0_amd64.deb` (105.8 MiB). **Arrastra el bug 3, así
  que esa release no arranca.**
- Versión del Gradle: `1.0.0` numérico. El sufijo alpha vive solo en el tag; Dmg exige
  `MAJOR > 0` y MSI rechaza prerelease, así que la versión del proyecto no puede llevar sufijo.
- Versión Debian resultante: `1.0.0-1` (`appRelease = "1"` fijado para que no cambie entre máquinas).
- Datos de usuario en `~/.denti-code-kt/`, fuera del paquete, nunca borrados al instalar o actualizar.
- 4 tests de preservación de base de datos en `DatabasePreservationTest.kt`, pasan.
- Icono de producción ausente: se empaqueta el de Compose por defecto.
- `Depends` no es reproducible bit a bit: lo deriva el `ldd` del host. Cosmético, pero conviene
  documentarlo.
