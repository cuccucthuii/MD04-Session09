package ra.pharmacyservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements CommandLineRunner {
    @Value("${app.branch-name}")
    private String branchName;

    @Value("${app.hotline}")
    private String hotline;

    @Override
    public void run(String... args) {
        System.out.println("=================================");
        System.out.println("Ten chi nhanh: " + branchName);
        System.out.println("Hotline: " + hotline);
        System.out.println("=================================");
    }
}
