package cc.martincao.rentigo.rentigobackend.vehicle.service;

import cc.martincao.rentigo.rentigobackend.vehicle.dto.VehicleDTO;

import java.util.List;

public interface VehicleService {
    List<VehicleDTO> listVehicles(Integer locationId);
    
    List<VehicleDTO> listAllVehicles();

    VehicleDTO addVehicle(VehicleDTO vehicleDTO);

    VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO);

    void removeVehicle(Long id);
}
