package com.clearsolution.testassigment.models.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name="users")
@Data
public class UserEntity {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="email")
    @NotEmpty(message = "Email should not be empty")
    @Email(message = "Email is incorrect")
    private String email;
    @NotEmpty(message = "First name should not be empty")
    @Column(name="first_name")
    private String firstName;
    @NotEmpty(message = "Last name should not be empty")
    @Column(name="last_name")
    private String lastName;
    @Past(message="Date must be earlier than current date")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd")
    @Column(name="birth_date")
    private Date birthDate;
    @Column(name="address")
    private String address;
    @Column(name="phone_number")
    @Pattern(regexp="^(|\\+\\d{12})$", message="Phone number should be empty or starts from '+' and contains only 12 digits")
    private String phoneNumber;

}
