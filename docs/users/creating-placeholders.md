# Creating Placeholders

DynamicAPI lets you define PlaceholderAPI placeholders directly in YAML, using simple expressions to access Bukkit's API — no Java required.

---

## Where to put your placeholders

All placeholder files go inside the `plugins/DynamicAPI/placeholders/` folder. You can have as many `.yml` files as you want — DynamicAPI will automatically load any file that contains a `Placeholders:` section.

```
plugins/DynamicAPI/placeholders/
├── placeholders.yml        ← loaded automatically
├── more_placeholders.yml   ← also loaded automatically
└── my_custom_ones.yml      ← you can add as many as you want
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

- `%teaminfo_size_rojo%` → captures `team = "rojo"`
- `%teaminfo_size_azul%` → captures `team = "azul"`

You can then use `<team>` in your expressions.

Multiple adaptive variables are also supported: `pattern: 'score_<objective>_<player>'`

/// admonition | Note
    type: note

Intermediate variables in a multi-variable pattern cannot contain underscores in their value. Only the last variable can. For example, in `size_<team>`, the team name can contain underscores, but in `score_<objective>_<player>`, the objective cannot.
///

---

## Examples

### Scoreboard team size

Returns the number of entries in a scoreboard team.

```yaml
team_size:
  enable: true
  prefix: 'teaminfo'
  pattern: 'size_<team>'
  requires_player: false
  context:
    team: 'scoreboard.getTeam(<team>)'
  variables:
    result: 'team.size'
  returns: '{result}'
  fallback: '0'
```

**Usage:** `%teaminfo_size_rojo%` → `3`

---

### Check if a team exists

Returns `true` or `false` depending on whether a team exists.

```yaml
team_exists:
  enable: true
  prefix: 'teaminfo'
  pattern: 'exists_<team>'
  requires_player: false
  context:
    team: 'scoreboard.getTeam(<team>)'
  variables:
    result: 'team.name'
  returns: 'true'
  fallback: 'false'
```

**Usage:** `%teaminfo_exists_azul%` → `true`

---

### Player's team name

Returns the name of the team the evaluating player belongs to.

```yaml
my_team_name:
  enable: true
  prefix: 'myteam'
  pattern: 'name'
  requires_player: true
  context:
    team: 'scoreboard.getPlayerTeam(player)'
  variables:
    result: 'team.name'
  returns: '{result}'
  fallback: 'none'
```

**Usage:** `%myteam_name%` → `rojo`

---

### Player health

Returns the current health of the player evaluating the placeholder.

```yaml
player_health:
  enable: true
  prefix: 'playerinfo'
  pattern: 'health'
  requires_player: true
  context: {}
  variables:
    result: 'player.health'
  returns: '{result}'
  fallback: '0'
```

**Usage:** `%playerinfo_health%` → `18.0`

---

### Player XP level

```yaml
player_level:
  enable: true
  prefix: 'playerinfo'
  pattern: 'level'
  requires_player: true
  context: {}
  variables:
    result: 'player.level'
  returns: '{result}'
  fallback: '0'
```

**Usage:** `%playerinfo_level%` → `42`

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
