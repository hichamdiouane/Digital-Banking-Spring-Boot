package diouane.hicham.ebanking.web;

import diouane.hicham.ebanking.dtos.AccountOperationDTO;
import diouane.hicham.ebanking.exceptions.BalanceNotSufficientException;
import diouane.hicham.ebanking.exceptions.BankAccountNotFoundException;
import diouane.hicham.ebanking.services.BankAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class OperationRestController {
    private BankAccountService bankAccountService;
    //new operation
    @PostMapping("/operations")
    public AccountOperationDTO saveOperation(@RequestBody AccountOperationDTO accountOperationDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        //check if the account exists
        // get auth user
        System.out.println("accountOperationDTO ==>>>>>>>>>>>>>>>> " + accountOperationDTO);
        return bankAccountService.saveOperation(accountOperationDTO);
    }
}
