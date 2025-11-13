package com.english.api.enrollment.service;

import com.english.api.enrollment.model.StudyPlanSchedule;

import java.util.UUID;

public interface GoogleCalendarService {
    /**
     * Creates an event in Google Calendar for a study schedule
     *
     * @param schedule the study plan schedule
     * @param userId the user ID
     * @return the Google Calendar event ID
     */
    String createCalendarEvent(StudyPlanSchedule schedule, UUID userId);

    /**
     * Updates an existing event in Google Calendar
     *
     * @param schedule the study plan schedule
     * @param userId the user ID
     */
    void updateCalendarEvent(StudyPlanSchedule schedule, UUID userId);

    /**
     * Deletes an event from Google Calendar
     *
     * @param eventId the Google Calendar event ID
     * @param userId the user ID
     */
    void deleteCalendarEvent(String eventId, UUID userId);

    /**
     * Checks if user has Google Calendar integration enabled
     *
     * @param userId the user ID
     * @return true if enabled, false otherwise
     */
    boolean isCalendarIntegrationEnabled(UUID userId);
}
