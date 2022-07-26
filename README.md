# Factions Money Mod

## About

This mod gives a purpose to excess valuables in your minecraft worlds. By using the preexisting [Factions Mod](https://github.com/ickerio/factions) this lightweight mod can give a faction more power based on the number of resources stored in their claims.

## How does it work?

When the server starts this mod checks every claim and every chest or barrel in that claim for valuables. Each type of item is assigned a value, for example, diamonds are worth five. Then when everything is counted it has its "money" value. It does one final calculation to turn that money value into a power value that is then added.

## Configuration

### `items`

This is a list of item ids (e.g. `minecraft:diamond`) and their corresponding money value

**Example**:
```json
{
    "minecraft:diamond": 5,
    "minecraft:emerald": 3
}
```

### `multiplier`

This is how much the money is multiplied by before it is added to the power

**Default**: `0.01`

### `useMax`

If this is set to true, the max power will be changed. If not the current power will be changed

**Default**: `false`

### `ticksToReload`

The number of ticks before it recounts each faction's money

**Default**: `4800` (4mins)

### Default Config

```json
{
  "items": {
    "minecraft:diamond_block": 45,
    "minecraft:gold": 3,
    "minecraft:emerald": 3,
    "minecraft:emerald_block": 27,
    "minecraft:iron_block": 18,
    "minecraft:diamond": 5,
    "minecraft:iron": 2,
    "minecraft:gold_block": 27
  },
  "multiplier": 0.01,
  "useMax": false,
  "ticksToReload": 4800
}
```