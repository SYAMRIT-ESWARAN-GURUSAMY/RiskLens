# SentinelX - Intelligent ATM Security Simulator

SentinelX is an educational Core Java 21 desktop application. It simulates ATM and digital banking transactions, evaluates them with explainable fraud rules, and maintains a tamper-evident local audit trail. It is **not** connected to a bank and must not be used for real financial data.

## Requirements and run

Install a JDK 21 or newer, then run these commands from this folder:

Double-click `Run SentinelX.bat` to compile and open the application. No command line is required.

From Command Prompt, use:

```cmd
cd /d "C:\Users\Ruby\Documents\ATM security"
powershell -NoProfile -Command "$n=(Get-Location).Path.Length+1; Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName.Substring($n) } | Set-Content -Encoding ascii sources.txt"
javac --release 21 -d out @sources.txt
java -cp out sentinelx.app.Main
```

For a non-interactive demonstration, run `java -cp out sentinelx.app.Main --demo`.

The program opens a Swing customer console. Authenticate using account `1001002`, PIN `5678`; then use **Attack simulator** to run safe local scenarios. Audit records are written to `data/audit.log`.

## Project structure

## Completed application

SentinelX includes the ATM/customer console, local account persistence, PIN authentication with three-attempt lockout, salted PIN storage, time-based verification challenges, timed sessions, transaction history, cross-channel simulations, behaviour profiling, weighted fraud detection, security alerts, administrator monitoring and account unfreeze controls, tamper-evident audit logging and verification, and safe attack-simulator scenarios.

Demo customer login: account `1001002`, PIN `5678`. Administrator dashboard code: `admin123`.
src/sentinelx/
  app/       application entry point
  model/     encapsulated domain objects and records
  security/  salted PIN hashing and authentication
  fraud/     strategy-based rules and decisions
  service/   transaction orchestration and audit log
  ui/        Swing administrator / ATM console
  simulator/ safe demonstration attack scenarios
```

## Syllabus mapping

| Topic | Where it appears |
|---|---|
| Encapsulation, constructors, access control | `model/Account`, `BehaviourProfile` |
| Inheritance, abstraction, polymorphism | `Account`, `SavingsAccount`, `CurrentAccount`, `FraudRule` |
| Interfaces | `FraudRule` implementations |
| Collections and strings | profile history, rules, audit explanations |
| Exceptions | `AuthenticationException`, `BankingException` |
| File handling | `AuditLogger` |
| Virtual threads / producer-consumer | `EventMonitor` |
| Swing and event handling | `ui/SentinelXFrame` |
| Java 21 records / pattern switch | `Transaction`, `RiskContribution`, `TransactionFormatter` |
| Static and final members | `Constants`, `PinHasher` |

## Important limitations

This is a classroom simulator. Its local storage, authentication, and rule engine demonstrate concepts but are not production banking security controls.
