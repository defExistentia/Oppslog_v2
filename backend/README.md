# Operations Logger V2

This is a containerized web application based off of my first application OpsLog.
Opslog was originally written in Java with a JavaFX front end as a desktop application.

## Purpose

Allows for users to log events in operational environments.
Used for shift based work that requires 24/7 365 manning.
This application allow users to track events so that shifts unable to
communicate can track events that happened when they were not present.

## Tech Stack

Kubernetese native application will use modern development frameworks built for
the cloud. Helm charts and Dockerfiles will be included.

This application will use the following stack:
    - Frontend: React
    - Backend: Java + Quarkus.io
    - Database: PostgreSQL

## Authentication

Will eventually include support for:
    - LDAP
    - OIDC
