package diouane.hicham.ebanking.dtos;

import lombok.*;
import diouane.hicham.ebanking.enums.AccountStatus;

import java.util.Date;
import java.util.List;

@Data
public class SavingBankAccountDTO extends BankAccountDTO {
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;
}
