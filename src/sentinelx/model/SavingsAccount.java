package sentinelx.model;

import sentinelx.util.Constants;

/** Account whose withdrawals preserve a minimum balance. */
public final class SavingsAccount extends Account {
    public SavingsAccount(String number, String customer, double openingBalance) { super(number, customer, openingBalance); }
    @Override public String accountKind() { return "Savings"; }
    @Override public boolean canWithdraw(double amount) { return !frozen() && amount > 0 && balance() - amount >= Constants.MINIMUM_SAVINGS_BALANCE; }
}
