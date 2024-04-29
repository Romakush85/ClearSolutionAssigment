package com.clearsolution.testassigment.models.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
        @NotNull
        private Long id;
        @NotEmpty(message = "Email should not be empty")
        @Email(message = "Email is incorrect")
        @Schema(example = "example@somemail.com", type="string")
        private String email;
        @NotEmpty(message = "First name should not be empty")
        private String firstName;
        @NotEmpty(message = "Last name should not be empty")
        private String lastName;
        @Past(message="Date must be earlier than current date")
        @Temporal(TemporalType.DATE)
        @DateTimeFormat(iso= DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
        @JsonFormat(pattern="yyyy-MM-dd")
        @Schema(example = "yyyy-MM-dd", type="string")
        private Date birthDate;
        private String address;
        @Pattern(regexp="^(|\\+\\d{12})$", message="Phone number should be empty or starts from '+' and contains only 12 digits")
        private String phoneNumber;

        private UserDTO(Builder builder) {
                setId(builder.id);
                setEmail(builder.email);
                setFirstName(builder.firstName);
                setLastName(builder.lastName);
                setBirthDate(builder.birthDate);
                setAddress(builder.address);
                setPhoneNumber(builder.phoneNumber);
        }


        public static final class Builder {
                private Long id;
                private String email;
                private String firstName;
                private String lastName;
                private Date birthDate;
                private String address;
                private String phoneNumber;

                public Builder() {
                }

                public Builder id(Long val) {
                        id = val;
                        return this;
                }

                public Builder email(String val) {
                        email = val;
                        return this;
                }

                public Builder firstName(String val) {
                        firstName = val;
                        return this;
                }

                public Builder lastName(String val) {
                        lastName = val;
                        return this;
                }

                public Builder birthDate(Date val) {
                        birthDate = val;
                        return this;
                }

                public Builder address(String val) {
                        address = val;
                        return this;
                }

                public Builder phoneNumber(String val) {
                        phoneNumber = val;
                        return this;
                }

                public UserDTO build() {
                        return new UserDTO(this);
                }
        }
}
