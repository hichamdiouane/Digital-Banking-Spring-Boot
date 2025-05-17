package diouane.hicham.ebanking.exceptions;

public class BankAccountNotFoundException extends Exception {
    public BankAccountNotFoundException(String bankAccountNotFound) {
        super(bankAccountNotFound);
    }
}
