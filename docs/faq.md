# FAQ

Frequently asked questions about DynamicAPI.

---

### Do I need to know Java to use DynamicAPI?

Not at all. DynamicAPI is specifically designed so that anyone who can read Bukkit's [JavaDocs](https://jd.papermc.io/paper/1.21/) can create placeholders using simple YAML expressions — no programming experience required.

---

### Does DynamicAPI replace PlaceholderAPI?

No. DynamicAPI is a **complement** to PlaceholderAPI, not a replacement. It depends on PAPI to work, and all placeholders you define through DynamicAPI are registered directly into PlaceholderAPI — just like any other expansion.

---

### Can I use DynamicAPI with other plugins' APIs?

Yes, as long as you can access the data through a chain of method calls starting from `server`, `scoreboard`, or `player`. For example, if another plugin exposes a manager accessible via `server.getPluginManager().getPlugin("MyPlugin")`, you can reach it from an expression.

The main limitation is that DynamicAPI doesn't support explicit casting between types, so deeply nested plugin-specific APIs may not always be reachable.

---

### My placeholder always returns the fallback value. Why?

This usually means one of the expressions in your `context` or `variables` is returning `null`. Common causes:

- The object you're trying to access doesn't exist (e.g., the team name is wrong)
- There's a typo in the method or property name
- The placeholder requires a player (`requires_player: true`) but is being evaluated without one

Use `/dapi list` to confirm the placeholder is registered, and double-check your expressions against the Bukkit JavaDocs.

---

### Can I have multiple placeholder files?

Yes. You can create as many `.yml` files as you want inside the `plugins/DynamicAPI/placeholders/` folder. Any file containing a `Placeholders:` section will be loaded automatically on startup or after `/dapi reload`.

---

### Does DynamicAPI support Folia?

Not at the moment. DynamicAPI is compatible with Spigot, Paper, and Purpur.

---

### Can I suggest a feature or report a bug?

Yes! Open an issue on the [GitHub repository](https://github.com/Voiid-Studios/dynamicapi/issues).
