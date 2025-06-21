package cc.martincao.rentigo.rentigobackend.vehicle.controller;

import cc.martincao.rentigo.rentigobackend.vehicle.dto.VehicleDTO;
import cc.martincao.rentigo.rentigobackend.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<VehicleDTO>> listVehicles(@RequestParam(required = false) Short locationId) {
        if (locationId != null) {
            return ResponseEntity.ok(vehicleService.listVehicles(locationId));
        } else {
            return ResponseEntity.ok(vehicleService.listAllVehicles());
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VehicleDTO> addVehicle(@RequestBody VehicleDTO vehicleDTO) {
        return ResponseEntity.ok(vehicleService.addVehicle(vehicleDTO));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VehicleDTO> updateVehicle(@PathVariable Long id, @RequestBody VehicleDTO vehicleDTO) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, vehicleDTO));
    }

    @DeleteMapping("/remove/{id}")
    @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> removeVehicle(@PathVariable Long id) {
        vehicleService.removeVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
