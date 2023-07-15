## What does this mod add?
This mod introduces infinite ore veins that gradually generate ore in their vicinity, whether it's a few blocks above or tens of blocks underground. These veins are scattered around the overworld's surface in variety of structures like trees or monuments (check the gallery for some examples of them).<br>
The intention of this mod is to encourage players to set up transportation logistics throughout the world such as trains or ice boat tunnels.

#### In total this mod adds 9 infinite veins:
- 3 small veins
- 3 common veins
- 3 rare veins

## Is this mod incompatible with any other mods?
While this mod is likely compatible with other mods, there are a few that may impact your experience: <br>
- Biome generation mods can infinite veins from this mod rarer.
- Mods like Waystones mod can make transportation too easy, destroying the intention of this mod.

## How to add a new ore vein?
To add a new ore vein follow this steps:
- Create a datapack (and obtain some knowledge on how to make them). <br>
- Within the datapack, create a new structure file in `data/<your datapack's name>/structure`.<br>
- Inside of that file you have to specify that your structure's type is `railworld:vein_structure` like
```json5
{
  "type": "railworld:vein_structure",
  // [ ... ]
}
```
- Define the vanilla spawning parameters for the structure.
```json5
{
  // [ ... ]
  "biomes": "#railworld:has_vein/iron_coal", // replace this tag with whatever tag represents the biomes your vein spawns in like [#minecraft:is_forest]
  "spawn_overrides": {},
  "step": "surface_structures",
  // [ ... ]
}
```
- Specify the vein's properties:
```json5
{
  // [ ... ]
  "ores": [ // blocks spawned by the ore vein
    {
      "probability": 0.4, // probabilities should in total add up to 1.0
      "id": "minecraft:diamond_ore" // id of the block you want to generate
    },
    {
      "probability": 0.6,
      "id": "minecraft:gold_ore"
    }
  ],
  "x": 0, // x offset from the vein
  "y": -35, // y offset from the vein 
  "z": 0, // z offset from the vein 

  "size": 12, // minimum size of the vein (sometimes the size can end up being smaller due to blocks stacking)
  "ySize": 7, // how high can the vain can go up?
  "extraSize": 6, // size + extraSize is the upper bound of the how large a vein can be
  "perOnce": 1, // how many blocks to place per placing
  "ticksPerPlace": 120, // how many ticks between the vein placing blocks

  "replaces": "#railworld:replacing/stone", // tag of the blocks the vein replaces

  "decor": [
    "railworld:gold_diamond/tree" // list of nbt structures the vein uses
  ]
}
```
When creating the nbt structure for the vein, ensure that you place structure blocks in the data mode with `vein` written as metadata at the locations where you want to create ore veins. <br>

Finally, you can add the structure into a structure set to make it spawn around the world.


