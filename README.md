# Denti-Code · Clínica (desktop)

Gestión de clínica dental.

![Main view](mockup/home.png)

## Instalación en Ubuntu

Denti-Code se distribuye como paquete `.deb` nativo (incluye su propio runtime de Java).

```bash
sudo apt install /ruta/al/archivo/denti-code_1.0.0_amd64.deb
```

Usa la ruta completa: `apt` sólo acepta rutas relativas con prefijo `./` y desde el directorio
donde está el archivo (si no, responde `E: Unsupported file ... given on commandline`).

Consulta la [guía del instalador de Ubuntu](docs/UBUNTU_INSTALLER.md) para compilarlo desde
el código, publicarlo o desinstalarlo. Los datos de la clínica viven en `~/.denti-code-kt/`
y nunca se modifican al instalar o actualizar.
