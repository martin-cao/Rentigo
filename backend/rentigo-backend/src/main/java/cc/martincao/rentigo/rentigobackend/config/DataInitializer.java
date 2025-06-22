package cc.martincao.rentigo.rentigobackend.config;

import cc.martincao.rentigo.rentigobackend.vehicle.model.Location;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.LocationRepository;
import cc.martincao.rentigo.rentigobackend.vehicle.model.Vehicle;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleStatus;
import cc.martincao.rentigo.rentigobackend.vehicle.model.VehicleType;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.VehicleRepository;
import cc.martincao.rentigo.rentigobackend.vehicle.repository.VehicleTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleRepository vehicleRepository;

    public DataInitializer(LocationRepository locationRepository, 
                          VehicleTypeRepository vehicleTypeRepository,
                          VehicleRepository vehicleRepository) {
        this.locationRepository = locationRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize VehicleTypes if they don't exist
        if (vehicleTypeRepository.count() == 0) {
            VehicleType sedan = new VehicleType();
            sedan.setId((byte) 1);
            sedan.setTypeName("Sedan");
            sedan.setDepositAmount(new BigDecimal("500.00"));
            
            VehicleType suv = new VehicleType();
            suv.setId((byte) 2);
            suv.setTypeName("SUV");
            suv.setDepositAmount(new BigDecimal("800.00"));
            
            VehicleType truck = new VehicleType();
            truck.setId((byte) 3);
            truck.setTypeName("Truck");
            truck.setDepositAmount(new BigDecimal("1000.00"));
            
            vehicleTypeRepository.save(sedan);
            vehicleTypeRepository.save(suv);
            vehicleTypeRepository.save(truck);
        }

        // Initialize Locations if they don't exist
        if (locationRepository.count() == 0) {
            Location location1 = new Location();
            location1.setId(1);
            location1.setCity("Beijing");
            location1.setCenterName("Beijing Central Service Center");
            location1.setAddress("123 Main Street, Beijing");
            location1.setLng(new BigDecimal("116.3974"));
            location1.setLat(new BigDecimal("39.9042"));

            Location location2 = new Location();
            location2.setId(2);
            location2.setCity("Shanghai");
            location2.setCenterName("Shanghai Central Service Center");
            location2.setAddress("456 Nanjing Road, Shanghai");
            location2.setLng(new BigDecimal("121.4737"));
            location2.setLat(new BigDecimal("31.2304"));

            locationRepository.save(location1);
            locationRepository.save(location2);
        }

        // Initialize Vehicles if they don't exist
        if (vehicleRepository.count() == 0) {
            // Get the saved data for relationships
            VehicleType sedan = vehicleTypeRepository.findById((byte) 1).orElse(null);
            VehicleType suv = vehicleTypeRepository.findById((byte) 2).orElse(null);
            Location beijingLocation = locationRepository.findById(1).orElse(null);
            Location shanghaiLocation = locationRepository.findById(2).orElse(null);

            if (sedan != null && beijingLocation != null) {
                Vehicle vehicle1 = new Vehicle();
                vehicle1.setModel("Toyota Camry");
                vehicle1.setLicensePlate("京A12345");
                vehicle1.setColor("White");
                vehicle1.setVehicleType(sedan);
                vehicle1.setLocation(beijingLocation);
                vehicle1.setDailyPrice(new BigDecimal("200.00"));
                vehicle1.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle1);
            }

            if (suv != null && shanghaiLocation != null) {
                Vehicle vehicle2 = new Vehicle();
                vehicle2.setModel("Honda CRV");
                vehicle2.setLicensePlate("沪B67890");
                vehicle2.setColor("Black");
                vehicle2.setVehicleType(suv);
                vehicle2.setLocation(shanghaiLocation);
                vehicle2.setDailyPrice(new BigDecimal("300.00"));
                vehicle2.setStatus(VehicleStatus.AVAILABLE);
                vehicleRepository.save(vehicle2);
            }
        }
    }
}
