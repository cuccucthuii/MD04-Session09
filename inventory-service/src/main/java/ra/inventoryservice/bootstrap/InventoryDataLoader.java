package ra.inventoryservice.bootstrap;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ra.inventoryservice.entity.Medicine;
import ra.inventoryservice.repository.MedicineRepository;

@Component
@RequiredArgsConstructor
public class InventoryDataLoader implements CommandLineRunner {

    private final MedicineRepository medicineRepository;

    @Override
    public void run(String... args) {
        if (medicineRepository.count() > 0) {
            return;
        }
        medicineRepository.save(Medicine.builder().id(42L).name("Thuốc demo (id 42)").stock(1_000).build());
        medicineRepository.save(Medicine.builder().id(1L).name("Vitamin").stock(500).build());
    }
}
