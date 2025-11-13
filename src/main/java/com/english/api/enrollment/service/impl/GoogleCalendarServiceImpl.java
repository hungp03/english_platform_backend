package com.english.api.enrollment.service.impl;

import com.english.api.enrollment.model.StudyPlanSchedule;
import com.english.api.enrollment.service.GoogleCalendarService;
import com.english.api.user.model.UserOAuth2Token;
import com.english.api.user.repository.UserOAuth2TokenRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private static final String APPLICATION_NAME = "English Learning Platform";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final UserOAuth2TokenRepository tokenRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Override
    public String createCalendarEvent(StudyPlanSchedule schedule, UUID userId) {
        try {
            Calendar service = getCalendarService(userId);
            if (service == null) {
                log.warn("Cannot create calendar event - user {} has no valid OAuth2 token", userId);
                return null;
            }

            Event event = buildEventFromSchedule(schedule);
            Event createdEvent = service.events().insert("primary", event).execute();

            log.info("Created Google Calendar event {} for schedule {}", createdEvent.getId(), schedule.getId());
            return createdEvent.getId();

        } catch (Exception e) {
            log.error("Error creating calendar event for schedule {}", schedule.getId(), e);
            return null;
        }
    }

    @Override
    public void updateCalendarEvent(StudyPlanSchedule schedule, UUID userId) {
        try {
            if (schedule.getGoogleCalendarEventId() == null) {
                log.warn("Cannot update calendar event - schedule {} has no event ID", schedule.getId());
                return;
            }

            Calendar service = getCalendarService(userId);
            if (service == null) {
                log.warn("Cannot update calendar event - user {} has no valid OAuth2 token", userId);
                return;
            }

            Event event = buildEventFromSchedule(schedule);
            service.events().update("primary", schedule.getGoogleCalendarEventId(), event).execute();

            log.info("Updated Google Calendar event {} for schedule {}", schedule.getGoogleCalendarEventId(), schedule.getId());

        } catch (Exception e) {
            log.error("Error updating calendar event for schedule {}", schedule.getId(), e);
        }
    }

    @Override
    public void deleteCalendarEvent(String eventId, UUID userId) {
        try {
            if (eventId == null) {
                return;
            }

            Calendar service = getCalendarService(userId);
            if (service == null) {
                log.warn("Cannot delete calendar event - user {} has no valid OAuth2 token", userId);
                return;
            }

            service.events().delete("primary", eventId).execute();
            log.info("Deleted Google Calendar event {}", eventId);

        } catch (Exception e) {
            log.error("Error deleting calendar event {}", eventId, e);
        }
    }

    @Override
    public boolean isCalendarIntegrationEnabled(UUID userId) {
        return tokenRepository.findByUserIdAndProvider(userId, "GOOGLE").isPresent();
    }

    private Calendar getCalendarService(UUID userId) {
        try {
            UserOAuth2Token token = tokenRepository.findByUserIdAndProvider(userId, "GOOGLE")
                    .orElse(null);

            if (token == null || token.getAccessToken() == null) {
                return null;
            }

            // Check if token is expired or about to expire (within 5 minutes)
            if (token.getTokenExpiresAt() != null &&
                token.getTokenExpiresAt().isBefore(java.time.OffsetDateTime.now().plusMinutes(5))) {

                log.debug("Access token expired for user {}, refreshing...", userId);
                token = refreshAccessToken(token);

                if (token == null) {
                    log.error("Failed to refresh access token for user {}", userId);
                    return null;
                }
            }

            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            @SuppressWarnings("deprecation")
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(clientId, clientSecret)
                    .setJsonFactory(JSON_FACTORY)
                    .setTransport(httpTransport)
                    .build()
                    .setAccessToken(token.getAccessToken())
                    .setRefreshToken(token.getRefreshToken());

            return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

        } catch (Exception e) {
            log.error("Error getting calendar service for user {}", userId, e);
            return null;
        }
    }

    private UserOAuth2Token refreshAccessToken(UserOAuth2Token token) {
        try {
            if (token.getRefreshToken() == null) {
                log.error("No refresh token available for user {}", token.getUser().getId());
                return null;
            }

            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            @SuppressWarnings("deprecation")
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(clientId, clientSecret)
                    .setJsonFactory(JSON_FACTORY)
                    .setTransport(httpTransport)
                    .build()
                    .setAccessToken(token.getAccessToken())
                    .setRefreshToken(token.getRefreshToken());

            // Refresh the token
            boolean refreshed = credential.refreshToken();

            if (!refreshed) {
                log.error("Failed to refresh token for user {}", token.getUser().getId());
                return null;
            }

            // Update the token in database
            token.setAccessToken(credential.getAccessToken());
            if (credential.getExpirationTimeMilliseconds() != null) {
                token.setTokenExpiresAt(
                    java.time.OffsetDateTime.now().plusSeconds(
                        (credential.getExpirationTimeMilliseconds() - System.currentTimeMillis()) / 1000
                    )
                );
            }

            tokenRepository.save(token);
            log.info("Successfully refreshed access token for user {}", token.getUser().getId());

            return token;

        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            return null;
        }
    }

    private Event buildEventFromSchedule(StudyPlanSchedule schedule) {
        Event event = new Event()
                .setSummary(schedule.getTaskDesc())
                .setDescription("Study plan task");

        ZonedDateTime startZoned = schedule.getStartTime().atZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime endZoned = startZoned.plusMinutes(schedule.getDurationMin());

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(Date.from(startZoned.toInstant())))
                .setTimeZone(startZoned.getZone().getId());
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(Date.from(endZoned.toInstant())))
                .setTimeZone(endZoned.getZone().getId());
        event.setEnd(end);

        return event;
    }
}
