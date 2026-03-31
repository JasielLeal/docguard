package br.com.harmony.DocGuard.application.services.enterprise.me;

import br.com.harmony.DocGuard.domain.model.Enterprise;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MeResponse {

    private final String legalName;
    private final String tradeName;
    private final String cnpj;
    private final String registrationStatus;
    private final LocalDate activityStartDate;
    private final String legalNature;
    private final String companySize;
    private final String primaryCnae;

    // address
    private final String number;
    private final String street;
    private final String complement;
    private final String district;
    private final String zipCode;
    private final String city;
    private final String state;

    // contact
    private final String email;
    private final String phone;

    public MeResponse(Enterprise enterprise) {
        this.legalName = enterprise.getLegalName();
        this.tradeName = enterprise.getTradeName();
        this.cnpj = enterprise.getCnpj();
        this.registrationStatus = enterprise.getRegistrationStatus();
        this.activityStartDate = enterprise.getActivityStartDate();
        this.legalNature = enterprise.getLegalNature();
        this.companySize = enterprise.getCompanySize();
        this.primaryCnae = enterprise.getPrimaryCnae();

        this.street = enterprise.getStreet();
        this.number = enterprise.getNumber();
        this.complement = enterprise.getComplement();
        this.district = enterprise.getDistrict();
        this.zipCode = enterprise.getZipCode();
        this.city = enterprise.getCity();
        this.state = enterprise.getState();

        this.email = enterprise.getEmail();
        this.phone = enterprise.getPhone();
    }
}