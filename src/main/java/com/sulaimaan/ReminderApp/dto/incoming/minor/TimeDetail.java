package com.sulaimaan.ReminderApp.dto.incoming.minor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TimeDetail {

    @NotNull(message = "Seconds cannot be null")
    @Min(value = 0, message = "Seconds must be between 0 and 59")
    @Max(value = 59, message = "Seconds must be between 0 and 59")
    public Integer seconds;

    @NotNull(message = "Minutes cannot be null")
    @Min(value = 0, message = "Minutes must be between 0 and 59")
    @Max(value = 59, message = "Minutes must be between 0 and 59")
    public Integer minutes;

    @NotNull(message = "Hours cannot be null")
    @Min(value = 0, message = "Hours must be between 0 and 23")
    @Max(value = 23, message = "Hours must be between 0 and 23")
    public Integer hours;

    @NotBlank(message = "Timezone cannot be blank")
    public String timezone;
}
