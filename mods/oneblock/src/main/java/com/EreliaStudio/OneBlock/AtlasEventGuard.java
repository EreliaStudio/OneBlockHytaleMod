package com.EreliaStudio.OneBlock;

/** One-use opaque server revision: stale selection/quantity and double-clicks cannot replay a craft. */
final class AtlasEventGuard {
    private String token = java.util.UUID.randomUUID().toString();
    String token() { return token; }
    void advance() { token = java.util.UUID.randomUUID().toString(); }
    boolean consume(String candidate) {
        if (!token.equals(candidate)) return false;
        advance();
        return true;
    }
}
