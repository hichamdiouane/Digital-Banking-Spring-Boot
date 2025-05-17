package diouane.hicham.ebanking.exceptions;

public class BalanceNotSufficientException extends Exception {
    public BalanceNotSufficientException(String insufficientBalance) {
        super(insufficientBalance);
    }
}
