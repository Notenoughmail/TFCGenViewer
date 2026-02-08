# 2.0.1

- Add dispatch gradient type
  - Only one dispatch type, `tfcgenviewer:hue_wheel`, currently exists
- The *Temperature* visualizer type now has tooltips in increments of 5°C, instead of 12°C
- The *Rainfall* VT now has tooltips in increments of 50mm, instead of 100mm
- The *Biomes*, *Rocks*, and *Climate Restricted Features* VTs now have dynamic color keys, only showing values encountered during generation
- VT specific options now have a teal border
- Tweaked the *Rocks* VT options
  - Changed 'at surface' to 'mode'; functionality is unchanged, but the wording should be more clear/descriptive
  - The 'elevation' option is now disabled if 'mode' is set to 'at surface'
- Add tooltips to all current VT options
- The configs now have lang entries
- [API]
  - VT specific options can now be dynamically disabled by passing a `BooleanSupplier` to `OptionProvider$Order#finish`
  - Add dispatch gradient registry, used by the above-mentioned dispatch gradient type
  - Add `RockCache`, a cache which
    - Wraps an inner cache, like `ClimateFeatureCache`
    - Keeps track of the colors in the rock color manager which have been encountered
    - Provides a color key describing the encountered colors
  - Add `ChunkSize` and `ChunkScale` for general use
- A new [demo video](https://youtu.be/RUc4P2T-jIs) for this update/2.0.0 in general

# 2.0.0

- Update to 1.21.1!
- Complete rework to the underlying functionality; TFCGenViewer now has an API! Expect more on it in the docs at some point
- Several reworks to certain visualizer types
  - By default, The *Biomes* VT now shows a lot more biomes to match TFC's expanded biome palette
  - The *Inland Height* VT has been merged with *Biome Altitude*, which now shows 7 discrete altitudes from very deep ocean to mountain elevation
  - The *Rivers and Mountains* VT now has a flat land color, shows hot spot ages, and has a 'sensitivity' option for river positions
  - The *Rocks* VT now has two options
    - 'At surface': If the preview should be created for the surface rock layer
    - 'Elevation': The y-level the preview should be created for if 'at surface' is turned off
- Add the *Köppen Climate Classification* visualizer type, which colors land according to its [Köppen climate classification](https://en.wikipedia.org/wiki/K%C3%B6ppen_climate_classification)
- The mod is now properly server optional! Players can join servers without TFCGV without any issues, though naturally they will not be able to use the visualizer features
- The permissions system has received an overhaul allowing for more fine-grained control of visualizer permissions
- For the moment, the rock editor sub-screen is unimplemented
- The seed button now also puts the seed into the seed text box when clicked

**Note**: Currently the wiki is pretty barren and only has pages on using the new permission system and the json types used to define colors. More will be added as I have the time

# 1.5.1

- Add *Climate Restricted Features* visualizer
  - By default shows Kaolin Clay and Coral spawn locations
  - Will display anything in the `tfcgenvewier:visualizable_features` placed feature tag
    - Can handle multiple, overlapping features gracefully
    - Uses the climate range set in the `tfc:climate` modifier of the feature
    - If the configured feature is a TFC ore-type, its biome tag will be respected
    - All entries also require a matching color definition in the `/tfcgenviewer/features/` folder
- Improve *Rivers and Mountains* and *Rocks* visualizer speeds, most noticeable with large visualization scales

# 1.5.0

- Add rock editor sub-screen
  - Can be opened via the "Edit Rocks" button below "Export Preview" in the preview screen
  - See the [wiki page](https://notenoughmail.github.io/tfcgv/1.20.1/rock-editor) for more information about usage
- Color gradients are now interpolated over the linear sRGB color space
- Changed the default gradients for the *Rainfall* and *Temperature* previewers to be more vibrant and distinct
  - As part of this, the `temp`, `temperature`, `rain`, and `rainfall` reference gradients are now separate from the `climate` gradient, which is now unused by default
- Adjust *Temperature* visualizer range to be -23 to 33 °C

# 1.4.1

- Add toggleable tooltip when hovering over the preview
- `Color`(`Gradient`)`Definition`s now have the ability to define a key for the tooltip (multiple in the case of gradients)
- *Biomes* and *Rocks* `ColorDefinition`s can now be disabled, preventing them from being added to the color key
- Fix in-world previews showing the coords the preview is centered on when the server has disabled coordinate viewing
- Adjust *Temperature* visualizer range to be -20 to 30 °C to better reflect the temperature ranges available in default TFC

# 1.4.0

- Speed up preview generation by caching a region's `Point`s
- Properly deal with errors that may occur during generation
- Add error handling config: can be set to immediately stop preview creation and tell the user or to simply skip the `Point` causing an issue and fill it in with safe, though not accurate, data
- Add progress bar along the bottom of the preview, can be disabled in the config
- Colors and gradients can now be randomized at resource load, see the wiki for how to do so via a resource pack
  - Add an in-built resource pack that randomizes everything
- Convert most color handlers to (`Registered`)`DataManagers`
  - Externally this means little more than most color (gradient) definition files are in different places than before, the new locations will be on the wiki
- Add new grayscale reference gradient

# 1.3.0

- The preview screen can now be opened in-world!
  - Also works on servers
  - Can be opened with the `K` key by default
  - Only has options to change the scale since the world settings are already defined
  - Which information a player is able to access can be disable/enabled from the server, see below
- Preview generation is now done off-thread, meaning the game should no longer completely lock up on previews that take a long time
  - While generating, a loading icon will be displayed instead of the previous preview, this can be disabled in the client config
  - When generation finishes, the game will ding, this can also be disabled in the client config
- Add a permissions system to limit what players can access via the in-world preview
  - See the [wiki page](https://notenoughmail.github.io/tfcgv/1.20.1/permissions) for an explanation

# 1.2.1

- Make spawn overlay colors customizable
- Allow preview size to be changed at preview time
  - Has 7 options: 4.1, 8.2, 16.4, 32.8, 65.5, 131.1, and 262.1 km
  - Change config option `previewSize` to `defaultPreviewSize`, determines which size the option will default to
- Add *Export Preview* button
  - Exports the currently displayed preview to a file in `screenshots/tfcgenviewer`
- Add in-world coordinates tooltip when hovering over the preview
  - Can be toggled on and off by clicking the preview
- Tweak the offset options to be in kilometers instead of chunks and to be based off of the center of the preview instead of the top and left edges

# 1.2.0

- Make visualizer colors resource packable
  - See [the wiki page](https://notenoughmail.github.io/tfcgv/1.20.1/customization/) for more information
- Adjust *Rocks* visualizer to show y = ~90 instead of ~0
- Add in-built resource pack that makes the *Rocks* visualizer use the colors of [TFCSeedMaker](https://github.com/dries007/TFCSeedMaker/tree/master)

# 1.1.0

- Overhaul the preview screen
  - The preview is now centered on the screen
  - The settings on the left of the screen are now in a single column and conform to the available space
  - The *Save* and *Cancel* buttons are now to either side of the seed button
  - The right info area now has more info
    - The visualizer used for the preview currently shown
    - Where the current preview is centered on
    - A color key for the visualizer
    - A compass pointing North
  - The right info are now becomes scrollable if the info contained is too tall for the screen
- Rename *Rocks* visualizer to *Rock Types*
- Add *Rocks* visualizer that displays the actual rock that will generate at that point

# 1.0.0

- Initial release!
