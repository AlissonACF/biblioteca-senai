package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.BibliotecaDtos.BootstrapResponse;
import com.example.demo.dto.BibliotecaDtos.ReservationDecisionRequest;
import com.example.demo.dto.BibliotecaDtos.ReservationRequest;
import com.example.demo.dto.BibliotecaDtos.ReservationResponse;
import com.example.demo.dto.BibliotecaDtos.ResourceRequest;
import com.example.demo.dto.BibliotecaDtos.ResourceResponse;
import com.example.demo.dto.BibliotecaDtos.UserRequest;
import com.example.demo.dto.BibliotecaDtos.UserResponse;
import com.example.demo.entity.RecursoBiblioteca;
import com.example.demo.entity.ReservaBiblioteca;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.repository.RecursoBibliotecaRepository;
import com.example.demo.repository.ReservaBibliotecaRepository;
import com.example.demo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class BibliotecaService {

    private final UsuarioRepository usuarioRepository;
    private final RecursoBibliotecaRepository recursoRepository;
    private final ReservaBibliotecaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;

    public BibliotecaService(
        UsuarioRepository usuarioRepository,
        RecursoBibliotecaRepository recursoRepository,
        ReservaBibliotecaRepository reservaRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.recursoRepository = recursoRepository;
        this.reservaRepository = reservaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public BootstrapResponse bootstrap() {
        return new BootstrapResponse(listUsers(), listResources(), listReservations());
    }

    public List<UserResponse> listUsers() {
        return usuarioRepository.findAll().stream()
            .sorted(Comparator.comparing(Usuario::getNome, Comparator.nullsLast(String::compareToIgnoreCase)))
            .map(this::toUserResponse)
            .toList();
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.name().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.password()));
        usuario.setNivelAcesso("admin".equals(request.role()) ? NivelAcesso.ADMIN : NivelAcesso.PADRAO);
        return toUserResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void removeUser(Long id) {
        usuarioRepository.softDeleteById(id);
    }

    public List<ResourceResponse> listResources() {
        return recursoRepository.findAll().stream()
            .sorted(Comparator.comparing(RecursoBiblioteca::getKind).thenComparing(RecursoBiblioteca::getName))
            .map(this::toResourceResponse)
            .toList();
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        RecursoBiblioteca recurso = new RecursoBiblioteca();
        recurso.setKind(request.kind());
        recurso.setName(request.name().trim());
        recurso.setCapacity(Math.max(1, request.capacity()));
        return toResourceResponse(recursoRepository.save(recurso));
    }

    @Transactional
    public void removeResource(Long id) {
        recursoRepository.softDeleteById(id);
        reservaRepository.findAll().stream()
            .filter(r -> r.getRecurso().getId().equals(id))
            .filter(r -> List.of(
                ReservationStatus.pending_approval,
                ReservationStatus.approved,
                ReservationStatus.checked_in
            ).contains(r.getStatus()))
            .forEach(r -> r.setStatus(ReservationStatus.cancelled));
    }

    public List<ReservationResponse> listReservations() {
        return reservaRepository.findAll().stream()
            .sorted(Comparator.comparing(ReservaBiblioteca::getDate).reversed()
                .thenComparing(ReservaBiblioteca::getStartHour).reversed())
            .map(this::toReservationResponse)
            .toList();
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, Usuario currentUser) {
        RecursoBiblioteca recurso = recursoRepository.findById(parseId(request.resourceId(), "resource"))
            .filter(RecursoBiblioteca::isAtivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso indisponivel."));

        int people = Math.max(1, request.people());
        if (people > recurso.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacidade entre 1 e " + recurso.getCapacity() + " pessoas.");
        }

        LocalDateTime start = request.date().atTime(request.startHour(), 0);
        if (start.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nao e possivel reservar no passado.");
        }

        if (!reservaRepository.findActiveConflicts(recurso.getId(), request.date(), request.startHour()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horario ja reservado.");
        }

        Usuario targetUser = currentUser;
        if (request.userId() != null && !request.userId().isBlank() && currentUser.getNivelAcesso() == NivelAcesso.ADMIN) {
            targetUser = usuarioRepository.findById(parseId(request.userId(), "user"))
                .filter(Usuario::isAtivo)
                .orElse(currentUser);
        }

        ReservaBiblioteca reserva = new ReservaBiblioteca();
        reserva.setUsuario(targetUser);
        reserva.setRecurso(recurso);
        reserva.setUserName(targetUser.getNome());
        reserva.setResourceName(recurso.getName());
        reserva.setResourceKind(recurso.getKind());
        reserva.setDate(request.date());
        reserva.setStartHour(request.startHour());
        reserva.setPeople(people);
        reserva.setStatus(currentUser.getNivelAcesso() == NivelAcesso.ADMIN || people == 1
            ? ReservationStatus.approved
            : ReservationStatus.pending_approval);

        return toReservationResponse(reservaRepository.save(reserva));
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id, boolean force) {
        ReservaBiblioteca reserva = getReservation(id);
        ensureStatus(reserva, List.of(ReservationStatus.pending_approval, ReservationStatus.approved, ReservationStatus.checked_in),
            "Esta reserva nao pode mais ser cancelada.");
        if (!force && minutesToStart(reserva) < 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelamento permitido apenas ate 30 minutos antes.");
        }
        reserva.setStatus(ReservationStatus.cancelled);
        return toReservationResponse(reserva);
    }

    @Transactional
    public ReservationResponse decideReservation(Long id, ReservationDecisionRequest request, Usuario admin) {
        ReservaBiblioteca reserva = getReservation(id);
        boolean approve = Boolean.TRUE.equals(request.approve());
        reserva.setStatus(approve ? ReservationStatus.approved : ReservationStatus.rejected);
        reserva.setApprovedBy(admin.getId());
        reserva.setDecidedAt(LocalDateTime.now());
        reserva.setRejectionReason(approve ? null : request.reason());
        return toReservationResponse(reserva);
    }

    @Transactional
    public ReservationResponse confirmReservation(Long id) {
        ReservaBiblioteca reserva = getReservation(id);
        ensureStatus(reserva, List.of(ReservationStatus.approved), "Apenas reservas aprovadas podem ser confirmadas.");
        if (reserva.getConfirmedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reserva ja confirmada.");
        }
        if (minutesToStart(reserva) < 60) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmacao deve ser feita ate 1 hora antes do horario.");
        }
        reserva.setConfirmedAt(LocalDateTime.now());
        return toReservationResponse(reserva);
    }

    @Transactional
    public ReservationResponse checkIn(Long id, boolean force) {
        ReservaBiblioteca reserva = getReservation(id);
        ensureStatus(reserva, List.of(ReservationStatus.approved), "Apenas reservas aprovadas podem fazer check-in.");
        if (!force && minutesToStart(reserva) < 60) {
            reserva.setStatus(ReservationStatus.expired);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in deve ser feito ate 1 hora antes do horario. A reserva expirou.");
        }
        reserva.setStatus(ReservationStatus.checked_in);
        reserva.setCheckInAt(LocalDateTime.now());
        if (reserva.getConfirmedAt() == null) {
            reserva.setConfirmedAt(LocalDateTime.now());
        }
        return toReservationResponse(reserva);
    }

    @Transactional
    public ReservationResponse checkOut(Long id) {
        ReservaBiblioteca reserva = getReservation(id);
        ensureStatus(reserva, List.of(ReservationStatus.checked_in), "Faca check-in primeiro.");
        reserva.setStatus(ReservationStatus.completed);
        reserva.setCheckOutAt(LocalDateTime.now());
        return toReservationResponse(reserva);
    }

    @Transactional
    public void sweepNoShows() {
        reservaRepository.findAll().stream()
            .filter(r -> r.getStatus() == ReservationStatus.approved)
            .filter(r -> minutesToStart(r) < 60)
            .forEach(r -> r.setStatus(ReservationStatus.expired));
    }

    public Usuario findCurrentUser(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado."));
    }

    private ReservaBiblioteca getReservation(Long id) {
        return reservaRepository.findById(id)
            .filter(ReservaBiblioteca::isAtivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva nao encontrada."));
    }

    private void ensureStatus(ReservaBiblioteca reserva, List<ReservationStatus> allowed, String message) {
        if (!allowed.contains(reserva.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private long minutesToStart(ReservaBiblioteca reserva) {
        return java.time.Duration.between(LocalDateTime.now(), reserva.getDate().atTime(reserva.getStartHour(), 0)).toMinutes();
    }

    private Long parseId(String raw, String prefix) {
        String digits = raw.replace(prefix + "-", "");
        try {
            return Long.valueOf(digits);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id invalido.");
        }
    }

    private UserResponse toUserResponse(Usuario usuario) {
        return new UserResponse(
            "user-" + usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            "",
            usuario.getNivelAcesso() == NivelAcesso.ADMIN ? "admin" : "user"
        );
    }

    private ResourceResponse toResourceResponse(RecursoBiblioteca recurso) {
        return new ResourceResponse(
            "resource-" + recurso.getId(),
            recurso.getKind(),
            recurso.getName(),
            recurso.getCapacity(),
            recurso.isAtivo()
        );
    }

    private ReservationResponse toReservationResponse(ReservaBiblioteca reserva) {
        return new ReservationResponse(
            "reservation-" + reserva.getId(),
            "user-" + reserva.getUsuario().getId(),
            reserva.getUserName(),
            "resource-" + reserva.getRecurso().getId(),
            reserva.getResourceName(),
            reserva.getResourceKind(),
            reserva.getDate(),
            reserva.getStartHour(),
            reserva.getPeople(),
            reserva.getStatus(),
            reserva.getCreatedAt(),
            reserva.getApprovedBy() == null ? null : "user-" + reserva.getApprovedBy(),
            reserva.getDecidedAt(),
            reserva.getCheckInAt(),
            reserva.getCheckOutAt(),
            reserva.getConfirmedAt(),
            reserva.getRejectionReason()
        );
    }
}
