# Skill Tree Data Pack Format

Skill trees, in categories and their nodes, as well as their displayed names, descriptions and icons, are configured using data files.

Quietus will always read every datapack for valid skill trees, those which are loaded into the world, as the skill trees for every player.

---

## Directory Structure

Skill tree data files are located in `data/<namespace>/quietus/skill_tree/`.

Each subfolder under `quietus/skill_tree/` represents a **Skill Category (Tab)** and contains:
- `_tab_.json`: Metadata for the skill category tab (`SkillCategory`).
- `<node_name>.json`: Data for an individual skill point/node (`SkillPoint`).

```
data/
└── <namespace>/
    └── quietus/
        └── skill_tree/
            └── <category_path>/
                ├── _tab_.json
                ├── <node_name_1>.json
                └── <node_name_2>.json
```

### Identifier Resolutions
- **Category ID**: Formed by the category folder path relative to `quietus/skill_tree/`, i.e. the name of the folder is taken as path. 
  *Example:* `data/myskilltree/quietus/skill_tree/combat/_tab_.json` &rarr; `myskilltree:combat`
- **Node ID**: Formed by the category folder path and the node file name (without `.json`).  
  *Example:* `data/quietus/quietus/skill_tree/combat/heavy_strike.json` &rarr; `quietus:combat/heavy_strike`

---

## Tab Metadata (`SkillCategory`)

File location: `data/<namespace>/quietus/skill_tree/<category_path>/_tab_.json`  
Resource Location: `<namespace>:<category_path>`

Defines tab-level properties such as display information, layout algorithm settings, and category prerequisites.

### Data Structure

```
ROOT
├── max_nodes_per_layer <- Int (optional)
├── seed <- Long (optional)
├── prerequisites <- Compound (optional)
│   ├── advancements <- Compound (optional)
│   │   └── <criterion_key> <- String (Resource Location)
│   ├── parents <- Compound (optional)
│   │   └── <criterion_key> <- String (Resource Location)
│   └── requirements <- List of Lists of Strings (optional)
│       └── TAG_List of TAG_Strings
│           └── TAG_String
└── tab_display <- Compound (optional)
    ├── icon <- String (Resource Location) (optional)
    ├── name <- Compound or String (JSON Text Component)
    ├── description <- Compound or String (JSON Text Component)
    ├── prerequisites <- Compound
    │   └── advancements <- Compound (optional)
    │       └── <criterion_key> <- Compound or String (JSON Text Component)
    ├── themeColour: Color (TAG_Int / TAG_String / TAG_List of TAG_Floats)
    └── background <- String (Resource Location) (optional)
```

### Fields

- **`max_nodes_per_layer`**: (TAG_Int) *(Optional, default: `16`)* The maximum number of nodes allowed per layer in automatic node positioning calculations.
- **`seed`**: (TAG_Long) *(Optional, default: `20260210`)* Random seed used by layout positioning algorithms. In particular, it dictates the order of which the skill tree nodes are chosen to be assigned layers and the order of the skill tree nodes within the same layer.
- **`prerequisites`**: (TAG_Compound) *(Optional)* Prerequisites required to view and unlock the entire category tab. If not specified, defaults to always unlocked and visible.
  - **`advancements`**: (TAG_Compound) *(Optional)* Maps arbitrary criterion string keys to Advancement Resource Locations (e.g., `"minecraft:story/mine_stone"`).
  - **`parents`**: (TAG_Compound) *(Optional)* Maps arbitrary criterion string keys to parent Skill Category Resource Locations.
  - **`requirements`**: (TAG_List of TAG_Lists of TAG_Strings) *(Optional)* Boolean requirement matrix in Conjunctive Normal Form (CNF). If omitted, defaults to requiring all listed criteria (`allOf`).
- **`tab_display`**: (TAG_Compound) *(Optional)* Visual display properties for the tab in the skill tree UI.
  - **`icon`**: (TAG_String) *(Optional)* Resource location pointing to the icon texture (e.g., `"quietus:textures/gui/icons/skill_tree/tab/example.png"`).
  - **`name`**: (TAG_Compound or TAG_String) JSON Text Component for the category tab title.
  - **`description`**: (TAG_Compound or TAG_String) JSON Text Component for the category tab description.
  - **`prerequisites`**: (TAG_Compound) Display metadata for prerequisite criteria.
    - **`advancements`**: (TAG_Compound) *(Optional)* Maps prerequisite criterion keys to human-readable JSON Text Components shown in UI tooltips.
  - **`themeColour`**: (Color: TAG_Int / TAG_String / TAG_List of TAG_Floats) RGB color for the theme of the tab. Colours various parts of the skill tree GUI using the given colour, when this tab is selected. Accepts a hex string (e.g., `"#FF5555"`), an integer color value (e.g., `16733525`), or an RGB float array (e.g., `[1.0, 0.33, 0.33]`).
  - **`background`**: (TAG_String) *(Optional)* Resource location pointing to a 32x32 background tile texture (e.g., `"quietus:textures/gui/skill_tree/backgrounds/occult.png"`). If omitted, renders an opaque dark background.

