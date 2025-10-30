package com.example.holiday_service.controller;

import com.example.holiday_service.dto.internal.HolidayCheckDTO;
import com.example.holiday_service.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/holidays")
@Tag(name = "Public Holiday Check", description = "API to check if a date is a public holiday") // Swagger Tag
@CrossOrigin(origins = "*") // Allow frontend access (restrict in production)
public class HolidayController {

    private static final Logger log = LoggerFactory.getLogger(HolidayController.class);

    @Autowired
    private HolidayService holidayService;

    @GetMapping("/check")
    @Operation(summary = "Check for Public Holiday", // Swagger Operation Summary
            description = "Checks if the given date is a public holiday in the specified country using the Nager.Date API.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully checked date"),
                    @ApiResponse(responseCode = "400", description = "Invalid input (e.g., bad date format, missing parameters)"),
                    @ApiResponse(responseCode = "500", description = "Internal server error (e.g., failed to call external API)")
            })
    public ResponseEntity<HolidayCheckDTO> checkHoliday(
            @Parameter(description = "Date to check (YYYY-MM-DD)", required = true, example = "2025-12-25")
            @RequestParam String date,

            @Parameter(description = "2-letter ISO country code (e.g., US, LK, GB)", required = true, example = "US")
            @RequestParam String countryCode) {

        log.info("Received request to check holiday for date: {} in country: {}", date, countryCode);

        // Basic input validation
        if (date == null || date.isBlank() || countryCode == null || countryCode.isBlank()) {
            log.warn("Missing required parameters: date='{}', countryCode='{}'", date, countryCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both 'date' (YYYY-MM-DD) and 'countryCode' parameters are required.");
        }
        if (countryCode.length() != 2) {
            log.warn("Invalid country code format received: {}", countryCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "countryCode must be a 2-letter ISO code.");
        }

        try {
            HolidayCheckDTO result = holidayService.checkHoliday(date, countryCode.toUpperCase());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Catches date format errors or potentially other validation errors from the service
            log.warn("Bad request processed for date '{}', country '{}': {}", date, countryCode, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            // Catch broader errors (like API failures within the service)
            log.error("Internal error processing request for date '{}', country '{}': {}", date, countryCode, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred while checking the holiday.");
        }
    }
}