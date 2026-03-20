# Creating Placeholders

DynamicAPI lets you define PlaceholderAPI placeholders directly in YAML, using simple expressions to access Bukkit's API — no Java required.

---

## Where to put your placeholders

All placeholder files go inside the `plugins/DynamicAPI/placeholders/` folder. You can have as many `.yml` files as you want — DynamicAPI will automatically load any file that contains a `Placeholders:` section.

```
plugins/DynamicAPI/placeholders/
├── placeholders.yml        <- loaded automatically
├── more_placeholders.yml   <- also loaded automatically
└── my_custom_ones.yml      <- you can add as many as you want
```

After editing any file, use `/dapi reload` to apply changes without restarting.

---

## Placeholder structure

Every placeholder follows this structure:

```yaml
Placeholders:

  my_placeholder:               # Internal ID — used in /dapi enable, /dapi disable and /dapi list
    enable: true                # true to activate, false to deactivate
    prefix: 'myplugin'          # First part of the placeholder: %myplugin_..%
    pattern: 'something'        # Second part: %myplugin_something%
    requires_player: false      # true if the placeholder needs a player to evaluate
    context:                    # Intermediate objects (evaluated in order)
      myobj: 'expression'
    variables:                  # Final values to extract
      result: 'expression'
    returns: '{result}'         # What the placeholder returns — use {varname} to interpolate
    fallback: '0'               # Returned if anything in the chain is null
```

---

## Expressions

Expressions are simple chains of method calls and property accesses, similar to Java.

### Available roots

These are always available in any expression:

| Root | Value |
|---|---|
| `server` | `Bukkit.getServer()` |
| `scoreboard` | `Bukkit.getScoreboardManager().getMainScoreboard()` |
| `player` | The player evaluating the placeholder (only when `requires_player: true`) |

### Syntax

| Syntax | What it does |
|---|---|
| `obj.property` | Calls `getProperty()`, `isProperty()`, or a no-arg method named `property` |
| `obj.method(arg)` | Calls a method with arguments |
| `obj.a.b.c` | Chains multiple accesses |

### Argument types

| Syntax | Type |
|---|---|
| `<team>` | Adaptive variable from the pattern |
| `"text"` | String literal |
| `42` | Integer literal |
| `true` / `false` | Boolean literal |
| `player` | The player object |
| `othervar` | Any other binding from `context` |

---

## Adaptive variables (pattern variables)

You can use `<varname>` in your pattern to capture part of the placeholder name at runtime.

For example, with `pattern: 'size_<team>'`:

- `%dapi_team_size_voiid%` -> captures `team = "voiid"`
- `%dapi_team_size_studios%` -> captures `team = "studios"`

You can then use `<team>` in your expressions.

Multiple adaptive variables are also supported: `pattern: 'score_<objective>_<player>'`

/// admonition | Note
    type: note

Intermediate variables in a multi-variable pattern cannot contain underscores in their value. Only the last variable can. For example, in `size_<team>`, the team name can contain underscores, but in `score_<objective>_<player>`, the objective cannot.
///

---

## Examples

///// note |
Click the :material-plus-circle: icons in the code blocks below for more information.
/////

### Scoreboard team size

Returns the number of entries in a scoreboard team.

```{ .yaml .annotate }
dapi_team_size:
  enable: true
  prefix: 'dapi'
  pattern: 'team_size_<team>'
  requires_player: false
  context:
    team: 'scoreboard.getTeam(<team>)' # (1)
  variables:
    result: 'team.size' # (2)
  returns: '{result}'
  fallback: '0'
```

1.  We retrieve the Team object using the scoreboard with the team name as the argument: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Scoreboard.html
2.  We get the int value of the team's size using "team.size" because we have obtained the Team object: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Team.html

**Usage:** `%dapi_team_size_voiid%` -> `3`

---

### Check if a team exists

Returns `true` or `false` depending on whether a team exists.

```{ .yaml .annotate }
dapi_team_exists:
  enable: true
  prefix: 'dapi'
  pattern: 'team_exists_<team>'
  requires_player: false
  context:
    team: 'scoreboard.getTeam(<team>)' # (1)
  variables:
    result: 'team.name' # (2)
  returns: 'true' # (3)
  fallback: 'false'
```

1.  Fetches the Team object. If the team doesn't exist, `getTeam()` returns `null` and the fallback is triggered.
2.  We access `team.name` just to verify the object is not null. The actual value is discarded.
3.  If we reach `returns`, it means the team exists — so we always return the literal `"true"`. If anything was null, the `fallback: 'false'` would have been returned instead.

**Usage:** `%dapi_team_exists_studios%` -> `true`

---

### Player's team name

Returns the name of the team the evaluating player belongs to.

```{ .yaml .annotate }
dapi_myteam_name:
  enable: true
  prefix: 'dapi'
  pattern: 'myteam_name'
  requires_player: true
  context:
    team: 'scoreboard.getPlayerTeam(player)' # (1)
  variables:
    result: 'team.name'
  returns: '{result}'
  fallback: 'none'
```

