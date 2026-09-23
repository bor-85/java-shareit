package ru.practicum.shareit.exception;

public class BookingValidationMessages {
    public static final String ERROR_USER_NOT_FOUND = "Не найден пользователь с id = ";
    public static final String ERROR_ITEM_NOT_FOUND = "Не найден Item с id = ";
    public static final String ERROR_BOOKING_NOT_FOUND = "Не найдено бронирование с id = ";

    public static final String ERROR_BOOKING_START_REQUIRED = "Дата начала бронирования обязательна";
    public static final String ERROR_BOOKING_END_REQUIRED = "Дата окончания бронирования обязательна";
    public static final String ERROR_BOOKING_DATE_ORDER = "Дата начала должна быть раньше даты окончания";
    public static final String ERROR_BOOKING_START_IN_FUTURE = "Дата начала бронирования должна быть в будущем";

    public static final String ERROR_OWNER_CANNOT_BOOK_OWN_ITEM = "Владелец вещи не может бронировать свою вещь";
    public static final String ERROR_ITEM_NOT_AVAILABLE = "Вещь недоступна для бронирования";

    public static final String ERROR_ONLY_WAITING_CAN_BE_APPROVED = "Подтвердить или отклонить можно только ожидающее бронирование";
    public static final String ERROR_UNKNOWN_STATE = "Неизвестный state: ";
}
