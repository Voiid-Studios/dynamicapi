# Installation

## Requirements

Before installing DynamicAPI, make sure your server meets the following requirements:

/// admonition | Requirements
    type: info

- **Minecraft version:** 1.8 or later
- **Server software:** Spigot, Paper or forks (In most cases, if PlaceholderAPI works on your server, that means DynamicAPI should work as well)
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
  # Change the language of DAPI within the game. The list of available languages is located at: plugins/DynamicAPI/messages
  language: 'en_US'
  # The plugin will attempt to auto-update when it finds a new version.
  auto_update: true
  # The plugin will send an update notification when it finds a new version if auto-update is disabled.
  update_notification: true
  # Contribute to our statistics anonymously with bStats. More information: https://bstats.org/
  bstats_metrics: true
```

---

## Updating

If `auto_update` is enabled in `config.yml`, DynamicAPI will automatically download new versions into the server's `update/` folder and apply them on the next restart.

If you prefer to update manually, simply replace the `.jar` file in `plugins/` with the new one and restart the server.