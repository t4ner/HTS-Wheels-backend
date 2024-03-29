package com.tobeto.rentacar.services.concretes;

import com.tobeto.rentacar.config.modelmapper.ModelMapperService;
import com.tobeto.rentacar.core.exceptions.DataNotFoundException;
import com.tobeto.rentacar.core.result.DataResult;
import com.tobeto.rentacar.core.result.Result;
import com.tobeto.rentacar.core.result.SuccessResult;

import com.tobeto.rentacar.entities.concretes.Customer;
import com.tobeto.rentacar.entities.concretes.*;

import com.tobeto.rentacar.repository.*;

import com.tobeto.rentacar.services.abstracts.RentalService;
import com.tobeto.rentacar.services.constans.rental.RentalMessages;
import com.tobeto.rentacar.services.dtos.requests.rental.CreateRentalRequests;
import com.tobeto.rentacar.services.dtos.requests.rental.DeleteRentalRequests;
import com.tobeto.rentacar.services.dtos.requests.rental.UpdateRentalRequests;
import com.tobeto.rentacar.services.dtos.responses.rental.GetAllRentalResponses;
import com.tobeto.rentacar.services.dtos.responses.rental.GetByIdRentalResponses;
import com.tobeto.rentacar.services.dtos.stripe.StripeChargeDto;
import com.tobeto.rentacar.services.rules.rental.RentalBusinessRules;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalManager implements RentalService {
    private final RentalRepository rentalRepository;
    private final ModelMapperService modelMapperService;
    private final CarRepository carRepository;
    private final RentalBusinessRules rentalBusinessRules;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public DataResult<List<GetAllRentalResponses>> getAll() {
        List<Rental> rentals = rentalRepository.findAll();
        List<GetAllRentalResponses> getAllRentalResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class)).collect(Collectors.toList());
        return new DataResult<>(getAllRentalResponses, true, RentalMessages.RENTALS_LISTED);
    }

    @Override
    public DataResult<GetByIdRentalResponses> getById(int id) {

        Rental rental = rentalRepository.findById(id).orElseThrow(() -> new DataNotFoundException(RentalMessages.DATA_NOT_FOUND));
        GetByIdRentalResponses getByIdRentalResponses = this.modelMapperService.forResponse()
                .map(rental, GetByIdRentalResponses.class);

        return new DataResult<>(getByIdRentalResponses, true, RentalMessages.RENTALS_LISTED);
    }

    @Override
    public Result add(CreateRentalRequests createRentalRequests) {

        this.rentalBusinessRules.checkIfDate(createRentalRequests);

        Rental rental = this.modelMapperService.forRequest()
                .map(createRentalRequests, Rental.class);

        Car car = this.carRepository.findById(createRentalRequests.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        Employee employee = this.employeeRepository.findById(createRentalRequests.getEmployeeId())
                .orElse(null);

        Customer customer = this.customerRepository.findById(createRentalRequests.getCustomerId())
                .orElse(null);

        rental.setCar(car);
        rental.setEmployee(employee);
        rental.setCustomer(customer);

        int rentalLimit = rental.getStartDate().until(rental.getEndDate()).getDays() + 1;
        rental.setTotalPrice(car.getPrice() * rentalLimit);

        this.rentalRepository.save(rental);

        return new SuccessResult(RentalMessages.RENTAL_ADDED);
    }

    @Override
    public Result update(UpdateRentalRequests updateRentalRequests) {
        Rental rental = this.modelMapperService.forRequest().map(updateRentalRequests, Rental.class);
        rental.setId(updateRentalRequests.getId());
        rental.setDiscount(updateRentalRequests.getDiscount());
        rental.setStartKilometer(updateRentalRequests.getStartKilometer());
        rental.setEndKilometer(updateRentalRequests.getEndKilometer());
        rental.setStartDate(updateRentalRequests.getStartDate());
        rental.setEndDate(updateRentalRequests.getEndDate());
        rental.setReturnDate(updateRentalRequests.getReturnDate());
        rental.setTotalPrice(updateRentalRequests.getTotalPrice());

        this.rentalRepository.save(rental);

        return new SuccessResult(RentalMessages.RENTAL_UPDATED);
    }

    @Override
    public Result delete(DeleteRentalRequests deleteRentalRequests) {
        Rental rental = this.modelMapperService.forRequest().map(deleteRentalRequests, Rental.class);
        this.rentalRepository.delete(rental);

        return new SuccessResult(RentalMessages.RENTAL_DELETED);
    }

    @Override
    public List<GetAllRentalResponses> findByStartDate(LocalDate startDate) {
        List<Rental> rentals = rentalRepository.findByStartDate(startDate);
        List<GetAllRentalResponses> findByStartDateResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByStartDateResponses;
    }

    @Override
    public List<GetAllRentalResponses> findByEndDate(LocalDate endDate) {
        List<Rental> rentals = rentalRepository.findByEndDate(endDate);
        List<GetAllRentalResponses> findByEndDateResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByEndDateResponses;
    }

    @Override
    public List<GetAllRentalResponses> findByReturnDate(LocalDate returnDate) {
        List<Rental> rentals = rentalRepository.findByReturnDate(returnDate);
        List<GetAllRentalResponses> findByReturnDateResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByReturnDateResponses;
    }

    @Override
    public List<GetAllRentalResponses> findByStartKilometer(int startKilometer) {
        List<Rental> rentals = rentalRepository.findByStartKilometer(startKilometer);
        List<GetAllRentalResponses> findByStartKilometerResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByStartKilometerResponses;
    }

    @Override
    public List<GetAllRentalResponses> findByEndKilometer(int endKilometer) {
        List<Rental> rentals = rentalRepository.findByEndKilometer(endKilometer);
        List<GetAllRentalResponses> findByEndKilometerResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByEndKilometerResponses;
    }

    @Override
    public List<GetAllRentalResponses> findByTotalPrice(double totalPrice) {
        List<Rental> rentals = rentalRepository.findByTotalPrice(totalPrice);
        List<GetAllRentalResponses> findByTotalPriceResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByTotalPriceResponses;
    }

    @Override
    public List<GetAllRentalResponses> findByDiscount(double discount) {
        List<Rental> rentals = rentalRepository.findByDiscount(discount);
        List<GetAllRentalResponses> findByDiscountResponses = rentals.stream()
                .map(rental -> this.modelMapperService.forResponse()
                        .map(rental, GetAllRentalResponses.class))
                .collect(Collectors.toList());

        return findByDiscountResponses;
    }
}
