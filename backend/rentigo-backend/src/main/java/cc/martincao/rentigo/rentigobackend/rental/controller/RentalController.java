package cc.martincao.rentigo.rentigobackend.rental.controller;

import cc.martincao.rentigo.rentigobackend.rental.dto.RentalRequestDTO;
import cc.martincao.rentigo.rentigobackend.rental.dto.RentalResponseDTO;
import cc.martincao.rentigo.rentigobackend.rental.service.RentalService;
import cc.martincao.rentigo.rentigobackend.user.User;
import cc.martincao.rentigo.rentigobackend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final UserRepository userRepository;

    public RentalController(RentalService rentalService, UserRepository userRepository) {
        this.rentalService = rentalService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RentalResponseDTO> createRental(@RequestBody RentalRequestDTO request) {
        Long userId = getCurrentUserId();
        RentalResponseDTO rental = rentalService.createRental(request, userId);
        return ResponseEntity.ok(rental);
    }

    @PostMapping("/{id}/return")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RentalResponseDTO> returnRental(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        RentalResponseDTO rental = rentalService.returnRental(id, userId);
        return ResponseEntity.ok(rental);
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<RentalResponseDTO>> getMyRentals() {
        Long userId = getCurrentUserId();
        List<RentalResponseDTO> rentals = rentalService.getMyRentals(userId);
        return ResponseEntity.ok(rentals);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<RentalResponseDTO>> getAllRentals() {
        List<RentalResponseDTO> rentals = rentalService.getAllRentals();
        return ResponseEntity.ok(rentals);
    }

    @PostMapping("/{id}/force-finish")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RentalResponseDTO> forceFinishRental(@PathVariable Long id) {
        RentalResponseDTO rental = rentalService.forceFinishRental(id);
        return ResponseEntity.ok(rental);
    }

    @PostMapping("/{id}/activate")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RentalResponseDTO> activateRental(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        RentalResponseDTO rental = rentalService.activateRental(id, userId);
        return ResponseEntity.ok(rental);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
