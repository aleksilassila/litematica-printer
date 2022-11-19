Litematica Printer
==================
This fork adds printing functionality for [Litematica fabric](https://github.com/maruohon/litematica) 1.19, 1.18 and 1.17 versions. Printer allows players to build
big structures more quickly by automatically placing the correct blocks around you.

![Demo](printer_demo.gif)

## Installation

 1. Download and install [Fabric](https://fabricmc.net/use/installer/) if you haven't already.
 2. Download the latest release for your Minecraft version from the
[releases page](https://github.com/aleksilassila/litematica-printing/releases/latest).
 3. Download [Litematica + MaLiLib](https://www.curseforge.com/minecraft/mc-mods/litematica) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/).
 4. Place the downloaded .jar files in your `mods/` folder.

## How To Use

Using the printer is straightforward: You can toggle the feature by pressing `CAPS_LOCK` by default. To configure variables such as
printing speed and range, open Litematica's settings by pressing `M + C` and navigate to "Generic" tab. Printer's configuration can be
found at the bottom of the page. You can also rebind the printing toggle under "Hotkeys" tab. Holding down `V` by default will also
print regardless if the printer is toggled on or off.

## Issues

If you have issues with the printer, **do not** bother the original creator of
Litematica (maruohon) with them. Contact me instead. Feature requests or bugs can
be reported via [GitHub issues](https://github.com/aleksilassila/litematica-printer/issues),
or in [Discord](https://discord.gg/enypPQh6pz).

Before creating an issue, make sure you are using the latest version of the mod.
To make fixing bugs easier, include the following information in your issue:
 - Minecraft version
 - Litematica version
 - Litematica Printer version
 - Detailed description of how to reproduce the issue
 - If you can, any additional information, such as error logs, screenshots or **incorrectly printed schematics**.

### List of blacklisted blocks
These blocks have not been implemented yet for various reasons and the printer will skip them instead of placing them wrong. If any
other blocks are placed incorrectly, try to lower the printing speed. If certain block is still placed incorrectly, you can create
[an issue](https://github.com/aleksilassila/litematica-printer/issues).
 - Grindstones
 - Skulls placed on the ground
 - Signs
 - Glow lichen and vines
 - Entities, including item frames and armor stands

## Building and Contributing

The code for the project can be found inside `implementations` gradle submodule.
Each Minecraft version has its own submodule, that has the default fabric mod development tasks
and contains the version-specific code. To reduce the amount of work I have to do to make
it work for multiple Minecraft versions, I created this hacky gradle script that copies the
common code over to the other version implementations. Currently, the script copies everything,
except `interfaces/` and `mixin/` folders, which should therefore be the only places containing any
version specific code.

If you want to make changes to the mod, I would recommend you to first implement them for
the latest Minecraft version, and then running the `syncImplementations` gradle task for that
version to copy the common code to the other implementations. After that you will only have to write
the version-specific code for the other versions and do some testing to ensure everything works.

Contributions are welcome and appreciated! Also, if you know a better way to develop for multiple
Minecraft versions that doesn't involve multiple git branches, please let me know.

Useful gradle tasks:

- `implementation:[v1_19/v1_18/v1_17]:syncImplementations`
  - Copy over common code to other implementations
- `buildAll`
  - Build all implementations and copy their jars to `build/` directory.
- `implementation:[v1_19/v1_18/v1_17]:runClient`
  - Start the target Minecraft version
