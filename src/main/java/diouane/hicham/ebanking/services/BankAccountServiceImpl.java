package diouane.hicham.ebanking.services;

import diouane.hicham.ebanking.dtos.*;
import diouane.hicham.ebanking.entities.*;
import diouane.hicham.ebanking.enums.AccountStatus;
import diouane.hicham.ebanking.enums.OperationType;
import diouane.hicham.ebanking.exceptions.BalanceNotSufficientException;
import diouane.hicham.ebanking.exceptions.BankAccountNotFoundException;
import diouane.hicham.ebanking.exceptions.CustomerNotFoundException;
import diouane.hicham.ebanking.mappers.BankAccountMapperImpl;
import diouane.hicham.ebanking.repositories.AccountOperationRepository;
import diouane.hicham.ebanking.repositories.BankAccountRepository;
import diouane.hicham.ebanking.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
//pour le logging
public class BankAccountServiceImpl implements BankAccountService{
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new customer {} to the database", customerDTO.getName());
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer=customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("update customer {} to the database", customerDTO.getName());
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer=customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }
    @Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null) throw new CustomerNotFoundException("Customer not found");

        CurrentAccount currentAccount=new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(initialBalance);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setStatus(AccountStatus.CREATED);
        currentAccount.setCustomer(customer);
        currentAccount.setOverDraft(overDraft);

        CurrentAccount savedBankAccount=bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null) throw new CustomerNotFoundException("Customer not found");

        SavingAccount savingAccount=new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(initialBalance);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setStatus(AccountStatus.CREATED);
        savingAccount.setCustomer(customer);
        savingAccount.setInterestRate(interestRate);
        SavingAccount savedBankAccount=bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(()->new BankAccountNotFoundException("Bank account not found"));
        if(bankAccount instanceof SavingAccount)
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(()->new BankAccountNotFoundException("Bank account not found"));
        if(bankAccount.getBalance()<amount) throw new BalanceNotSufficientException("Insufficient balance");
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(()->new BankAccountNotFoundException("Bank account not found"));
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to "+accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from "+accountIdSource);
    }

    @Override
    public AccountOperationDTO saveOperation(AccountOperationDTO accountOperationDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountOperationDTO.getBankAccountDTO().getId()).orElseThrow(()->new BankAccountNotFoundException("Bank account not found"));
        if(accountOperationDTO.getType()==OperationType.DEBIT){
            if(bankAccount.getBalance()<accountOperationDTO.getAmount()) throw new BalanceNotSufficientException("Insufficient balance");
            bankAccount.setBalance(bankAccount.getBalance()-accountOperationDTO.getAmount());
        }else{
            bankAccount.setBalance(bankAccount.getBalance()+accountOperationDTO.getAmount());
        }
        AccountOperation accountOperation=dtoMapper.fromAccountOperationDTO(accountOperationDTO);
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setOperationDate(new Date());
        AccountOperation savedAccountOperation=accountOperationRepository.save(accountOperation);
        return dtoMapper.fromAccountOperation(savedAccountOperation);
    }

    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts=bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS=bankAccounts.stream().map(bankAccount -> {
            if(bankAccount instanceof SavingAccount)
                return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            else
                return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElseThrow(()->new CustomerNotFoundException("Customer not found"));
        return dtoMapper.fromCustomer(customer);
    }
    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers= customerRepository.findAll();
        return customers.stream().map(customer -> dtoMapper.fromCustomer(customer)).toList();
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        if(keyword==null || keyword.isEmpty()) return listCustomers();
        List<Customer> customers=customerRepository.findByNameContains(keyword);
        return customers.stream().map(customer -> dtoMapper.fromCustomer(customer)).toList();
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.stream().map(accountOperation -> {
            return dtoMapper.fromAccountOperation(accountOperation);
        }).toList();
        return accountOperationDTOS;
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(()->new BankAccountNotFoundException("Bank account not found"));
        Page<AccountOperation> accountOperations=accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS=accountOperations.getContent().stream().map(accountOperation -> dtoMapper.fromAccountOperation(accountOperation)).toList();
        accountHistoryDTO.setAccountOperations(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }
}
