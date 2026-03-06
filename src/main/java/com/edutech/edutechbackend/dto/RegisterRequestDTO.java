package com.edutech.edutechbackend.dto;


import com.edutech.edutechbackend.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class RegisterRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;


    // ── FIELD: Gender ───────────────────────────────────────────────
    @NotNull(message = "Gender is required")
    // ↑ @NotNull not @NotBlank because Gender is an Enum not a String
    //   @NotBlank only works on Strings
    //   @NotNull works on any object including Enum
    private Gender gender;
    // ↑ client sends "MALE", "FEMALE", or "OTHER" as string in JSON
    //   Jackson (Spring's JSON library) auto-converts to Gender enum
    //   if client sends "INVALID" → 400 Bad Request automatically

    // ── FIELD: Date of Birth ────────────────────────────────────────
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    // ↑ @Past validates that the date is before today
    //   prevents: setting birthday as future date
    //   e.g. "2099-01-01" → 400 Bad Request ❌
    private LocalDate dateOfBirth;
    // ↑ client sends "2000-05-15" as string in JSON
    //   Jackson auto-converts to LocalDate
    //   format must be: "yyyy-MM-dd"
}
