### Summary

This Java 11 application uses Spring Boot 2.7.10, PostgreSQL, and NGINX to allow users to book rooms in a hotel.

### Functionality

The application provides users with the ability to:

- View a list of all current bookings
- Make a new booking with a guest name, check-in date, and check-out date
- Check the availability of rooms for a given set of dates
- Cancel an existing booking
- Modify an existing booking

### Requirements

To run the application, you must meet the following requirements:

- Docker must be installed on the machine you will be using to run the application.
- Ports 80, 443, 8080, and 5432 must be available.
- An internet connection is required as Docker will download the project dependencies before starting the application..

### Run application

To start the application, navigate to the root folder in your terminal and run:

```bash
docker-compose -p alten-project up
```

Be sure to include the flag **'p alten-project'**  to ensure that the containers are named correctly.

To stop the application, run:

```bash
docker-compose -p alten-project down
```

### Endpoints

- **'GET localhost:80/api/v1/bookings'**:  Retrieves a list of all current bookings
- **'POST localhost:80/api/v1/bookings/add'**: Creates a new booking with the given guest name, check-in date, and check-out
  date. Example body:

```json
{
  "guestName": "New Alten guest",
  "checkInDate": "2023-04-24",
  "checkOutDate": "2023-04-26"
}
```

- **'GET localhost:80/api/v1/bookings/availability'**:  Retrieves the availability of rooms for the given dates
- **'CANCEL localhost:80/api/v1/bookings/cancel/{reservationId}'**: Cancels the booking with the given reservation ID
- **'PUT localhost:80/api/v1/bookings/modify/{reservationId}'**: Modifies the booking with the given reservation ID

You can find additional examples in the postman collection located in the postman collection folder.

### Additional Information

If you have any questions, please don't hesitate to contact me at [abrilrdev@gmail.com](mailto:abrilrdev@gmail.com).
