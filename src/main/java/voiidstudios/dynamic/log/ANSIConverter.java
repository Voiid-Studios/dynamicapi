package voiidstudios.dynamic.log;

import java.util.HashMap;
import java.util.Map;

public final class ANSIConverter {
    private static final Map<EpicChatColor, String> replacements = new HashMap<EpicChatColor, String>();

    static {
        replacements.put(EpicChatColor.BLACK, "\u001B[30;22m");
        replacements.put(EpicChatColor.DARK_BLUE, "\u001B[34;22m");
        replacements.put(EpicChatColor.DARK_GREEN, "\u001B[32;22m");
        replacements.put(EpicChatColor.DARK_AQUA, "\u001B[36;22m");
        replacements.put(EpicChatColor.DARK_RED, "\u001B[31;22m");
        replacements.put(EpicChatColor.DARK_PURPLE, "\u001B[35;22m");
        replacements.put(EpicChatColor.GOLD, "\u001B[33;22m");
        replacements.put(EpicChatColor.GRAY, "\u001B[37;22m");
        replacements.put(EpicChatColor.DARK_GRAY, "\u001B[30;1m");
        replacements.put(EpicChatColor.BLUE, "\u001B[34;1m");
        replacements.put(EpicChatColor.GREEN, "\u001B[32;1m");
        replacements.put(EpicChatColor.AQUA, "\u001B[36;1m");
        replacements.put(EpicChatColor.RED, "\u001B[31;1m");
        replacements.put(EpicChatColor.LIGHT_PURPLE, "\u001B[35;1m");
        replacements.put(EpicChatColor.YELLOW, "\u001B[33;1m");
        replacements.put(EpicChatColor.WHITE, "\u001B[37;1m");
        replacements.put(EpicChatColor.MAGIC, "\u001B[5m");
        replacements.put(EpicChatColor.BOLD, "\u001B[21m");
        replacements.put(EpicChatColor.STRIKETHROUGH, "\u001B[9m");
        replacements.put(EpicChatColor.UNDERLINE, "\u001B[4m");
        replacements.put(EpicChatColor.ITALIC, "\u001B[3m");
        replacements.put(EpicChatColor.RESET, "\u001B[0;39m");
    }

    private ANSIConverter() {}

    public static String convertToAnsi(String minecraftMessage) {
        String result = minecraftMessage;
        for (EpicChatColor color : EpicChatColor.values()) {
            result = result.replaceAll("(?i)" + color.toString(), replacements.containsKey(color) ? replacements.get(color) : "");
        }
        return result;
    }
}