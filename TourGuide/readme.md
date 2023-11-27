# Darves-Bornoz_Giovanni_TourGuide
TourGuide,an Openclassrooms project.  
The application suggests attractions to users based on their location.
The rewards are applied based on the attractions visited.

## Technologies
> - Java 17  
> - Maven
> - Spring Boot 3.1.1 
> - JUnit 5.9.3
> - Lombok 1.18.28
> - gpsUtil 1.0.0
> - rewardCentral 1.0.0
> - tripPricer 1.0.0

# Endpoints
## GET
To test the different endpoints, don't forget to create a user. Use the POST endpoint with a tool like Postman.


### Return the location of a user :
http://localhost:8080/getLocation?userName={userName}
* Ex : http://localhost:8080/getLocation?userName=Gio

### Return the five closest attraction for a user :
http://localhost:8080/getNearbyAttractions?userName={userName}
* Ex : http://localhost:8080/getNearbyAttractions?userName=Gio

### Returns the list of attractions visited by a user with reward points earned :
http://localhost:8080/getRewards?userName={userName}
* Ex : http://localhost:8080/getRewards?userName=Gio

### Returns the list of deals won by the user :
http://localhost:8080/getTripDeals?userName={userName}
* Ex : http://localhost:8080/getTripDeals?userName=Gio


## POST
http://localhost:8080/addUser
### JSON Body to create a user for testing endpoints
    { 
        "userId": "5f58253d-6387-4bb2-b772-135b3cd93615",
        "userName": "Gio",
        "phoneNumber": "000-111-2222",
        "emailAddress": "rougeetnoir@email.com" 
    }

# Technical diagram