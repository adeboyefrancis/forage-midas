package com.jpmc.midascore;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public KafkaConsumer(UserRepository userRepository, 
                         TransactionRecordRepository transactionRecordRepository, 
                         RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        // --- TASK 2: Log every incoming amount ---
        logger.info("Processing transaction: amount={}", transaction.getAmount());

        // --- TASK 3 & 4: Database Logic & External API ---
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        // Validation: IDs must exist and sender must have enough funds
        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {
            
            // 1. Call the Incentives API (Task 4)
            // Ensure the incentives-api.jar is running on port 8080!
            String incentiveUrl = "http://localhost:8080/incentive";
            float incentiveAmount = 0f;
            try {
                Incentive response = restTemplate.postForObject(incentiveUrl, transaction, Incentive.class);
                if (response != null) {
                    incentiveAmount = response.getAmount();
                }
            } catch (Exception e) {
                logger.error("Failed to connect to Incentives API: {}", e.getMessage());
            }

            // 2. Update Balances
            // Sender: deducts ONLY the transaction amount
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            // Recipient: receives transaction amount PLUS the incentive (Task 4)
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

            // 3. Create Transaction Record
            TransactionRecord record = new TransactionRecord();
            record.setSender(sender);
            record.setRecipient(recipient);
            record.setAmount(transaction.getAmount());
            record.setIncentive(incentiveAmount); // Ensure this field exists in your Entity!

            // 4. Save everything to H2 Database
            transactionRecordRepository.save(record);
            userRepository.save(sender);
            userRepository.save(recipient);

            // 5. Watch for Wilbur (Task 4 Submission)
            if (sender.getName().equalsIgnoreCase("wilbur") || recipient.getName().equalsIgnoreCase("wilbur")) {
                UserRecord wilbur = userRepository.findByName("wilbur");
                if (wilbur != null) {
                    logger.warn(">>> WILBUR BALANCE UPDATED: {}", (int) Math.floor(wilbur.getBalance()));
                }
            }
        } else {
            logger.debug("Transaction skipped: Invalid users or insufficient balance.");
        }
    }
}
