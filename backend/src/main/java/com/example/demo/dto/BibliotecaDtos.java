package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.enums.ReservationStatus;
import com.example.demo.enums.ResourceKind;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BibliotecaDtos {

    public record UserResponse(
        String id,
        String name,
        String email,
        String password,
        String role
    ) {
    }

    public record UserRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String role
    ) {
    }

    public record ResourceResponse(
        String id,
        ResourceKind kind,
        String name,
        Integer capacity,
        Boolean active
    ) {
    }

    public record ResourceRequest(
        @NotNull ResourceKind kind,
        @NotBlank String name,
        @Min(1) Integer capacity
    ) {
    }

    public record ReservationResponse(
        String id,
        String userId,
        String userName,
        String resourceId,
        String resourceName,
        ResourceKind resourceKind,
        LocalDate date,
        Integer startHour,
        Integer people,
        ReservationStatus status,
        LocalDateTime createdAt,
        String approvedBy,
        LocalDateTime decidedAt,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        LocalDateTime confirmedAt,
        String rejectionReason
    ) {
    }

    public record ReservationRequest(
        @NotBlank String resourceId,
        @NotNull LocalDate date,
        @NotNull Integer startHour,
        @Min(1) Integer people,
        String userId
    ) {
    }

    public record ReservationDecisionRequest(Boolean approve, String reason) {
    }

    public record BootstrapResponse(
        List<UserResponse> users,
        List<ResourceResponse> resources,
        List<ReservationResponse> reservations
    ) {
    }
}
