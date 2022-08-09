Litematica Printer
==================
This fork adds printing functionality for [Litematica fabric](https://github.com/maruohon/litematica) 1.19, 1.18 and 1.17 versions. Printer allows players to build
big structures more quickly by automatically placing the correct blocks around you.

The main branch (printing) is dedicated to latest version of Minecraft, while printing_1.17 and printing_1.16 are
for the older versions respectively. If you have issues with the printer, **do not** bother the original creator of
Litematica (maruohon) with them. Contact me instead. Feature requests or bugs can be reported via github issues.

For downloads check out [releases](https://github.com/aleksilassila/litematica-printing/releases/latest).
To install the mod, first download the original Litematica and MaLiLib from [here](https://www.curseforge.com/minecraft/mc-mods/litematica).
You will also need [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/).
Finally, move the printer's .jar from [releases](https://github.com/aleksilassila/litematica-printing/releases/latest) to your mods folder.

![Demo](printer_demo.gif)

How To Use
----------
Using the printer is straightforward: You can toggle the feature by pressing `CAPS_LOCK` by default. To configure variables such as
printing speed and range, open Litematica's settings by pressing `M + C` and navigate to "Generic" tab. Printer's configuration can be
found at the bottom of the page. You can also rebind the printing toggle under "Hotkeys" tab. Holding down `V` by default will also
print regardless if the printer is toggled on or off.

### List of blacklisted blocks
These blocks have not been implemented yet for various reasons and the printer will skip them instead of placing them wrong. If any
other blocks are placed incorrectly, try to lower the printing speed. If certain block is still placed incorrectly, you can create
[an issue](https://github.com/aleksilassila/litematica-printer/issues).
 - Grindstones
 - Skulls placed on the ground
 - Signs
 - Glow lichen and vines
 - Entities, including item frames and armor stands

### Buildshelf.net

[Buildshelf.net](https://buildshelf.net) is a project of mine that aims to help people share and discover build designs in form of litematics.
Combined with litematica-printer, buildshelf.net is a powerful tool that allows you to build in survival faster than ever. All contributions are much appreciated!
