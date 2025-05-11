package diouane.hicham.ebanking.repositories;
import diouane.hicham.ebanking.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {
    long count();
}
