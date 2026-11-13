## Neo4flix

### Overview

In this project, you will build a **movie recommendation engine** called **Neo4flix**, leveraging **Neo4j**, **Spring Boot**, **Angular**, **microservices**, and **Docker**.  
The goal is to design and deploy a complete system capable of recommending movies based on user interactions, preferences, and relationships between movies, users, and ratings.

This project combines **graph database modeling**, **microservices architecture**, **secure backend development**, and **frontend UI design** into one cohesive application.

### Role Play

You are a **full-stack engineer** working for a media streaming platform aiming to create a personalized movie recommendation system.  
Your mission is to design and develop the full architecture — from graph data modeling in **Neo4j**, to microservices and APIs in **Spring Boot**, to an intuitive user interface in **Angular**, all containerized and deployed with **Docker**.

### Learning Objectives

- Design **graph data models** using **Neo4j** (nodes, relationships, Cypher queries)
- Develop **microservices** for movies, users, ratings, and recommendations
- Implement secure authentication with **OAuth2** or **JWT**
- Create a **responsive Angular frontend** for movie browsing and recommendations
- Use **Docker** for containerization and microservice deployment
- Apply **HTTPS encryption** and strong **security practices**
- Conduct **functional**, **usability**, and **security** testing

### Instructions

#### 1. Microservices Development

Develop independent microservices using **Spring Boot**, each handling a specific part of the recommendation engine:

- **Movie Microservice**  
  Handles reading and writing movie data, including title, genre, and release information.  
  Generates movie recommendations based on user ratings and related movie nodes in Neo4j.

- **User Microservice**  
  Manages user information and their rating history.  
  Handles account creation, authentication, and user profile management.

- **Rating Microservice**  
  Manages CRUD operations for movie ratings and connects users with rated movies.  
  Stores rating values and timestamps.

- **Recommendation Microservice**  
  Uses graph traversal queries in Neo4j to generate personalized recommendations.  
  Provides recommendation endpoints for the frontend.

> All microservices must support **CRUD operations** and be secured with **OAuth2** or **JWT-based authentication**.

#### 2. User Interface (Angular Frontend)

Develop a clean and user-friendly web application using **Angular**.  
The app must include the following pages and functionalities:

- **Login Page:** Secure user login using JWT or OAuth2 tokens.
- **Registration Page:** Allow new users to sign up.
- **Home Page:** Display a list of all movies available.
- **Search Bar:** Enable users to search movies by title, genre, or release year.
- **Movie Details Page:** Show information such as title, release date, genre, and average rating.
- **Rating Page:** Allow users to rate movies and view their own ratings.
- **Recommendations Page:** Display personalized recommendations based on user activity.

**Functionalities to include:**

- Search movies by multiple criteria (title, genre, release date, etc.)
- Rate movies and update ratings
- Filter recommendations by genre or release date
- Save movies to a **watchlist**
- Share movie recommendations with friends

#### 3. Security Measures

Implement strong security throughout both backend and frontend components:

- Use **HTTPS** to encrypt all client-server communication (consider **Let’s Encrypt** for SSL certificates).
- Use **OAuth 2.0** or **JWT** for user authentication and role-based access.
- Store user data securely in the database.
- Enforce **strong password policies** (minimum length, complexity, etc.).
- Implement **Two-Factor Authentication (2FA)** for enhanced user account protection.

### Constraints

- Use **Neo4j** as the main graph database.
- Use **Spring Boot** for all backend microservices.
- Use **Angular** for the frontend.
- All services must be **containerized** using **Docker**.
- Authentication must be implemented using **OAuth2** or **JWT**.
- All microservices must communicate via REST APIs.

### Evaluation

Your project will be evaluated based on:

- 🧩 **Architecture**: Proper use of Neo4j, microservices, and Docker
- 🧠 **Functionality**: Correct implementation of recommendation logic and features
- 🔐 **Security**: Robust authentication and HTTPS setup
- 🎨 **Frontend Design**: User-friendly and responsive Angular interface

### Resources

- [Build a Cypher Recommendation Engine](https://neo4j.com/developer/cypher/guide-build-a-recommendation-engine/)
- [Neo4j Documentation](https://neo4j.com/docs/)
- [Neo4j Aura Cloud Console](https://neo4j.com/cloud/platform/aura-graph-database/?ref=neo4j-home-hero)
- [Spring Boot Microservices Guide](https://spring.io/projects/spring-boot)
- [Angular Official Documentation](https://angular.io/docs)
- [Docker Documentation](https://docs.docker.com/)