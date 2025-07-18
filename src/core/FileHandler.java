package core;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {

    // Save transactions to a file
    public static void saveTransactions(List<Transaction> transactions) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("transactions.dat"))) {
            oos.writeObject(transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load transactions from a file
    public static List<Transaction> loadTransactions() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("transactions.dat"))) {
            return (List<Transaction>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Save users to a file
    public static void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load users from a file
    public static Map<String, User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            return (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
