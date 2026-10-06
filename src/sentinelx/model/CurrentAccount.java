package sentinelx.model;

/** Account with a small configurable overdraft, for inheritance demonstration. */
public final class CurrentAccount extends Account {
    private final double overdraftLimit;
    public CurrentAccount(String number, String customer, double openingBalance, double overdraftLimit) {
        super(number, customer, openingBalance); this.overdraftLimit = overdraftLimit;
    }
    @Override public String accountKind() { return "Current"; }
    @Override public boolean canWithdraw(double amount) { return !frozen() && amount > 0 && balance() - amount >= -overdraftLimit; }
}
