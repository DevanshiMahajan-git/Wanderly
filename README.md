# Wanderly — Your Journey, Beautifully Planned

A Java 17 + Spring Boot itinerary planner for the Jenkins-based DevOps assignment.

## Features
- Dashboard with itinerary totals, search, and status workflow
- Create, edit, and delete trips
- H2 database; Thymeleaf frontend
- Dockerfile, Jenkinsfile, Ansible starter, and 15 task-report templates

## Run locally
Install Java 17 and Maven, then run `mvn spring-boot:run`; open http://localhost:8080.

## Run with Docker
`docker build -t wanderly:0.1.0 .` then `docker run --rm -p 8080:8080 wanderly:0.1.0`.

The role selector is an educational workflow demonstration, not production authentication.
