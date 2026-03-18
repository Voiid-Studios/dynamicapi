package voiidstudios.dynamic;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class DynamicExpansion extends PlaceholderExpansion {
    private final String pluginVersion;
    private final String identifier;
    private final List<PlaceholderEntry> entries;

    public DynamicExpansion(String pluginVersion, String identifier, List<PlaceholderEntry> entries) {
        this.pluginVersion = pluginVersion;
        this.identifier = identifier;
        this.entries = entries;
    }

    @Override public String getIdentifier() { return identifier; }
    @Override public String getAuthor() { return "DynamicAPI"; }
    @Override public String getVersion() { return pluginVersion; }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        for (PlaceholderEntry entry : entries) {
            if (!entry.enabled) continue;

            Matcher matcher = entry.regex.matcher(params);
            if (!matcher.matches()) continue;

            if (entry.requiresPlayer && player == null) return entry.fallback;

            Map<String, Object> bindings = baseBindings(player);
            for (int g = 0; g < entry.captureGroupNames.size(); g++) {
                String varName = entry.captureGroupNames.get(g);
                String value = matcher.group(g + 1);
                bindings.put("<" + varName + ">", value);
                bindings.put(varName, value);
            }

            for (Map.Entry<String, String> ctx : entry.context.entrySet()) {
                bindings.put(ctx.getKey(), ExpressionEvaluator.evaluate(ctx.getValue(), bindings));
            }

            for (Map.Entry<String, String> varDef : entry.variables.entrySet()) {
                Object val = safeEval(varDef.getValue(), bindings);
                if (val == null) return entry.fallback;
                bindings.put(varDef.getKey(), val);
            }

            return interpolate(entry.returns, bindings, entry.fallback);
        }
        return null;
    }


    private static Map<String, Object> baseBindings(Player player) {
        Map<String, Object> b = new LinkedHashMap<String, Object>();
        b.put("server", Bukkit.getServer());
        b.put("scoreboard", getMainScoreboard());
        if (player != null) b.put("player", player);
        return b;
    }

    private static Scoreboard getMainScoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private static Object safeEval(String expr, Map<String, Object> bindings) {
        String firstToken = expr.split("[.(]")[0].trim();
        if (bindings.containsKey(firstToken) && bindings.get(firstToken) == null) return null;
        return ExpressionEvaluator.evaluate(expr, bindings);
    }

    private static String interpolate(String template, Map<String, Object> bindings, String fallback) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            if (template.charAt(i) == '{') {
                int close = template.indexOf('}', i);
                if (close == -1) { result.append(template.substring(i)); break; }
                String varName = template.substring(i + 1, close);
                if (!bindings.containsKey(varName) || bindings.get(varName) == null) return fallback;
                result.append(bindings.get(varName));
                i = close + 1;
            } else {
                result.append(template.charAt(i++));
            }
        }
        return result.toString();
    }

    public List<PlaceholderEntry> getEntries() { return entries; }
}
