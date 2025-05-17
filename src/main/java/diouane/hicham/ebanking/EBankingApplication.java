package diouane.hicham.ebanking;

import diouane.hicham.ebanking.dtos.BankAccountDTO;
import diouane.hicham.ebanking.dtos.CurrentBankAccountDTO;
import diouane.hicham.ebanking.dtos.CustomerDTO;
import diouane.hicham.ebanking.dtos.SavingBankAccountDTO;
import diouane.hicham.ebanking.entities.AccountOperation;
import diouane.hicham.ebanking.entities.CurrentAccount;
import diouane.hicham.ebanking.entities.Customer;
import diouane.hicham.ebanking.entities.SavingAccount;
import diouane.hicham.ebanking.enums.AccountStatus;
import diouane.hicham.ebanking.enums.OperationType;
import diouane.hicham.ebanking.exceptions.CustomerNotFoundException;
import diouane.hicham.ebanking.repositories.AccountOperationRepository;
import diouane.hicham.ebanking.repositories.BankAccountRepository;
import diouane.hicham.ebanking.repositories.CustomerRepository;
import diouane.hicham.ebanking.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBankingApplication.class, args);
    }

    //Tester la couche dao
    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository) {
        return args -> {
            Stream.of("hicham", "mohamed", "mourad").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(c -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random() * 9000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(c);
                currentAccount.setOverDraft(10000);
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random() * 9000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(c);
                savingAccount.setInterestRate(2.4);
                bankAccountRepository.save(savingAccount);
            });
            bankAccountRepository.findAll().forEach(a -> {
                for (int i = 0; i < 10; i++) {
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 1200);
                    accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
                    accountOperation.setBankAccount(a);
                    accountOperationRepository.save(accountOperation);
                }
            });
        };
    }

    //Tester la couche service
    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService) {
        return args -> {
            Stream.of("hicham","mohamed","mourad").forEach(name -> {
                CustomerDTO customerDTO = new CustomerDTO();
                customerDTO.setName(name);
                customerDTO.setEmail(name + "@gmail.com");
                bankAccountService.saveCustomer(customerDTO);
            });
            bankAccountService.listCustomers().forEach(c -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random()*90000,9000, c.getId());
                    bankAccountService.saveSavingBankAccount(Math.random()*12000, 5.5, c.getId());

                } catch (CustomerNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
            List<BankAccountDTO> bankAccountDTOS = bankAccountService.bankAccountList();
            for(BankAccountDTO bankAccount:bankAccountDTOS){
                for (int i = 0; i < 10; i++) {
                    String accountId;
                    if(bankAccount instanceof SavingBankAccountDTO){
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    }else {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000 + Math.random() * 12000, "Credit");
                    bankAccountService.debit(accountId, 1000 + Math.random() * 1200, "Debit");
                }
            }
        };
    }
}
