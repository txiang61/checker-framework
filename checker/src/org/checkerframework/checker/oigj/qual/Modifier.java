package org.checkerframework.checker.oigj.qual;

import java.lang.annotation.*;

import org.checkerframework.framework.qual.*;

/**
 * @checker_framework.manual #oigj-checker OIGJ Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf(Dominator.class)
@DefaultQualifierInHierarchy
@DefaultFor({ TypeUseLocation.IMPLICIT_UPPER_BOUND, TypeUseLocation.EXCEPTION_PARAMETER})
public @interface Modifier {}
