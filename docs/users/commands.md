---
description: All available commands in DynamicAPI and their permissions.
---

# Commands

All DynamicAPI commands use the `/dapi` alias (or `/dynamicapi`).

/// admonition | Permissions
    type: info

All commands require the `dynamicapi.admin` permission, which is granted to server OPs by default.
///

---

## Command List

### `/dapi help`
Shows all available commands.

**Permission:** `dynamicapi.help`

---

### `/dapi reload`
Reloads `config.yml` and all placeholder files from the `placeholders/` folder without restarting the server.

**Permission:** `dynamicapi.reload`

/// admonition | Tip
    type: tip

Use this after editing any `.yml` file in the `placeholders/` folder to apply your changes instantly.
///

---

### `/dapi list`
Lists all placeholders that DynamicAPI has loaded, including both enabled and disabled ones.

- :material-check-circle:{ style="color: green" } **Green** - placeholder is active and registered in PlaceholderAPI
- :material-close-circle:{ style="color: red" } **Red** - placeholder is defined but currently disabled

**Permission:** `dynamicapi.list`

---

### `/dapi enable <id>`
Enables a placeholder by its internal ID. This edits the corresponding `.yml` file and reloads the expansions automatically.

**Permission:** `dynamicapi.enable`

**Example:**
```
/dapi enable team_size
```

---

### `/dapi disable <id>`
Disables a placeholder by its internal ID. This edits the corresponding `.yml` file and reloads the expansions automatically.

**Permission:** `dynamicapi.disable`

**Example:**
```
/dapi disable player_health
```

---

## Permissions Reference

| Permission | Description | Default |
|---|---|---|
| `dynamicapi.*` | All DynamicAPI permissions | ❌ |
| `dynamicapi.admin` | All admin permissions | OP |
| `dynamicapi.help` | Use `/dapi help` | OP |
| `dynamicapi.reload` | Use `/dapi reload` | OP |
| `dynamicapi.list` | Use `/dapi list` | OP |
| `dynamicapi.enable` | Use `/dapi enable` | OP |
| `dynamicapi.disable` | Use `/dapi disable` | OP |
