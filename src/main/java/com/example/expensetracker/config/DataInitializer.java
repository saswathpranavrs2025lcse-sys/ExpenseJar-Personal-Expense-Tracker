package com.example.expensetracker.config;

import com.example.expensetracker.entity.Budget;
import com.example.expensetracker.entity.Category;
import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.RecurringType;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        UserRepository userRepository,
        CategoryRepository categoryRepository,
        ExpenseRepository expenseRepository,
        BudgetRepository budgetRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Prevent duplicate seeding on application restarts if already populated
        if (userRepository.count() > 0 && expenseRepository.count() > 20) {
            log.info("Database already initialized with college hostel dataset. Skipping population.");
            return;
        }

        // If partially initialized with old minimal data, clear it for the authentic hostel dataset
        if (userRepository.count() > 0) {
            log.info("Refreshing database with rich 3-month college hostel dataset...");
            expenseRepository.deleteAll();
            budgetRepository.deleteAll();
        } else {
            log.info("Initializing 3-month College Hostel Student dataset for Expense Tracker...");
        }

        // 1. Seed or Retrieve Users with securely hashed passwords and unique User IDs
        User john = userRepository.findByEmail("john@example.com").orElseGet(() -> {
            User u = new User();
            u.setName("Mithil Pranav");
            u.setEmail("john@example.com");
            u.setPassword(passwordEncoder.encode("password123"));
            u.setUserIdCode("EXP-10001");
            return userRepository.save(u);
        });

        User alex = userRepository.findByEmail("alex@example.com").orElseGet(() -> {
            User u = new User();
            u.setName("Alex Rivera");
            u.setEmail("alex@example.com");
            u.setPassword(passwordEncoder.encode("password123"));
            u.setUserIdCode("EXP-10002");
            return userRepository.save(u);
        });

        // 2. Seed Default Global Categories
        Category food = getOrCreateCategory("Food", "Hostel mess, night canteen, cafeteria, Maggi, tea & Swiggy");
        Category travel = getOrCreateCategory("Travel", "Train tickets home, metro smart card, bus & auto shares");
        Category stationery = getOrCreateCategory("Stationery", "Textbooks, lab record files, project printouts & binding");
        Category entertainment = getOrCreateCategory("Entertainment", "Spotify, movie outings with roomies, gaming & tech fest");
        Category shopping = getOrCreateCategory("Shopping", "Hostel flip-flops, casual wear & room accessories");
        Category other = getOrCreateCategory("Other", "Room setup, laundry, mobile recharge & hostel medical kit");

        YearMonth currentYm = YearMonth.now().getYear() == 2026 && YearMonth.now().getMonthValue() == 9
            ? YearMonth.now()
            : YearMonth.of(2026, 9);
        YearMonth prevYm1 = currentYm.minusMonths(1); // 2026-08
        YearMonth prevYm2 = currentYm.minusMonths(2); // 2026-07

        // =========================================================================
        // 3. SEED BUDGETS FOR ALL 3 MONTHS (Hostel Student Budget Allocation)
        // =========================================================================
        // July 2026 (Semester Reopening & Move-in)
        budgetRepository.save(new Budget(new BigDecimal("5000.00"), prevYm2.toString(), john, food));
        budgetRepository.save(new Budget(new BigDecimal("2500.00"), prevYm2.toString(), john, travel));
        budgetRepository.save(new Budget(new BigDecimal("2500.00"), prevYm2.toString(), john, stationery));
        budgetRepository.save(new Budget(new BigDecimal("1500.00"), prevYm2.toString(), john, entertainment));
        budgetRepository.save(new Budget(new BigDecimal("2500.00"), prevYm2.toString(), john, other));

        // August 2026 (Mid-Semester Routine & Independence Day)
        budgetRepository.save(new Budget(new BigDecimal("5500.00"), prevYm1.toString(), john, food));
        budgetRepository.save(new Budget(new BigDecimal("3000.00"), prevYm1.toString(), john, travel));
        budgetRepository.save(new Budget(new BigDecimal("1500.00"), prevYm1.toString(), john, stationery));
        budgetRepository.save(new Budget(new BigDecimal("1500.00"), prevYm1.toString(), john, entertainment));
        budgetRepository.save(new Budget(new BigDecimal("1200.00"), prevYm1.toString(), john, other));

        // September 2026 (Current Month - Mid-Terms & Project Submission)
        budgetRepository.save(new Budget(new BigDecimal("5000.00"), currentYm.toString(), john, food));
        budgetRepository.save(new Budget(new BigDecimal("3000.00"), currentYm.toString(), john, travel));
        budgetRepository.save(new Budget(new BigDecimal("1500.00"), currentYm.toString(), john, stationery));
        budgetRepository.save(new Budget(new BigDecimal("1500.00"), currentYm.toString(), john, entertainment));
        budgetRepository.save(new Budget(new BigDecimal("1200.00"), currentYm.toString(), john, other));

        // Budgets for Alex (User B) to ensure data isolation
        budgetRepository.save(new Budget(new BigDecimal("4000.00"), currentYm.toString(), alex, food));
        budgetRepository.save(new Budget(new BigDecimal("2000.00"), currentYm.toString(), alex, travel));

        // =========================================================================
        // 4. SEED 3-MONTH DATASET FOR COLLEGE BOY IN HOSTEL
        // =========================================================================

        // -------------------------------------------------------------------------
        // MONTH 1: JULY 2026 — Hostel Move-in, Room Setup & Semester Reopening
        // -------------------------------------------------------------------------
        expenseRepository.save(new Expense(new BigDecimal("350.00"), prevYm2.atDay(2),
            "Auto-rickshaw from railway station to college hostel with luggage", john, travel, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("1450.00"), prevYm2.atDay(3),
            "Hostel room setup: bucket, mug, extension cord, study lamp, bedsheet", john, other, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("1800.00"), prevYm2.atDay(5),
            "Hostel mess monthly meal token coupon booklet", john, food, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("1250.00"), prevYm2.atDay(7),
            "Semester core textbooks (Data Structures, OS & Digital Electronics)", john, stationery, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("450.00"), prevYm2.atDay(9),
            "Lab record notebooks, graph sheets, file folders, and gel pens", john, stationery, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("220.00"), prevYm2.atDay(12),
            "Night canteen Maggi, bun maska, and hot tea with roomies", john, food, RecurringType.DAILY));
        expenseRepository.save(new Expense(new BigDecimal("1199.00"), prevYm2.atDay(15),
            "Casual t-shirts and hostel flip-flops", john, shopping, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("600.00"), prevYm2.atDay(18),
            "Metro smart card recharge for city commute", john, travel, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("480.00"), prevYm2.atDay(20),
            "Swiggy chicken biryani order during late night coding session", john, food, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("380.00"), prevYm2.atDay(23),
            "Movie ticket with hostel wing-mates at city mall", john, entertainment, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("749.00"), prevYm2.atDay(25),
            "Jio 5G 3-month hostel hotspot & unlimited calling plan", john, other, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("260.00"), prevYm2.atDay(28),
            "Evening samosa and cold coffee at campus Nescafe corner", john, food, RecurringType.DAILY));
        expenseRepository.save(new Expense(new BigDecimal("320.00"), prevYm2.atDay(30),
            "Hostel laundry service and Ariel detergent washing powder pack", john, other, RecurringType.MONTHLY));

        // -------------------------------------------------------------------------
        // MONTH 2: AUGUST 2026 — Mid-Semester Routine & Independence Day Hometown Trip
        // -------------------------------------------------------------------------
        expenseRepository.save(new Expense(new BigDecimal("1800.00"), prevYm1.atDay(1),
            "Hostel mess monthly advance meal token", john, food, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("119.00"), prevYm1.atDay(3),
            "Spotify Student monthly music subscription", john, entertainment, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("320.00"), prevYm1.atDay(6),
            "Hostel back-gate Shawarma roll and chilled lime soda", john, food, RecurringType.DAILY));
        expenseRepository.save(new Expense(new BigDecimal("380.00"), prevYm1.atDay(8),
            "Mid-sem seminar report color printouts & notes photocopies", john, stationery, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("750.00"), prevYm1.atDay(11),
            "State transport bus ticket to hometown for Independence Day weekend", john, travel, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("720.00"), prevYm1.atDay(15),
            "Return train ticket from home back to hostel", john, travel, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("460.00"), prevYm1.atDay(17),
            "Late-night canteen parotta & egg bhurji with wing friends", john, food, RecurringType.DAILY));
        expenseRepository.save(new Expense(new BigDecimal("350.00"), prevYm1.atDay(19),
            "Scientific calculator battery and engineering chart sheets", john, stationery, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("450.00"), prevYm1.atDay(21),
            "Weekend gaming cafe LAN multiplayer tournament with batchmates", john, entertainment, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("450.00"), prevYm1.atDay(23),
            "Shared auto & metro fare to college examination center", john, travel, RecurringType.WEEKLY));
        expenseRepository.save(new Expense(new BigDecimal("650.00"), prevYm1.atDay(25),
            "Domino's pizza treat with study group after mock tests", john, food, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("420.00"), prevYm1.atDay(28),
            "Personal hygiene kit (shampoo, soap, face wash, toothpaste)", john, other, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("280.00"), prevYm1.atDay(30),
            "Hostel night canteen chai and butter toast during exam prep", john, food, RecurringType.DAILY));

        // -------------------------------------------------------------------------
        // MONTH 3: SEPTEMBER 2026 — Mid-Term Exams & Major Project Submissions
        // -------------------------------------------------------------------------
        expenseRepository.save(new Expense(new BigDecimal("1800.00"), currentYm.atDay(1),
            "Hostel mess monthly advance meal token", john, food, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("119.00"), currentYm.atDay(2),
            "Spotify Student monthly music subscription", john, entertainment, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("850.00"), currentYm.atDay(4),
            "Train reservation ticket home for upcoming festival holidays", john, travel, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("380.00"), currentYm.atDay(6),
            "Campus cafeteria lunch thali and fruit juice", john, food, RecurringType.DAILY));
        expenseRepository.save(new Expense(new BigDecimal("650.00"), currentYm.atDay(8),
            "Major project report binding, glossy cover, and spiral sheets", john, stationery, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("540.00"), currentYm.atDay(11),
            "Swiggy midnight study snack during mid-term exam week", john, food, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("350.00"), currentYm.atDay(13),
            "Hostel medical kit: paracetamol, cough syrup, and pain spray", john, other, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("950.00"), currentYm.atDay(16),
            "Shared cab with hostel batchmates for industrial plant visit", john, travel, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("280.00"), currentYm.atDay(18),
            "Exam answer sheets clipboard, blue pens pack, and sticky notes", john, stationery, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("850.00"), currentYm.atDay(20),
            "Post-exam celebration dinner at local dhaba with hostel friends", john, food, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("1050.00"), currentYm.atDay(22),
            "Cab rides back from project presentation and station", john, travel, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("630.00"), currentYm.atDay(24),
            "Hostel night canteen snacks, samosa and tea during hackathon", john, food, RecurringType.DAILY));
        expenseRepository.save(new Expense(new BigDecimal("500.00"), currentYm.atDay(26),
            "College tech-fest pass and esports gaming tournament entry", john, entertainment, RecurringType.ONE_TIME));
        expenseRepository.save(new Expense(new BigDecimal("299.00"), currentYm.atDay(28),
            "Monthly mobile recharge & hostel hotspot data booster", john, other, RecurringType.MONTHLY));

        // Seed 2 sample expenses for Alex to verify user isolation
        expenseRepository.save(new Expense(new BigDecimal("2100.00"), currentYm.atDay(3),
            "Monthly groceries & personal care", alex, food, RecurringType.MONTHLY));
        expenseRepository.save(new Expense(new BigDecimal("800.00"), currentYm.atDay(14),
            "Metro travel card recharge", alex, travel, RecurringType.MONTHLY));

        log.info("Successfully populated 3-month realistic College Hostel Student dataset for Mithil Pranav (EXP-10001).");
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() ->
            categoryRepository.save(new Category(name, description))
        );
    }
}