---

## Skill Node Data (`SkillPoint`)

File location: `data/<namespace>/quietus/skill_tree/<category_path>/<node_name>.json`  
Resource Location: `<namespace>:<category_path>/<node_name>`

Defines an individual skill point node within a skill category.

### Tree Structure

```
ROOT
├── max_amount <- Int (optional)
├── progress <- Int (optional)
├── layout <- Compound
│   ├── top <- Byte / TAG_Boolean (optional)
│   └── prerequisites <- Compound (optional)
│       ├── advancements <- Compound (optional)
│       │   └── <criterion_key> <- String (Resource Location)
│       ├── parents <- Compound (optional)
│       │   └── <criterion_key> <- String (Resource Location)
│       └── requirements <- List of Lists of Strings (optional)
│           └── TAG_List of TAG_Strings
│               └── TAG_String
├── unlock <- Compound (optional)
│   └── prerequisites <- Compound (optional)
│       ├── advancements <- Compound (optional)
│       │   └── <criterion_key> <- String (Resource Location)
│       ├── parents <- Compound (optional)
│       │   └── <criterion_key> <- String (Resource Location)
│       └── requirements <- List of Lists of Strings (optional)
│           └── TAG_List of TAG_Strings
│               └── TAG_String
├── rewards <- Compound (optional)
│   ├── skills <- List of Compounds (optional)
│   │   └── TAG_Compound
│   │       ├── skill <- String (Resource Location)
│   │       ├── amount <- Int (optional)
│   │       └── source <- String (optional)
│   └── function <- String (Resource Location) (optional)
└── display <- Compound (optional)
    ├── type <- String (optional)
    ├── icon <- String (Resource Location) (optional)
    ├── header <- Compound or String (JSON Text Component)
    ├── description <- Compound or String (JSON Text Component)
    └── prerequisites <- Compound
        └── advancements <- Compound (optional)
            └── <criterion_key> <- Compound or String (JSON Text Component)
```

### Fields

- **`max_amount`**: (TAG_Int) *(Optional, default: `1`)* Maximum number of times this skill node can be upgraded/unlocked by a player.
- **`progress`**: (TAG_Int) *(Optional, default: `1`)* Required progress/points to unlock or level up this skill node.
- **`layout`**: (TAG_Compound) Layout graph structure configuration.
  - **`top`**: (TAG_Byte / TAG_Boolean) *(Optional, default: `false`)* If `true`, this node is treated as a top root node in tree layout positioning algorithms.
  - **`prerequisites`**: (TAG_Compound) *(Optional)* Prerequisites for when to display this node in player's GUI. If not specified, this node is always visible. \*
    - **`advancements`**: (TAG_Compound) *(Optional)* Maps arbitrary criterion string keys to Advancement Resource Locations.
    - **`parents`**: (TAG_Compound) *(Optional)* Maps arbitrary criterion string keys to parent Skill Node Resource Locations.
    - **`requirements`**: (TAG_List of TAG_Lists of TAG_Strings) *(Optional)* Boolean requirement matrix in Conjunctive Normal Form (CNF).
- **`unlock`**: (TAG_Compound) *(Optional)* Unlock requirements.
  - **`prerequisites`**: (TAG_Compound) *(Optional)* Requirements that must be fulfilled before the player can unlock or upgrade this skill node.
    - **`advancements`**: (TAG_Compound) *(Optional)* Maps arbitrary criterion string keys to Advancement Resource Locations.
    - **`parents`**: (TAG_Compound) *(Optional)* Maps arbitrary criterion string keys to parent Skill Node Resource Locations.
    - **`requirements`**: (TAG_List of TAG_Lists of TAG_Strings) *(Optional)* Boolean requirement matrix in Conjunctive Normal Form (CNF).
- **`rewards`**: (TAG_Compound) *(Optional)* Rewards granted to the player upon unlocking or upgrading this node.
  - **`skills`**: (TAG_List of TAG_Compounds) *(Optional)* List of skill rewards granted to the player upon unlocking or upgrading this node.
    - **`skill`**: (TAG_String) Resource Location of the target skill to award (e.g., `"quietus:strength"`).
    - **`amount`**: (TAG_Int) *(Optional, default: `0`)* Amount of skill levels or experience points granted per upgrade.
    - **`source`**: (TAG_String) *(Optional, default: `"none"`)* Identifier/source attribution tag for awarding the skill.
  - **`function`**: (TAG_String) *(Optional)* Resource Location of a datapack function executed when unlocking or upgrading this node (e.g., `"namespace:function_name"`).
