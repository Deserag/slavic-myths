# Исходный курганный loot до правок 0.9.6

Все перечисленные ID сохраняются. Новые room tables расширяют распределение, старые tables остаются доступны.

## `slavicmyths:chests/burial_common`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_coin"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_comb"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_beads"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:old_buckle"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:old_arrowhead"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:pottery_fragment"
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/burial_great`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.55
        }
      ],
      "entries": [
        {
          "type": "minecraft:loot_table",
          "weight": 10,
          "value": "slavicmyths:chests/burial_common"
        }
      ]
    },
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.28
        }
      ],
      "entries": [
        {
          "type": "minecraft:loot_table",
          "weight": 6,
          "value": "slavicmyths:chests/burial_common"
        },
        {
          "type": "minecraft:loot_table",
          "weight": 6,
          "value": "slavicmyths:chests/burial_jewelry"
        },
        {
          "type": "minecraft:loot_table",
          "weight": 4,
          "value": "slavicmyths:chests/burial_weapons"
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/burial_jewelry`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_fibula"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:grivna"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:lunula"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:grave_ward"
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/burial_small`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.55
        }
      ],
      "entries": [
        {
          "type": "minecraft:loot_table",
          "weight": 10,
          "value": "slavicmyths:chests/burial_common"
        }
      ]
    },
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.28
        }
      ],
      "entries": [
        {
          "type": "minecraft:loot_table",
          "weight": 6,
          "value": "slavicmyths:chests/burial_common"
        },
        {
          "type": "minecraft:loot_table",
          "weight": 3,
          "value": "slavicmyths:chests/burial_jewelry"
        },
        {
          "type": "minecraft:loot_table",
          "weight": 2,
          "value": "slavicmyths:chests/burial_weapons"
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/burial_warrior`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.55
        }
      ],
      "entries": [
        {
          "type": "minecraft:loot_table",
          "weight": 10,
          "value": "slavicmyths:chests/burial_common"
        }
      ]
    },
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.28
        }
      ],
      "entries": [
        {
          "type": "minecraft:loot_table",
          "weight": 6,
          "value": "slavicmyths:chests/burial_common"
        },
        {
          "type": "minecraft:loot_table",
          "weight": 4,
          "value": "slavicmyths:chests/burial_jewelry"
        },
        {
          "type": "minecraft:loot_table",
          "weight": 5,
          "value": "slavicmyths:chests/burial_weapons"
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/burial_weapons`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_carolingian_sword"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_chekan"
        },
        {
          "type": "minecraft:item",
          "name": "slavicmyths:ancient_spear"
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_great_burial`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.4
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_great_important`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_weapons"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.4
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_great_offering`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_jewelry"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.4
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_small_burial`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.25
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_small_important`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_weapons"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.25
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_small_offering`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_jewelry"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.25
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_warrior_burial`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.25
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_warrior_important`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_weapons"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.25
        }
      ]
    }
  ]
}
```

## `slavicmyths:chests/kurgan_warrior_offering`

```json
{
  "type": "minecraft:chest",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_common"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.6
        }
      ]
    },
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:loot_table",
          "value": "slavicmyths:chests/burial_jewelry"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:random_chance",
          "chance": 0.25
        }
      ]
    }
  ]
}
```
