package sentinelx.security;

import sentinelx.exception.AuthenticationException;
import sentinelx.model.Account;
import sentinelx.util.Constants;

/** Handles PIN validation and account lockout. */
public final class AuthService {
    public void setPin(Account account, String pin) {
        if (pin == null || !pin.matches("\\d{4}")) throw new IllegalArgumentException("PIN must contain exactly four digits");
        String salt = PinHasher.newSalt(); account.setPinCredentials(PinHasher.hash(pin, salt), salt); account.markPinChanged();
    }
    public void authenticate(Account account, String pin, int failedAttempts) throws AuthenticationException {
        if (account.frozen()) throw new AuthenticationException("Account is frozen");
        if (!PinHasher.matches(pin, account.pinHash(), account.pinSalt())) {
            if (failedAttempts + 1 >= Constants.MAX_PIN_ATTEMPTS) account.freeze();
            throw new AuthenticationException(account.frozen() ? "PIN attempts exceeded; account frozen" : "Incorrect PIN");
        }
    }
}
