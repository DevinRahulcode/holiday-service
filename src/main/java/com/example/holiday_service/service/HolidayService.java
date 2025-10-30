package com.example.holiday_service.service;


import com.example.holiday_service.dto.external.NagerHolidayDTO;
import com.example.holiday_service.dto.internal.HolidayCheckDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

    private static final Logger log = LoggerFactory.getLogger(HolidayService.class);

    private final RestTemplate restTemplate;
    private final String nagerApiBaseUrl;

    @Autowired
    public HolidayService(RestTemplate restTemplate, @Value("${api.nager.url}") String nagerApiBaseUrl) {
        this.restTemplate = restTemplate;
        this.nagerApiBaseUrl = nagerApiBaseUrl;
    }

    public HolidayCheckDTO checkHoliday(String dateString, String countryCode) {
        // 1. Validate and parse the date to extract the year
        String year;
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
            year = String.valueOf(date.getYear());
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format received: {}", dateString);
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD.");
        }

        // 2. Fetch all holidays for the year and country
        List<NagerHolidayDTO> holidaysForYear = getHolidaysFromApi(year, countryCode);

        // 3. Search the list for the specific date
        Optional<NagerHolidayDTO> foundHoliday = holidaysForYear.stream()
                .filter(holiday -> holiday.getDate().equals(dateString))
                // Optionally filter by type if needed (e.g., only "Public")
                // .filter(holiday -> "Public".equalsIgnoreCase(holiday.getType()))
                .findFirst();

        // 4. Create the response DTO
        if (foundHoliday.isPresent()) {
            log.info("Date {} IS a public holiday in {}: {}", dateString, countryCode, foundHoliday.get().getName());
            return new HolidayCheckDTO(dateString, countryCode, true, foundHoliday.get().getName());
        } else {
            log.info("Date {} is NOT a public holiday in {}", dateString, countryCode);
            return new HolidayCheckDTO(dateString, countryCode, false, null);
        }
    }


    private List<NagerHolidayDTO> getHolidaysFromApi(String year, String countryCode) {
        String url = UriComponentsBuilder.fromHttpUrl(nagerApiBaseUrl)
                .pathSegment(year, countryCode) // Appends /{year}/{countryCode}
                .toUriString();

        log.debug("Calling Nager API: {}", url);
        try {
            NagerHolidayDTO[] response = restTemplate.getForObject(url, NagerHolidayDTO[].class);
            return (response != null) ? Arrays.asList(response) : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            // Handle specific HTTP errors (like 404 Not Found if country code is invalid)
            log.error("HTTP error calling Nager API for {}/{}: {} - {}", year, countryCode, e.getStatusCode(), e.getResponseBodyAsString());
            // Depending on requirements, you might want to re-throw or handle differently
            return Collections.emptyList();
        } catch (RestClientException e) {
            // Handle other errors (network issues, timeouts, etc.)
            log.error("Error calling Nager API for {}/{}: {}", year, countryCode, e.getMessage());
            // Depending on requirements, you might want to re-throw or handle differently
            return Collections.emptyList();
        }
    }
}
