package com.example.engine

import com.example.model.PackCategory

object BedrockTemplateGenerator {

    fun sanitizeIdentifier(name: String): String {
        return name.lowercase()
            .replace("[^a-z0-9_]".toRegex(), "_")
            .trim('_')
            .ifEmpty { "custom_addon" }
    }

    fun generateEntityBehavior(namespace: String, id: String, displayName: String, prompt: String): String {
        val health = if (prompt.contains("chefe", true) || prompt.contains("boss", true)) 300 else 50
        val speed = if (prompt.contains("rápido", true) || prompt.contains("agil", true)) 0.35 else 0.25
        val attack = if (prompt.contains("forte", true) || prompt.contains("dano", true)) 12 else 6

        return """
{
  "format_version": "1.20.0",
  "minecraft:entity": {
    "description": {
      "identifier": "$namespace:$id",
      "is_spawnable": true,
      "is_summonable": true,
      "is_experimental": false
    },
    "component_groups": {},
    "components": {
      "minecraft:is_hidden_when_invisible": {},
      "minecraft:experience_reward": {
        "on_death": "query.last_hit_by_player ? 15 : 0"
      },
      "minecraft:type_family": {
        "family": [ "$id", "mob", "monster" ]
      },
      "minecraft:breathable": {
        "total_supply": 15,
        "suffocate_time": 0
      },
      "minecraft:health": {
        "value": $health,
        "max": $health
      },
      "minecraft:movement": {
        "value": $speed
      },
      "minecraft:navigation.walk": {
        "can_path_over_water": true,
        "avoid_damage_blocks": true
      },
      "minecraft:movement.basic": {},
      "minecraft:jump.static": {},
      "minecraft:can_climb": {},
      "minecraft:collision_box": {
        "width": 0.8,
        "height": 1.9
      },
      "minecraft:attack": {
        "damage": $attack
      },
      "minecraft:behavior.melee_attack": {
        "priority": 2,
        "speed_multiplier": 1.25,
        "track_target": true
      },
      "minecraft:behavior.nearest_attackable_target": {
        "priority": 3,
        "entity_types": [
          {
            "filters": {
              "test": "is_family",
              "subject": "other",
              "value": "player"
            },
            "max_dist": 16
          }
        ],
        "must_see": true
      },
      "minecraft:behavior.random_stroll": {
        "priority": 6,
        "speed_multiplier": 0.8
      },
      "minecraft:behavior.random_look_around": {
        "priority": 7
      },
      "minecraft:physics": {}
    }
  }
}
""".trimIndent()
    }

    fun generateClientEntity(namespace: String, id: String): String {
        return """
{
  "format_version": "1.10.0",
  "minecraft:client_entity": {
    "description": {
      "identifier": "$namespace:$id",
      "materials": {
        "default": "entity_alphatest"
      },
      "textures": {
        "default": "textures/entity/$id"
      },
      "geometry": {
        "default": "geometry.$id"
      },
      "render_controllers": [
        "controller.render.default"
      ],
      "spawn_egg": {
        "base_color": "#4a2870",
        "overlay_color": "#a855f7"
      }
    }
  }
}
""".trimIndent()
    }

    fun generateItemBehavior(namespace: String, id: String, displayName: String, prompt: String): String {
        val isFood = prompt.contains("comida", true) || prompt.contains("alimento", true) || prompt.contains("esporo", true)
        val isWeapon = prompt.contains("espada", true) || prompt.contains("arma", true) || prompt.contains("dano", true)

        val customComponent = when {
            isFood -> """
      "minecraft:food": {
        "nutrition": 6,
        "saturation_modifier": "high",
        "can_always_eat": true
      },
      "minecraft:use_animation": "eat"
"""
            isWeapon -> """
      "minecraft:damage": 9,
      "minecraft:hand_equipped": true,
      "minecraft:durability": {
        "max_durability": 1561
      }
"""
            else -> """
      "minecraft:max_stack_size": 64
"""
        }

        return """
{
  "format_version": "1.20.50",
  "minecraft:item": {
    "description": {
      "identifier": "$namespace:$id",
      "menu_category": {
        "category": "${if (isWeapon) "equipment" else if (isFood) "nature" else "items"}"
      }
    },
    "components": {
      "minecraft:icon": {
        "textures": {
          "default": "${id}_icon"
        }
      },
      "minecraft:display_name": {
        "value": "$displayName"
      },
$customComponent
    }
  }
}
""".trimIndent()
    }