- **`display`**: (TAG_Compound) *(Optional)* Graphical display configuration for the skill widget on the skill tree screen.
  - **`type`**: (TAG_String) *(Optional, default: `"square_node"`)* Shape variant of the node icon frame (e.g., `"square_node"`).
  - **`icon`**: (TAG_String) *(Optional)* Resource location pointing to the icon texture.
  - **`header`**: (TAG_Compound or TAG_String) JSON Text Component for the skill node title.
  - **`description`**: (TAG_Compound or TAG_String) JSON Text Component for the skill node description.
  - **`prerequisites`**: (TAG_Compound) Display metadata for prerequisite criteria.
    - **`advancements`**: (TAG_Compound) *(Optional)* JSON Text Component instructions for completing the corresponding advancement listed as criterion in `unlock.prerequisites.advancements`. If not specified, an empty line (blank description text beside the status symbol) is rendered for that criterion in the GUI. \*\*

\* Whether the node is displayed is achieved through whether the node will be sent amongst other nodes from the server to a client for display and player interactions in their skill tree GUI. When these prerequisites are not met for a client, the children of this node will not be sent to the said client as well. 
\*\* While other prerequisite criteria, i.e. parent skill tree nodes, are displayed in the info screen of this node by directly taking their display names, advancements as criteria however are displayed taking the text specified in this field, word by word. For an example, adding the vanilla advancement `minecraft:adventure/kill_a_mob` (Monster Hunter) as a criterion of a node, and write `"Defeat a monster"` in this field with the same key, will result as the player's GUI rendering `"Defeat a monster"` in place where the criterion would be, and the checkbox at the start of the line indicating whether this player completed `minecraft:adventure/kill_a_mob`.

---

## Common Structures

### Prerequisites Compound (`Prerequisites`)

Prerequisite definitions are used in tab categories (`SkillCategory`), node layout requirements (`LayoutInfo`), and node unlock conditions (`UnlockInfo`).

```
prerequisites <- Compound
├── advancements <- Compound (optional)
│   └── <criterion_key> <- String (Resource Location)
├── parents <- Compound (optional)
│   └── <criterion_key> <- String (Resource Location)
└── requirements <- List of Lists of Strings (optional)
    └── TAG_List of TAG_Strings
        └── TAG_String
```

- **`advancements`**: A map of custom criterion keys to Minecraft Advancement Resource Locations (`<namespace>:<path>`).
- **`parents`**: A map of custom criterion keys to parent Skill Nodes (or parent Skill Categories) Resource Locations.
- **`requirements`**: Matrix of criterion keys written in **Conjunctive Normal Form (CNF)** (an `AND` of `OR` clauses).
  - *Example:* `[["p1", "p2"], ["a1"]]` evaluates to `(p1 OR p2) AND (a1)`.
  - If not specified, defaults to requiring **all** criteria specified in `advancements` and `parents` (`allOf`).

---

## Example Data Files

### `_tab_.json`

```json
{
  "max_nodes_per_layer": 16,
  "seed": 20260210,
  "prerequisites": {},
  "tab_display": {
    "icon": "quietus:textures/gui/icons/skill_tree/tab/example.png",
    "name": {
      "translate": "skillTree.quietus.tab.example.name"
    },
    "description": {
      "translate": "skillTree.quietus.tab.example.description"
    },
    "prerequisites": {},
    "themeColour": "#FF5555"
  }
}
```

### `<node_name>.json`

```json
{
  "max_amount": 5,
  "progress": 1,
  "layout": {
    "top": false,
    "prerequisites": {
      "parents": {
        "p1": "quietus:example_tab/example_root"
      },
      "requirements": [
        ["p1"]
      ]
    }
  },
  "unlock": {
    "prerequisites": {
      "parents": {
        "p1": "quietus:example_tab/example_root"
      },
      "advancements": {
        "a1": "minecraft:story/mine_stone"
      },
      "requirements": [
        ["p1"],
        ["a1"]
      ]
    }
  },
  "rewards": {
    "skills": [
      {
        "skill": "quietus:example_skill",
        "amount": 1,
        "source": "quietus:skill_tree"
      }
    ]
  },
  "display": {
    "type": "square_node",
    "icon": "quietus:textures/gui/icons/skill_tree/nodes/example.png",
    "header": {
      "translate": "skillTree.quietus.example_tab/example_branch.header"
    },
    "description": {
      "translate": "skillTree.quietus.example_tab/example_branch.description"
    },
    "prerequisites": {
      "advancements": {
        "a1": {
          "translate": "advancements.story.mine_stone.title"
        }
      }
    }
  }
}
```
