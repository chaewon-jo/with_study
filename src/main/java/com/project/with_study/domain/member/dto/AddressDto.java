package com.project.with_study.domain.member.dto;

import com.project.with_study.domain.member.entity.Address;
import jakarta.validation.constraints.NotBlank;

public record AddressDto(
        @NotBlank String base,
        @NotBlank String detail,
        @NotBlank String postalCode
) {
    public Address toAddress() {
        return Address.builder()
                .base(base)
                .detail(detail)
                .postalCode(postalCode)
                .build();
    }

    public static AddressDto from(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressDto(address.getBase(), address.getDetail(), address.getPostalCode());
    }
}