    fun generateBlockBehavior(namespace: String, id: String, displayName: String, prompt: String): String {
        val lightEmission = if (prompt.contains("brilh", true) || prompt.contains("luz", true) || prompt.contains("luminescen", true)) 12 else 0

        return """
{
  "format_version": "1.20.50",
  "minecraft:block": {
    "description": {
      "identifier": "$namespace:$id",
      "menu_category": {
        "category": "construction"
      }
    },
    "components": {
      "minecraft:destructible_by_mining": {
        "seconds_to_destroy": 2.5
      },
      "minecraft:destructible_by_explosion": {
        "explosion_resistance": 6.0
      },
      "minecraft:friction": 0.6,
      "minecraft:light_emission": $lightEmission,
      "minecraft:map_color": "#7c3aed",
      "minecraft:unit_cube": {},
      "minecraft:material_instances": {
        "*": {
          "texture": "${id}_block",
          "render_method": "opaque"
        }
      }
    }
  }
}
""".trimIndent()
    }

    fun generateStandardGeometry(id: String): String {
        return """
{
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.$id",
        "texture_width": 64,
        "texture_height": 64,
        "visible_bounds_width": 2,
        "visible_bounds_height": 3,
        "visible_bounds_offset": [0, 1, 0]
      },
      "bones": [
        {
          "name": "root",
          "pivot": [0, 0, 0]
        },
        {
          "name": "body",
          "parent": "root",
          "pivot": [0, 12, 0],
          "cubes": [
            {
              "origin": [-4, 12, -2],
              "size": [8, 12, 4],
              "uv": [16, 16]
            }
          ]
        },
        {
          "name": "head",
          "parent": "body",
          "pivot": [0, 24, 0],
          "cubes": [
            {
              "origin": [-4, 24, -4],
              "size": [8, 8, 8],
              "uv": [0, 0]
            }
          ]
        },
        {
          "name": "right_arm",
          "parent": "body",
          "pivot": [-5, 22, 0],
          "cubes": [
            {
              "origin": [-8, 12, -2],
              "size": [4, 12, 4],
              "uv": [40, 16]
            }
          ]
        },
        {
          "name": "left_arm",
          "parent": "body",
          "pivot": [5, 22, 0],
          "cubes": [
            {
              "origin": [4, 12, -2],
              "size": [4, 12, 4],
              "uv": [32, 48]
            }
          ]
        },
        {
          "name": "right_leg",
          "parent": "root",
          "pivot": [-1.9, 12, 0],
          "cubes": [
            {
              "origin": [-3.9, 0, -2],
              "size": [4, 12, 4],
              "uv": [0, 16]
            }
          ]
        },
        {
          "name": "left_leg",
          "parent": "root",
          "pivot": [1.9, 12, 0],
          "cubes": [
            {
              "origin": [-0.1, 0, -2],
              "size": [4, 12, 4],
              "uv": [16, 48]
            }
          ]
        }
      ]
    }
  ]
}
""".trimIndent()
    }

    fun generateItemTextureJson(id: String): String {
        return """
{
  "resource_pack_name": "BedrockForge",
  "texture_name": "atlas.items",
  "texture_data": {
    "${id}_icon": {
      "textures": "textures/items/$id"
    }
  }
}
""".trimIndent()
    }

    fun generateTerrainTextureJson(id: String): String {
        return """
{
  "resource_pack_name": "BedrockForge",
  "texture_name": "atlas.terrain",
  "padding": 8,
  "num_mip_levels": 4,
  "texture_data": {
    "${id}_block": {
      "textures": "textures/blocks/$id"
    }
  }
}
""".trimIndent()
    }

    fun generateLangFile(namespace: String, id: String, displayName: String, category: PackCategory): String {
        return when (category) {
            PackCategory.ENTITY -> """
entity.$namespace:$id.name=$displayName
item.spawn_egg.entity.$namespace:$id.name=Invocar $displayName
""".trimIndent()
            PackCategory.ITEM -> """
item.$namespace:$id=$displayName
""".trimIndent()
            PackCategory.BLOCK -> """
tile.$namespace:$id.name=$displayName
""".trimIndent()
            PackCategory.COMPLETE -> """
entity.$namespace:${id}_mob.name=$displayName Guardião
item.$namespace:${id}_sword=$displayName Espada
tile.$namespace:${id}_ore.name=Minério $displayName
""".trimIndent()
        }
    }
}
