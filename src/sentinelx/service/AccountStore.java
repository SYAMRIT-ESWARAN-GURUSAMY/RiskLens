package sentinelx.service;

import sentinelx.model.Account;
import sentinelx.model.CurrentAccount;
import sentinelx.model.SavingsAccount;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Simple local file persistence; deliberately replaces a database for this academic project. */
public final class AccountStore {
    private final Path file = Path.of("data", "accounts.dat");

    public List<Account> load() {
        List<Account> loaded = new ArrayList<>();
        if (!Files.exists(file)) return loaded;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", -1);
                if (p.length != 8) continue;
                Account account = "Current".equals(p[0]) ? new CurrentAccount(p[1], decode(p[2]), Double.parseDouble(p[3]), 10_000) : new SavingsAccount(p[1], decode(p[2]), Double.parseDouble(p[3]));
                account.setPinCredentials(p[4], p[5]); account.setTotpSecret(p[6]);
                if (Boolean.parseBoolean(p[7])) account.freeze();
                loaded.add(account);
            }
        } catch (IOException | IllegalArgumentException ignored) { }
        return loaded;
    }
    public void save(List<Account> accounts) {
        try {
            Files.createDirectories(file.getParent());
            List<String> rows = accounts.stream().map(a -> String.join("|", a.accountKind(), a.accountNumber(), encode(a.customerName()), String.valueOf(a.balance()), a.pinHash(), a.pinSalt(), a.totpSecret(), String.valueOf(a.frozen()))).toList();
            Files.write(file, rows, StandardCharsets.UTF_8);
        } catch (IOException exception) { throw new IllegalStateException("Could not save local account data", exception); }
    }
    private static String encode(String text) { return Base64.getUrlEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)); }
    private static String decode(String text) { return new String(Base64.getUrlDecoder().decode(text), StandardCharsets.UTF_8); }
}
