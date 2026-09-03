package com.EreliaStudio.OneBlockAchievement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/** Reflection keeps Glymera optional while using its official public balance methods when installed. */
final class GlymeraEconomy {
    private final Object plugin;
    private final Method getBalance;
    private final Method removeBalance;
    private final Method addBalance;

    private GlymeraEconomy(Object plugin, Method getBalance, Method removeBalance, Method addBalance) {
        this.plugin = plugin;
        this.getBalance = getBalance;
        this.removeBalance = removeBalance;
        this.addBalance = addBalance;
    }

    static GlymeraEconomy discover() {
        try {
            Class<?> type = Class.forName("de.glymera.merchant.GlymeraMerchant");
            Object plugin = type.getMethod("get").invoke(null);
            if (plugin == null) return null;
            return new GlymeraEconomy(plugin, type.getMethod("getBalance", UUID.class),
                    type.getMethod("removeBalance", UUID.class, long.class),
                    type.getMethod("addBalance", UUID.class, long.class));
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return null;
        }
    }

    long balance(UUID playerId) { return (Long) invoke(getBalance, playerId); }
    boolean withdraw(UUID playerId, long amount) { return (Boolean) invoke(removeBalance, playerId, amount); }
    void refund(UUID playerId, long amount) {
        if (!(Boolean) invoke(addBalance, playerId, amount))
            throw new IllegalStateException("Glymera rejected a currency refund");
    }

    private Object invoke(Method method, Object... arguments) {
        try { return method.invoke(plugin, arguments); }
        catch (IllegalAccessException error) { throw new IllegalStateException("Cannot access Glymera economy", error); }
        catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            throw new IllegalStateException("Glymera economy failed: " + (cause == null ? error.getMessage() : cause.getMessage()), cause);
        }
    }
}
