package com.example.demo.comun;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class Util {
    private Util() { }

    public static BigDecimal dec(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        return new BigDecimal(o.toString());
    }

    public static int ent(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }

    public static Integer entOpt(Object o) { return o == null ? null : ent(o); }

    public static String str(Object o) { return o == null ? null : o.toString(); }

    public static Timestamp ahora() { return Timestamp.valueOf(LocalDateTime.now()); }
}
