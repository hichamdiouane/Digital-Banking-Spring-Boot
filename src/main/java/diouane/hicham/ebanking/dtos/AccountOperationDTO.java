package diouane.hicham.ebanking.dtos;

import lombok.Data;
import  diouane.hicham.ebanking.enums.OperationType;

import java.util.Date;
@Data
public class AccountOperationDTO {
    private Long id;
    private BankAccountDTO bankAccountDTO;
    private Date operationDate;
    private double amount;
    private OperationType type;
    private String description;
}