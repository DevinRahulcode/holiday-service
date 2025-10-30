package com.example.holiday_service.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayCheckDTO {

    private String date;
    private String countryCode;
    private boolean isHoliday;
    private String holidayName;

}