1.  `getPlayerTeam()` returns the team the player belongs to, or `null` if they are not in any team. In that case the fallback `none` is returned: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Scoreboard.html

**Usage:** `%dapi_myteam_name%` -> `voiid`

---

### Player health

Returns the current health of the player evaluating the placeholder.

```{ .yaml .annotate }
dapi_player_health:
  enable: true
  prefix: 'dapi'
  pattern: 'player_health'
  requires_player: true
  context: {}
  variables:
    result: 'player.health' # (1)
  returns: '{result}'
  fallback: '0'
```

1.  Calls `player.getHealth()` which returns a `double` between `0.0` and the player's max health: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Damageable.html

**Usage:** `%dapi_player_health%` -> `18.0`

---

### Player XP level

```{ .yaml .annotate }
dapi_player_level:
  enable: true
  prefix: 'dapi'
  pattern: 'player_level'
  requires_player: true
  context: {}
  variables:
    result: 'player.level' # (1)
  returns: '{result}'
  fallback: '0'
```

1.  Calls `player.getLevel()` which returns the player's current experience level as an `int`: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Player.html

**Usage:** `%dapi_player_level%` -> `42`

---

### Player's current world name

Returns the name of the world the evaluating player is currently in.

```{ .yaml .annotate }
dapi_player_world:
  enable: true
  prefix: 'dapi'
  pattern: 'player_world'
  requires_player: true
  context:
    world: 'player.getWorld()' # (1)
  variables:
    result: 'world.name' # (2)
  returns: '{result}'
  fallback: 'unknown'
```

1.  Retrieves the `World` object the player is currently in: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html
2.  Calls `world.getName()` which returns the world's name as defined in the server configuration.

**Usage:** `%dapi_player_world%` -> `world_nether`

---

### Online players and server capacity

Returns the number of players currently online, and the maximum player slots.

```{ .yaml .annotate }
dapi_server_online:
  enable: true
  prefix: dapi
  pattern: server_online
  requires_player: false
  context: {}
  variables:
    result: 'server.onlinePlayers.size' # (1)
  returns: '{result}'
  fallback: '0'

dapi_server_max_players:
  enable: true
  prefix: dapi
  pattern: server_maxplayers
  requires_player: false
  context: {}
  variables:
    result: 'server.maxPlayers' # (2)
  returns: '{result}'
  fallback: '0'
```

1.  Calls `server.getOnlinePlayers()` which returns a `Collection<Player>`, then `.size()` on that collection to get the count.
2.  Calls `server.getMaxPlayers()` which returns the player slot limit configured in `server.properties`.

**Usage:** `%dapi_server_online%` -> `12` · `%dapi_server_max_players%` -> `20`

---

### Player balance (EssentialsX)

Returns the economy balance of the evaluating player using EssentialsX.

/// admonition | Requirement
    type: warning

This placeholder requires [EssentialsX](https://essentialsx.net) to be installed on your server.
///

```{ .yaml .annotate }
dapi_essentials_balance:
  enable: true
  prefix: 'dapi'
  pattern: 'eco_balance'
  requires_player: true
  context:
    ess: 'server.getPluginManager().getPlugin("Essentials")' # (1)
    user: 'ess.getUser(player)' # (2)
  variables:
    result: 'user.money' # (3)
  returns: '{result}'
  fallback: '0'
```

1.  Retrieves the EssentialsX plugin instance via the Bukkit PluginManager. If EssentialsX is not installed this returns `null` and the fallback is triggered.
2.  Fetches the EssentialsX `User` object for the evaluating player: https://jd.essentialsx.net/
3.  Calls `user.getMoney()` which returns the player's balance as a `BigDecimal`. Since `getPlugin()` returns a generic `Plugin` object, DynamicAPI accesses this through reflection — no direct API dependency needed.

**Usage:** `%dapi_eco_balance%` -> `15420.50`

/// admonition | Note
    type: note

Since `getPlugin()` returns a generic `Plugin` object, DynamicAPI accesses `getUser()` and `getMoney()` through reflection. As long as EssentialsX is loaded and the method signatures remain the same, this works without needing a direct API dependency.
///

---

## The `returns` field

The `returns` field supports simple string interpolation using `{varname}`:

```yaml
returns: 'The team has {result} members'
```

You can also return a literal string without any variable:

```yaml
returns: 'true'   # always returns "true" if no fallback was triggered
```

---

## The `fallback` field

The fallback is returned whenever:

- A `context` expression evaluates to `null` (e.g., the team doesn't exist)
- A `variables` expression evaluates to `null`
- A `{varname}` in `returns` references a variable that is `null`

Always set a sensible fallback to avoid empty outputs in your server.

---

## Customizing messages

You can override any plugin message without touching the original language files. Simply add your overrides to `plugins/DynamicAPI/messages/custom/custom.yml`:

```yaml
command:
  reload: "&aPlaceholders reloaded successfully!"

list:
  title: "&ePlaceholders loaded (%PLACEHOLDERS%):"
```

Use `/dapi reload` to apply message changes.