# Application icons

`build.gradle.kts` wires these files into the Compose Desktop native distributions.
**Every file here is optional**: while one is missing, the Compose plugin falls back to its own
default icon, so `./gradlew packageDeb` keeps working (and logs a warning naming the missing path).
Dropping the production asset in place is all that is required — there is no build flag to flip.

| File | Platform | Notes |
| --- | --- | --- |
| `denti-code.png` | Linux (`.deb`) | **Required for a public release.** PNG with alpha, square, 256x256 or 512x512. jpackage requires `.png`/`.svg` for `--linux`. |
| `denti-code.ico` | Windows (`.msi`) | Multi-resolution `.ico` (16/32/48/256). |
| `denti-code.icns` | macOS (`.dmg`) | Standard macOS icon set. |

## Verification

```bash
./gradlew packageDeb
```

The build prints one of:

```
[denti-code] Application icon: /.../src/main/resources/icons/denti-code.png
[denti-code] No application icon at /.../src/main/resources/icons/denti-code.png: the Compose default icon will be packaged.
```

Then confirm what actually ended up inside the package:

```bash
dpkg-deb -c build/compose/binaries/main/deb/*.deb | grep -E '\.png|\.ico'
dpkg-deb -e build/compose/binaries/main/deb/*.deb /tmp/denti-deb-control
cat /tmp/denti-deb-control/control   # check Name / Version / Maintainer / Section
```

On Linux the installed icon shows up at `/opt/denti-code/lib/*.png`, referenced by the generated
`.desktop` entry that `postinst` registers through `xdg-desktop-menu`.
