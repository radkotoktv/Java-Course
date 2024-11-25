package bg.sofia.uni.fmi.mjt.vehiclerent.vehicle;

import bg.sofia.uni.fmi.mjt.vehiclerent.driver.Driver;
import bg.sofia.uni.fmi.mjt.vehiclerent.exception.InvalidRentingPeriodException;
import bg.sofia.uni.fmi.mjt.vehiclerent.exception.VehicleAlreadyRentedException;
import bg.sofia.uni.fmi.mjt.vehiclerent.exception.VehicleNotRentedException;

import java.time.LocalDateTime;
import java.util.Objects;

public sealed abstract class Vehicle permits Bicycle, Car, Caravan {
    private String id;
    private String model;
    private boolean rented;
    private int tax;
    private LocalDateTime startRentTime;
    public Vehicle(String id, String model) {
        this.id = id;
        this.model = model;
        this.rented = false;
    }

    public void setTax(int tax)
    {
        this.tax = tax;
    }

    public int getTax()
    {
        return this.tax;
    }

    public void setStartRentTime(LocalDateTime startRentTime)
    {
        this.startRentTime = startRentTime;
    }

    public LocalDateTime getStartRentTime()
    {
        return this.startRentTime;
    }

    public void setRented(boolean rented)
    {
        this.rented = rented;
    }

    public boolean getRented()
    {
        return this.rented;
    }

    /**
     * Simulates rental of the vehicle. The vehicle now is considered rented by the provided driver and the start of the rental is the provided date.
     * @param driver the driver that wants to rent the vehicle.
     * @param startRentTime the start time of the rent
     * @throws VehicleAlreadyRentedException in case the vehicle is already rented by someone else or by the same driver.
     */
    public void rent(Driver driver, LocalDateTime startRentTime) {
        try
        {
            if(this.getRented())
            {
                throw new VehicleAlreadyRentedException("This vehicle has already been rented!");
            }
            driver.setRentedVehicle(this);
            setStartRentTime(startRentTime);
            this.setRented(true);
            this.setTax(driver.getGroup().getValue());

        } catch(VehicleAlreadyRentedException e)
        {
            System.out.println("Vehicle has already been rented!");
        }
    }

    /**
     * Simulates end of rental for the vehicle - it is no longer rented by a driver.
     * @param rentalEnd time of end of rental
     * @throws IllegalArgumentException in case @rentalEnd is null
     * @throws VehicleNotRentedException in case the vehicle is not rented at all
     * @throws InvalidRentingPeriodException in case the rentalEnd is before the currently noted start date of rental or
     * in case the Vehicle does not allow the passed period for rental, e.g. Caravans must be rented for at least a day
     * and the driver tries to return them after an hour.
     */
    public void returnBack(LocalDateTime rentalEnd) throws InvalidRentingPeriodException {
        try
        {
            if(rentalEnd == null)
            {
                throw new IllegalArgumentException("rentalEnd is null!");
            }
            if(!this.getRented())
            {
                throw new VehicleNotRentedException("This vehicle has not been rented!");
            }
            this.setRented(false);
        } catch(IllegalArgumentException e) {
            System.out.println("rentalEnd is null");
        } catch(VehicleNotRentedException e) {
            System.out.println("Vehicle not rented");
        }
    }

    /**
     * Used to calculate potential rental price without the vehicle to be rented.
     * The calculation is based on the type of the Vehicle (Car/Caravan/Bicycle).
     *
     * @param startOfRent the beginning of the rental
     * @param endOfRent the end of the rental
     * @return potential price for rent
     * @throws InvalidRentingPeriodException in case the vehicle cannot be rented for that period of time or
     * the period is not valid (end date is before start date)
     */
    public abstract double calculateRentalPrice(LocalDateTime startOfRent, LocalDateTime endOfRent) throws InvalidRentingPeriodException;

}
