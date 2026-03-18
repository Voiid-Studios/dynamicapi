package voiidstudios.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class PlaceholderEntry {
    public final String id;
    public final boolean enabled;
    public final String prefix;
    public final String pattern;
    public final boolean requiresPlayer;
    public final Map<String, String> context;
    public final Map<String, String> variables;
    public final String returns;
    public final String fallback;

    public final Pattern regex;
    public final List<String> captureGroupNames;

    public PlaceholderEntry(
            String id, boolean enabled,
            String prefix, String pattern, boolean requiresPlayer,
            Map<String, String> context, Map<String, String> variables,
            String returns, String fallback) {

        this.id = id;
        this.enabled = enabled;
        this.prefix = prefix;
        this.pattern = pattern;
        this.requiresPlayer = requiresPlayer;
        this.context = context;
        this.variables = variables;
        this.returns = returns;
        this.fallback = fallback;

        this.captureGroupNames = new ArrayList<String>();
        this.regex = compilePattern(pattern, captureGroupNames);
    }

    private static Pattern compilePattern(String pattern, List<String> groupNames) {
        StringBuilder sb = new StringBuilder("^");
        int i = 0;

        List<int[]> varPositions = new ArrayList<int[]>();
        int search = 0;
        while ((search = pattern.indexOf('<', search)) != -1) {
            int close = pattern.indexOf('>', search);
            if (close == -1) break;
            varPositions.add(new int[]{search, close});
            search = close + 1;
        }

        for (int vi = 0; vi < varPositions.size(); vi++) {
            int[] pos = varPositions.get(vi);
            sb.append(Pattern.quote(pattern.substring(i, pos[0])));
            groupNames.add(pattern.substring(pos[0] + 1, pos[1]));
            sb.append(vi == varPositions.size() - 1 ? "(.+)" : "([^_]+)");
            i = pos[1] + 1;
        }

        if (i < pattern.length()) sb.append(Pattern.quote(pattern.substring(i)));
        sb.append("$");
        return Pattern.compile(sb.toString());
    }

    public boolean matches(String params) {
        return regex.matcher(params).matches();
    }

    @Override
    public String toString() {
        return "PlaceholderEntry{%" + prefix + "_" + pattern + "%}";
    }
}
