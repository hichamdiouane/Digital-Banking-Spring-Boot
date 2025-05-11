package diouane.hicham.ebanking.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@DiscriminatorValue("SA")
@AllArgsConstructor @NoArgsConstructor @Getter @Setter
public class SavingAccount extends BankAccount{
    private double interestRate;
}
