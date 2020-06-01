package com.ixingji.task.util;

public class AssertUtils {

    public static class AssertUnexpectedException extends RuntimeException {
        public AssertUnexpectedException(String message) {
            super(message);
        }
    }

    public static void notNull(Object target, String message) {
        if (target == null) {
            throw new AssertUnexpectedException(message);
        }
    }

    public static void isTrue(boolean target, String message) {
        if (!target) {
            throw new AssertUnexpectedException(message);
        }
    }

}
