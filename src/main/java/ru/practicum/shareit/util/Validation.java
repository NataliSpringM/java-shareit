package ru.practicum.shareit.util;

/**
 * Validation data for patching operations
 */
public class Validation {

    /**
     * Checks String is not null and not blank
     *
     * @param string String object
     * @return if string is valid
     */
    public static boolean stringIsNotNullOrBlank(String string) {
        return string != null && !string.isBlank();
    }

    /**
     * Checks Email is in appropriate form
     *
     * @param email email
     * @return if email is valid
     */
    public static boolean validEmail(String email) {
        return !email.contains(" ") && email.contains("@");
    }

    /**
     * Checks Object is not null
     *
     * @param object Object
     * @return if object exists
     */

    public static boolean objectIsNotNull(Object object) {
        return object != null;
    }
}
