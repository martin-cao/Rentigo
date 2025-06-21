package cc.martincao.rentigo.rentigobackend.vehicle.service.impl;

import cc.martincao.rentigo.rentigobackend.vehicle.dto.VehicleDTO;
import cc.martincao.rentigo.rentigobackend.vehicle.model.Location;
import cc.martincao.rentigo.rentigobackend.vehicle.model.Vehicle;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleStatus;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleType;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.LocationRepository;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.VehicleRepository;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.VehicleTypeRepository;
import cc.martincao.rentigo.rentigobackend.vehicle.service.VehicleService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final LocationRepository locationRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, 
                             VehicleTypeRepository vehicleTypeRepository, 
                             LocationRepository locationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    @Cacheable(value = "vehicles", key = "#locationId")
    public List<VehicleDTO> listVehicles(Short locationId) {
        return vehicleRepository.findByLocationIdAndStatus(locationId, VehicleStatus.AVAILABLE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "vehicles", key = "'all'")
    public List<VehicleDTO> listAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleDTO addVehicle(VehicleDTO vehicleDTO) {
        // Validate vehicle type and location exist
        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleDTO.getVehicleTypeId())
                .orElseThrow(() -> new RuntimeException("Vehicle type not found"));
        Location location = locationRepository.findById(vehicleDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        Vehicle vehicle = new Vehicle();
        // Exclude id from copying to prevent issues with auto-generated IDs
        BeanUtils.copyProperties(vehicleDTO, vehicle, "id", "vehicleTypeName", "depositAmount");
        vehicle.setVehicleType(vehicleType);
        vehicle.setLocation(location);
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToDTO(savedVehicle);
    }

    @Override
    @CacheEvict(value = "vehicles", allEntries = true)
    public VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        
        // Validate vehicle type and location exist
        VehicleType vehicleType = vehicleTypeRepository.findById(vehicleDTO.getVehicleTypeId())
                .orElseThrow(() -> new RuntimeException("Vehicle type not found"));
        Location location = locationRepository.findById(vehicleDTO.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        BeanUtils.copyProperties(vehicleDTO, vehicle, "id", "createdAt", "vehicleTypeName", "depositAmount");
        vehicle.setVehicleType(vehicleType);
        vehicle.setLocation(location);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToDTO(savedVehicle);
    }

    @Override
    @CacheEvict(value = "vehicles", allEntries = true)
    public void removeVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    private VehicleDTO convertToDTO(Vehicle vehicle) {
        VehicleDTO dto = new VehicleDTO();
        BeanUtils.copyProperties(vehicle, dto);
        dto.setVehicleTypeId(vehicle.getVehicleType().getId());
        dto.setVehicleTypeName(vehicle.getVehicleType().getTypeName());
        dto.setDepositAmount(vehicle.getVehicleType().getDepositAmount());
        dto.setLocationId(vehicle.getLocation().getId());
        return dto;
    }
}
