package diouane.hicham.ebanking.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.*;

@Entity
@DiscriminatorValue("CA")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class CurrentAccount extends BankAccount{
    private double overDraft;
}
