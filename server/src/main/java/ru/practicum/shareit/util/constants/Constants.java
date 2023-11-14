package ru.practicum.shareit.util.constants;

public class Constants {

    /**
     * Path parts constants
     */

    public static final String HEADER_USER_ID = "X-Sharer-User-Id";
    public static final String USERS_PATH = "/users";
    public static final String BOOKINGS_PATH = "/bookings";
    public static final String ITEMS_PATH = "/items";
    public static final String REQUESTS_PATH = "/requests";
    public static final String OWNER_PATH = "/owner";
    public static final String SEARCH_PATH = "/search";
    public static final String ALL_PATH = "/all";

    /**
     * Path variables' names' constants
     */

    public static final String USER_ID_PATH_VARIABLE = "/{id}";
    public static final String BOOKING_ID_PATH_VARIABLE = "/{bookingId}";
    public static final String REQUEST_ID_PATH_VARIABLE = "{requestId}";
    public static final String ITEM_ID_PATH_VARIABLE = "/{itemId}";
    public static final String COMMENT_PATH_VARIABLE = "/{itemId}/comment";

    /**
     * Parameters' names and default values constants
     */
    public static final String APPROVED_PARAM_NAME = "approved";
    public static final String BOOKING_STATE_PARAMETER_NAME = "state";
    public static final String TEXT_PARAMETER_NAME = "text";
    public static final String FROM_PARAMETER_NAME = "from";
    public static final String SIZE_PARAMETER_NAME = "size";
    public static final String ZERO_DEFAULT_VALUE = "0";
    public static final String TEN_DEFAULT_VALUE = "10";
    public static final String ALL_DEFAULT_VALUE = "all";


}
