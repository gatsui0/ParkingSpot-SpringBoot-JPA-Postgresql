package com.api.parkingcontrol.controllers;


import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {
    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){
        if (parkingSpotService.exitsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License plate car is already in use!");
        }
        if (parkingSpotService.exitsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: parking spot is already in use!");
        }
        if (parkingSpotService.exitsByApartamentAndBlock(parkingSpotDto.getApartament(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: parking spot is already registered for this apartment/block!");
        }

        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotModel>> findAll(){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> findOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModel = parkingSpotService.findById(id);
        if (!parkingSpotModel.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModel.get());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOneParkingSpot(@PathVariable (value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotModel = parkingSpotService.findById(id);
        if (!parkingSpotModel.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        parkingSpotService.delete(parkingSpotModel.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted sucessfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable (value = "id") UUID id,
                                                    @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotModel = parkingSpotService.findById(id);
        if (!parkingSpotModel.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }

        ParkingSpotModel parkingSpotModelUpdate = parkingSpotModel.get();

        parkingSpotModelUpdate.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
        parkingSpotModelUpdate.setApartament(parkingSpotDto.getApartament());
        parkingSpotModelUpdate.setBlock(parkingSpotDto.getBlock());
        parkingSpotModelUpdate.setBrandCar(parkingSpotDto.getBrandCar());
        parkingSpotModelUpdate.setColorCar(parkingSpotDto.getColorCar());
        parkingSpotModelUpdate.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
        parkingSpotModelUpdate.setResponsibleName(parkingSpotDto.getResponsibleName());


        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModelUpdate));
    }
}
