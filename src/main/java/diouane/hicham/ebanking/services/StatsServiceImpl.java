package diouane.hicham.ebanking.services;

import diouane.hicham.ebanking.entities.AccountOperation;
import diouane.hicham.ebanking.enums.OperationType;
import diouane.hicham.ebanking.repositories.AccountOperationRepository;
import diouane.hicham.ebanking.repositories.BankAccountRepository;
import diouane.hicham.ebanking.repositories.CustomerRepository;
import lombok.AllArgsConstructor;



import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {
    private BankAccountRepository bankAccountRepository;
    private CustomerRepository customerRepository;
    private AccountOperationRepository accountOperationRepository;

    @Override
    public Map<String, Object> getDashboardStats(String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Parse start and end date strings to Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // Fetch operations based on the date range
            List<AccountOperation> operations = accountOperationRepository.findByOperationDateBetween(start, end);

            // Calculate the total number of operations and the total amount
            int totalOperations = operations.size();
            double totalAmount = operations.stream().mapToDouble(AccountOperation::getAmount).sum();

            long numberOfAccounts = bankAccountRepository.count();
            long numberOfCustomers = customerRepository.count();

            // Add statistics to the map
            stats.put("numberOfAccounts", numberOfAccounts);
            stats.put("numberOfCustomers", numberOfCustomers);
            stats.put("totalOperations", totalOperations);
            stats.put("totalAmount", totalAmount);

        } catch (Exception e) {
            stats.put("error", "Invalid date format");
        }

        return stats;
    }

    @Override
    public Map<String, Map<String, Double>> getOperationChartData(String startDate, String endDate) {
        Map<String, Map<String, Double>> chartData = new HashMap<>();
        Map<String, Double> debitData = new HashMap<>();
        Map<String, Double> creditData = new HashMap<>();
        Map<String, Double> transferData = new HashMap<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // Fix: include full day for endDate
            Calendar cal = Calendar.getInstance();
            cal.setTime(end);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            end = cal.getTime();

            List<AccountOperation> operations = accountOperationRepository.findByOperationDateBetween(start, end);
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

            System.out.println("Found operations: " + operations.size());

            for (AccountOperation operation : operations) {
                String day = dayFormat.format(operation.getOperationDate());
                double amount = operation.getAmount();
                OperationType type = operation.getType();

                if (type == OperationType.DEBIT) {
                    debitData.put(day, debitData.getOrDefault(day, 0.0) + amount);
                } else if (type == OperationType.CREDIT) {
                    creditData.put(day, creditData.getOrDefault(day, 0.0) + amount);
                }
            }

            chartData.put("debit", debitData);
            chartData.put("credit", creditData);
            chartData.put("transfer", transferData);
            System.out.println("Chart Data: " + chartData);
        } catch (Exception e) {
            e.printStackTrace(); // utile pour le debug
        }

        return chartData;
    }
    @Override
    public Map<String, Long> getAccountsByType() {
        Map<String, Long> typeStats = new HashMap<>();
        bankAccountRepository.findAll().forEach(account -> {
            String type = account.getClass().getSimpleName(); // e.g., CurrentAccount, SavingAccount
            typeStats.put(type, typeStats.getOrDefault(type, 0L) + 1);
        });
        return typeStats;
    }
}
