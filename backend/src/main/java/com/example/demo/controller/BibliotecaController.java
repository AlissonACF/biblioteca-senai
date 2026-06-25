package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.BibliotecaDtos.BootstrapResponse;
import com.example.demo.dto.BibliotecaDtos.ReservationDecisionRequest;
import com.example.demo.dto.BibliotecaDtos.ReservationRequest;
import com.example.demo.dto.BibliotecaDtos.ReservationResponse;
import com.example.demo.dto.BibliotecaDtos.ResourceRequest;
import com.example.demo.dto.BibliotecaDtos.ResourceResponse;
import com.example.demo.dto.BibliotecaDtos.UserRequest;
import com.example.demo.dto.BibliotecaDtos.UserResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.service.BibliotecaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BibliotecaController {

    private final BibliotecaService service;

    public BibliotecaController(BibliotecaService service) {
        this.service = service;
    }

    @GetMapping("/bootstrap")
    public BootstrapResponse bootstrap() {
        return service.bootstrap();
    }

    @GetMapping("/usuarios")
    public List<UserResponse> listUsers() {
        return service.listUsers();
    }

    @Admin
    @PostMapping("/usuarios")
    public UserResponse createUser(@RequestBody @Valid UserRequest request) {
        return service.createUser(request);
    }

    @Admin
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> removeUser(@PathVariable String id) {
        service.removeUser(parseId(id, "user"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resources")
    public List<ResourceResponse> listResources() {
        return service.listResources();
    }

    @Admin
    @PostMapping("/resources")
    public ResourceResponse createResource(@RequestBody @Valid ResourceRequest request) {
        return service.createResource(request);
    }

    @Admin
    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> removeResource(@PathVariable String id) {
        service.removeResource(parseId(id, "resource"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> listReservations() {
        return service.listReservations();
    }

    @PostMapping("/reservations")
    public ReservationResponse createReservation(@RequestBody @Valid ReservationRequest request, Principal principal) {
        return service.createReservation(request, currentUser(principal));
    }

    @PostMapping("/reservations/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable String id, @RequestParam(defaultValue = "false") boolean force) {
        return service.cancelReservation(parseId(id, "reservation"), force);
    }

    @Admin
    @PostMapping("/reservations/{id}/decision")
    public ReservationResponse decideReservation(
        @PathVariable String id,
        @RequestBody ReservationDecisionRequest request,
        Principal principal
    ) {
        return service.decideReservation(parseId(id, "reservation"), request, currentUser(principal));
    }

    @PostMapping("/reservations/{id}/confirm")
    public ReservationResponse confirmReservation(@PathVariable String id) {
        return service.confirmReservation(parseId(id, "reservation"));
    }

    @PostMapping("/reservations/{id}/check-in")
    public ReservationResponse checkIn(@PathVariable String id, @RequestParam(defaultValue = "false") boolean force) {
        return service.checkIn(parseId(id, "reservation"), force);
    }

    @PostMapping("/reservations/{id}/check-out")
    public ReservationResponse checkOut(@PathVariable String id) {
        return service.checkOut(parseId(id, "reservation"));
    }

    @PostMapping("/reservations/sweep")
    public ResponseEntity<Void> sweepNoShows() {
        service.sweepNoShows();
        return ResponseEntity.noContent().build();
    }

    private Usuario currentUser(Principal principal) {
        String email = principal == null ? null : principal.getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((email == null || email.isBlank()) && authentication != null) {
            email = authentication.getName();
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado.");
        }
        return service.findCurrentUser(email);
    }

    private Long parseId(String raw, String prefix) {
        return Long.valueOf(raw.replace(prefix + "-", ""));
    }
}
