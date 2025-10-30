package com.example.holiday_service.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NagerHolidayDTO {

    private String date;
    private String localName;
    private String name;
    private String countryCode;
    private String type;
}
