# Mob teams

Quietus loads mob teams from data packs at `data/<namespace>/mob_teams/*.json`.
Mobs which share at least one team are treated as allies: they cannot target one
another and damage from one to the other is cancelled.

Each file defines one team. Members may be entity IDs or entity-type tags:

```json
{
  "members": [
    "minecraft:zombie",
    "minecraft:husk",
    "#examplemod:my_undead_mobs"
  ]
}
```

The file name is only the team's identifier, so teams can be named and organized
freely (for example `mob_teams/undead.json`). An entity may appear in more than
one team. Entity tags are the easiest extension point for other mods: add values
to the relevant file under `tags/entity_type`, then run `/reload`.

Quietus includes `quietus:undead` and `quietus:ender_creations` entity tags and
uses them in its two default team files. A higher-priority data pack can replace
a team file, while entity-tag files use normal data-pack tag merging rules.

Players are intentionally excluded even if `minecraft:player` is listed.
