package br.com.harmony.DocGuard.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "enterprises")
public class Enterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String legalName;

    private String tradeName;

    @Column(unique = true, nullable = false, length = 14)
    private String cnpj;

    private String registrationStatus;

    private LocalDate activityStartDate;

    private String legalNature;

    private String companySize;

    private String primaryCnae;

    // address
    private String street;
    private String number;
    private String complement;
    private String district;
    private String zipCode;
    private String city;
    private String state;

    // contact
    private String email;
    private String phone;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}