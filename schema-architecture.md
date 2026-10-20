# Architecture summary
This Spring Boot application uses both MVC and REST controllers.
Thymeleaf templates are used for the dashboards, and REST APIs serve all other modules.
The application interacts with two databases:
* MySQL (for patient, doctor, appointment, and admin data)
* MongoDB (for prescriptions)
All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories.
MySQL uses JPA entities while MongoDB uses document models.

# Numbered flow of data and control

1. User accesses AdminDashboard or Appointment pages.
2. The action is routed to the appropriate Thymeleaf or REST controller.
3. The controller calls the service layer.
4. The service layer access the DBs.
5. The service layer sends a REST response.