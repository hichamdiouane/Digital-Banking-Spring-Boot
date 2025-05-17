package diouane.hicham.ebanking.dtos;

import lombok.Data;
import diouane.hicham.ebanking.enums.AccountStatus;

import java.util.Date;

@Data
public class CurrentBankAccountDTO extends BankAccountDTO {
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double overDraft;
}
