package com.edutech.edutechbackend.entity;


public enum Gender {

    MALE,
    FEMALE,
    OTHER;
    // ↑ only these three values are allowed
    //   trying to pass anything else → validation error
    //   stored in DB as string: "MALE", "FEMALE", "OTHER"
}