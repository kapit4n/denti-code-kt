# Instalador de Ubuntu (.deb)

Denti-Code se distribuye en Ubuntu como un paquete Debian nativo. No hay que instalar
Java por separado: el paquete incluye su propio runtime.

- **Nombre del paquete:** `denti-code`
- **Aplicación instalada en:** `/opt/denti-code`
- **Lanzador:** `/opt/denti-code/bin/Denti-Code` (también aparece en el menú de aplicaciones)
- **Datos del usuario:** `~/.denti-code-kt/`

## Instalar una versión publicada

1. Descarga el `.deb` desde la sección *Releases* del repositorio.
2. Instálalo con `apt`:

   ```bash
   sudo apt install ./denti-code_1.0.0_amd64.deb
   ```

   Sustituye el nombre de archivo por el de la versión que descargaste.

Instalar o actualizar **nunca** borra la base de datos de la clínica: el instalador solo
reemplaza archivos de la aplicación dentro de `/opt/denti-code`.

## Desinstalar

```bash
sudo apt remove denti-code          # conserva los datos
sudo apt remove --purge denti-code  # conserva los datos
```

`apt` no conoce `~/.denti-code-kt/`, así que la base de datos sobrevive a la desinstalación.
Para borrarla explícitamente:

```bash
sudo apt remove --purge denti-code
rm -rf ~/.denti-code-kt
```

Haz una copia de seguridad antes de ejecutar ese `rm -rf`.

## Compilar el instalador desde el código

Requiere Linux y un JDK (el proyecto usa toolchain 17; el Gradle wrapper la descarga sola).

```bash
./scripts/build-installer.sh
```

El script ejecuta las pruebas, genera el paquete y lo verifica (nombre de paquete, versión,
metadatos Debian, instalación bajo `/opt` y ausencia de archivos de base de datos).
Publica una copia con nombre estable en `build/release/`, que es la ruta que usa la
automatización de publicación:

```
build/release/denti-code_<VERSION>_amd64.deb
```

El archivo original de `jpackage` queda en `build/compose/binaries/main/deb/`, pero su
nombre puede llevar un sufijo de revisión Debian según la máquina (ver
[ Versión Debian](#versión-debian-100-1)).

La versión viene de `version` en `build.gradle.kts`; es la única fuente de verdad.
Para instalarlo en tu máquina:

```bash
./scripts/install-local.sh
```

## Publicar una versión

El flujo de publicación se dispara al subir un tag y está definido en
[`.github/workflows/release.yml`](../.github/workflows/release.yml).

```bash
# 1. Actualiza 'version' en build.gradle.kts y haz commit
# 2. Crea el tag y publícalo
git tag v1.0.1
git push origin v1.0.1
```

El workflow valida el formato del tag y que su versión coincida con `build.gradle.kts`;
si no coincide, la ejecución falla sin publicar nada. Después compila, verifica el `.deb`
y lo adjunta a una GitHub Release.

### Tags con sufijo alfa

El tag admite un sufijo (`v1.0.0-alpha.1`), pero el `.deb` se genera siempre con la versión
numérica (`denti-code_1.0.0_amd64.deb`). Motivo: la versión de `packageVersion` es
compartida por los tres objetivos y Windows (MSI) exige `MAJOR.MINOR.BUILD` puramente
numérico, así que un sufijo en ella rompe la compilación en Windows.

**Consecuencia importante:** el sufijo no llega al paquete. Dos alfas del mismo número
(`v1.0.0-alpha.1` y `v1.0.0-alpha.2`) generan el mismo `.deb` con la misma versión Debian.
Para evitar que `apt` los confunda, sube la versión en `build.gradle.kts` entre-publicaciones
y reserva números distintos:

| Tag                  | `version` en build.gradle.kts | `.deb` resultante            |
| -------------------- | ----------------------------- | ---------------------------- |
| `v1.0.0-alpha.1`     | `1.0.0`                       | `denti-code_1.0.0_amd64.deb` |
| `v1.0.0`             | `1.0.1`                       | `denti-code_1.0.1_amd64.deb` |
| `v1.1.0-alpha.1`     | `1.1.0`                       | `denti-code_1.1.0_amd64.deb` |
### Restricción del número de versión

La versión debe empezar por un entero **mayor que 0**: el objetivo macOS (`Dmg`) rechaza
`MAJOR = 0`, así que no se pueden usar versiones `0.x.y` mientras `Dmg` esté en
`targetFormats`.


## Metadatos del paquete

`build.gradle.kts` define el `vendor` como `Denti-Code` y el campo `Maintainer` de Debian
como `Denti-Code <maintainer@denti-code.local>`. Para publicar con una dirección de contacto
real, pásala por propiedad de Gradle:

```bash
./gradlew packageDeb -Pdenti.deb.maintainer=contacto@tu-dominio.com
```

### Versión Debian: `1.0.0-1`

Una versión Debian es `upstream_version-revision`. Con `appRelease = "1"` fijado en
`build.gradle.kts`, el `.deb` siempre lleva `Version: 1.0.0-1`.

Ese valor está fijado a propósito. Algunas versiones de `jpackage` (por ejemplo la del OpenJDK
de Ubuntu) poner `app-release` en `1` por defecto y otras lo dejan vacío, así que sin fijarlo
el artefacto se renombraba según la máquina que compilara: `denti-code_1.0.0_amd64.deb` en un
entorno y `denti-code_1.0.0-1_amd64.deb` en otro.

Por eso los scripts nunca interpretan el nombre del archivo: leen la versión con
`dpkg-deb -f ... Version` y comparan solo la parte *upstream* (lo anterior al guion). El
nombre público del instalador lo decide `build-installer.sh` y es siempre
`denti-code_<VERSION>_amd64.deb`.

### Dependencias no reproducibles entre máquinas

`jpackage` calcula el campo `Depends` ejecutando `ldd` sobre los binarios, así que la lista
depende de las bibliotecas instaladas en la máquina que compila. Dos equipos pueden generar
`.deb` con listas distintas. La Release de GitHub se construye siempre en el runner de Ubuntu,
así que el artefacto publicado es siempre el de ese entorno.

## Icono de la aplicación

El icono se toma de `src/main/resources/icons/denti-code.png` si existe. Mientras tanto el
plugin de Compose empaqueta su icono por defecto y avisa durante la compilación. Ver
[`src/main/resources/icons/README.md`](../src/main/resources/icons/README.md).
