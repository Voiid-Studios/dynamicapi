# Installation

## Requirements

Before installing DynamicAPI, make sure your server meets the following requirements:

/// admonition | Requirements
    type: info

- **Minecraft version:** 1.8 or later
- **Server software:** Spigot, Paper, or Purpur (not CraftBukkit)
- **Dependency:** [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) must be installed
///

---

## Steps

**1.** Download the latest version of DynamicAPI from [Modrinth](https://modrinth.com/plugin/dynamicapi).

**2.** Place the downloaded `.jar` file inside your server's `plugins/` folder.

**3.** Make sure [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) is also in your `plugins/` folder.

**4.** Restart your server. DynamicAPI will generate the following folder structure:

```
plugins/
└── DynamicAPI/
    ├── config.yml
    ├── placeholders/
    │   ├── placeholders.yml
    │   └── more_placeholders.yml
    └── messages/
        ├── origins/
        │   └── en_US.yml
        └── custom/
            └── custom.yml
```

**5.** You're ready to go! Use `/dapi help` to see all available commands.

---

## Configuration

The `config.yml` file contains the main settings for DynamicAPI:

```yaml
Config:
  language: 'en_US'           # Language of the plugin messages
  update_notification: true   # Notify admins when a new update is available
  auto_update: false          # Automatically download and install updates
  bstats_metrics: true        # Send anonymous usage statistics to bStats
```

---

## Updating

If `auto_update` is enabled in `config.yml`, DynamicAPI will automatically download new versions into the server's `update/` folder and apply them on the next restart.

If you prefer to update manually, simply replace the `.jar` file in `plugins/` with the new one and restart the server.
