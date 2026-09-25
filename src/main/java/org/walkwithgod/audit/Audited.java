package org.walkwithgod.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    AuditAction action();
    String targetType() default "";
    /** Optional SpEL that resolves to the target UUID. */
    String targetIdSpel() default "";
}