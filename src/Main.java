import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// КЛАСС ДЛЯ ЛОГИРОВАНИЯ
class LoggerUtil {
    private static final String LOG_FILE = "application.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logInfo(String message) {
        log("INFO", message);
    }

    public static void logError(String message) {
        log("ERROR", message);
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        System.out.println(logMessage);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logMessage);
        } catch (IOException e) {
            System.err.println("Ошибка записи лога: " + e.getMessage());
        }
    }
}

//КЛАСС ТОВАР
record Item(String name, double price, int quantity) {
    public double getTotalPrice() {
        return price * quantity;
    }
}

//КЛАСС ДЛЯ ПЕЧАТИ ЧЕКА
class CheckPrinter {
    private final Item[] items;
    private int itemCount;

    public CheckPrinter(int maxItems) {
        this.items = new Item[maxItems];
        this.itemCount = 0;
        LoggerUtil.logInfo("Создан чек на " + maxItems + " товаров");
    }

    public void addItem(Item item) {
        if (itemCount < items.length) {
            items[itemCount] = item;
            itemCount++;
            LoggerUtil.logInfo("Добавлен товар: " + item.name());
        }
    }

    public void printCheck() {
        LoggerUtil.logInfo("Печать чека");

        System.out.println("\nТОВАРНЫЙ ЧЕК");
        System.out.println("-----------");

        double total = 0.0;

        for (Item item : items) {
            if (item != null) {
                System.out.printf("%-20s %3d x %6.2f = %7.2f руб.%n",
                        item.name(),
                        item.quantity(),
                        item.price(),
                        item.getTotalPrice());
                total += item.getTotalPrice();
            }
        }

        System.out.println("-----------");
        System.out.printf("ИТОГО: %28.2f руб.%n", total);

        LoggerUtil.logInfo("Чек напечатан на сумму: " + total + " руб.");
    }
}

public class Main {
    public static void main(String[] args) {
        LoggerUtil.logInfo("Программа запущена");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Генератор чеков");
            System.out.print("Сколько товаров? ");

            int itemCount = scanner.nextInt();
            scanner.nextLine();

            LoggerUtil.logInfo("Количество товаров: " + itemCount);

            if (itemCount <= 0) {
                System.out.println("Ошибка: число должно быть больше 0");
                LoggerUtil.logError("Некорректное количество: " + itemCount);
                return;
            }

            CheckPrinter printer = new CheckPrinter(itemCount);

            for (int i = 0; i < itemCount; i++) {
                System.out.println("\nТовар #" + (i + 1));

                System.out.print("Название: ");
                String name = scanner.nextLine();

                if (name.trim().isEmpty()) {
                    name = "Товар " + (i + 1);
                }

                System.out.print("Цена: ");
                double price = scanner.nextDouble();

                if (price < 0) {
                    price = 0;
                }

                System.out.print("Количество: ");
                int quantity = scanner.nextInt();

                if (quantity <= 0) {
                    quantity = 1;
                }

                scanner.nextLine();

                Item item = new Item(name, price, quantity);
                printer.addItem(item);
            }

            printer.printCheck();

        } catch (InputMismatchException e) {
            System.out.println("Ошибка: нужно вводить числа");
            LoggerUtil.logError("Ошибка ввода: " + e.getMessage());

        } finally {
            System.out.println("\nПрограмма завершена");
            System.out.println("Логи в файле: " + new java.io.File("application.log").getAbsolutePath());
            LoggerUtil.logInfo("Программа завершена");
        }
    }
}