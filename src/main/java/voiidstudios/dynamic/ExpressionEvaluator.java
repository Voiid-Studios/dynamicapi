package voiidstudios.dynamic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExpressionEvaluator {
    private ExpressionEvaluator() {}

    public static Object evaluate(String expression, Map<String, Object> bindings) {
        if (expression == null || expression.trim().isEmpty()) return null;
        expression = expression.trim();

        int firstDot = dotOutsideParens(expression, 0);
        if (firstDot == -1) return resolveArg(expression, bindings);

        String rootName = expression.substring(0, firstDot);
        String rest = expression.substring(firstDot + 1);

        Object current = bindings.get(rootName);
        if (current == null) return null;

        return evaluateChain(current, rest, bindings);
    }

    private static Object evaluateChain(Object obj, String chain, Map<String, Object> bindings) {
        if (obj == null || chain == null || chain.isEmpty()) return obj;

        int parenOpen = chain.indexOf('(');
        int dotIdx = dotOutsideParens(chain, 0);
        boolean isMethod = (parenOpen != -1) && (dotIdx == -1 || parenOpen < dotIdx);

        if (isMethod) {
            int parenClose = matchingParen(chain, parenOpen);
            if (parenClose == -1) return null;

            String methodName = chain.substring(0, parenOpen).trim();
            String argsStr = chain.substring(parenOpen + 1, parenClose).trim();
            String rest = (parenClose + 2 < chain.length()) ? chain.substring(parenClose + 2) : null;

            Object result = callMethod(obj, methodName, parseArgs(argsStr, bindings));
            if (result == null || rest == null || rest.isEmpty()) return result;
            return evaluateChain(result, rest, bindings);

        } else {
            String propName = (dotIdx == -1) ? chain : chain.substring(0, dotIdx);
            String rest = (dotIdx == -1) ? null  : chain.substring(dotIdx + 1);

            Object result = getProperty(obj, propName);
            if (result == null || rest == null || rest.isEmpty()) return result;
            return evaluateChain(result, rest, bindings);
        }
    }

    private static Object[] parseArgs(String argsStr, Map<String, Object> bindings) {
        if (argsStr == null || argsStr.trim().isEmpty()) return new Object[0];
        List<String> parts = splitArgs(argsStr);
        Object[] args = new Object[parts.size()];
        for (int i = 0; i < parts.size(); i++) args[i] = resolveArg(parts.get(i).trim(), bindings);
        return args;
    }

    static Object resolveArg(String arg, Map<String, Object> bindings) {
        if (arg.startsWith("<") && arg.endsWith(">")) {
            Object v = bindings.get(arg);
            return v != null ? v : arg.substring(1, arg.length() - 1);
        }
        if (arg.startsWith("\"") && arg.endsWith("\"") && arg.length() >= 2)
            return arg.substring(1, arg.length() - 1);
        if ("true".equalsIgnoreCase(arg)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(arg)) return Boolean.FALSE;
        try { return Integer.parseInt(arg); } catch (NumberFormatException ignored) {}
        try { return Double.parseDouble(arg); } catch (NumberFormatException ignored) {}
        if (bindings.containsKey(arg)) return bindings.get(arg);
        return arg;
    }

    private static List<String> splitArgs(String s) {
        List<String> result = new ArrayList<String>();
        int depth = 0, start = 0;
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inString = !inString;
            else if (!inString) {
                if (c == '(') depth++;
                else if (c == ')') depth--;
                else if (c == ',' && depth == 0) {
                    result.add(s.substring(start, i));
                    start = i + 1;
                }
            }
        }
        if (start < s.length()) result.add(s.substring(start));
        return result;
    }

    private static Object getProperty(Object obj, String name) {
        String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Object r;
        if ((r = invokeNoArgs(obj, "get" + cap)) != null) return r;
        if ((r = invokeNoArgs(obj, "is"  + cap)) != null) return r;
        if ((r = invokeNoArgs(obj, name)) != null) return r;
        try {
            Field f = findField(obj.getClass(), name);
            if (f != null) { f.setAccessible(true); return f.get(obj); }
        } catch (Exception ignored) {}
        return null;
    }

    private static Object invokeNoArgs(Object obj, String name) {
        Method m = findMethod(obj.getClass(), name, new Class[0]);
        if (m == null) return null;
        try { m.setAccessible(true); return m.invoke(obj); } catch (Exception e) { return null; }
    }

    private static Object callMethod(Object obj, String name, Object[] args) {
        Class<?>[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        Method m = findMethod(obj.getClass(), name, argTypes);
        if (m == null) return null;
        try { m.setAccessible(true); return m.invoke(obj, args); } catch (Exception e) { return null; }
    }


    static Method findMethod(Class<?> clazz, String name, Class<?>[] argTypes) {
        return findMethodRecursive(clazz, name, argTypes, new HashSet<Class<?>>());
    }

    private static Method findMethodRecursive(Class<?> clazz, String name, Class<?>[] argTypes, Set<Class<?>> visited) {
        if (clazz == null || !visited.add(clazz)) return null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == argTypes.length
                    && argsCompatible(m.getParameterTypes(), argTypes)) return m;
        }
        Method r = findMethodRecursive(clazz.getSuperclass(), name, argTypes, visited);
        if (r != null) return r;
        for (Class<?> iface : clazz.getInterfaces()) {
            r = findMethodRecursive(iface, name, argTypes, visited);
            if (r != null) return r;
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }

    private static boolean argsCompatible(Class<?>[] expected, Class<?>[] actual) {
        for (int i = 0; i < expected.length; i++) {
            Class<?> e = expected[i], a = actual[i];
            if (a == Object.class || e.isAssignableFrom(a) || isPrimBoxCompat(e, a)) continue;
            return false;
        }
        return true;
    }

    private static boolean isPrimBoxCompat(Class<?> p, Class<?> b) {
        return (p == int.class && b == Integer.class)
            || (p == double.class && b == Double.class)
            || (p == float.class && b == Float.class)
            || (p == long.class && b == Long.class)
            || (p == boolean.class && b == Boolean.class)
            || (p == short.class && b == Short.class)
            || (p == byte.class && b == Byte.class);
    }
    

    private static int dotOutsideParens(String s, int from) {
        int depth = 0;
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if      (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == '.' && depth == 0) return i;
        }
        return -1;
    }

    private static int matchingParen(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            if      (s.charAt(i) == '(') depth++;
            else if (s.charAt(i) == ')') { if (--depth == 0) return i; }
        }
        return -1;
    }
}
