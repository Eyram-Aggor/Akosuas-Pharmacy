# Akosua's Pharmacy: Stock and Dispensing Tracker

A small program that tracks what is in stock and what gets dispensed, for a community
pharmacy. This is a rebuild of an inherited single-file program. It does the same job
as before, but a stock count can no longer become invalid, and a new kind of medicine
(such as the controlled substances coming next) can be added without editing existing code.

Read [DIAGNOSIS.md](DIAGNOSIS.md) first for what was wrong with the original and why.

## Running it

Prerequisite: JDK 21. Maven is optional.

```bash
# with Maven
mvn test                                   # run the 30 tests
mvn compile exec:java -Dexec.mainClass=com.amalitech.pharmacy.PharmacyApp

# or with the JDK only
javac -d target/classes $(find src/main -name "*.java")
java -cp target/classes com.amalitech.pharmacy.PharmacyApp
```

Expected output:

```
Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.0
Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by Dr. Boateng) for GHS 28.5
Restocked 50 x Paracetamol 500mg
Cannot dispense 100 x Amoxicillin 250mg: only 39 in stock

=== Akosua's Pharmacy - end of day ===
Stock levels:
  Paracetamol 500mg (OTC): 148 in stock
  Amoxicillin 250mg (PRESCRIPTION): 39 in stock
Transactions today: 3
```

## Structure

```
com.amalitech.pharmacy
├── PharmacyApp            entry point; replays the original demo day
├── common/                Money, Prescriber       (small immutable values, records)
├── exception/             DispenseException and its two refusals
├── medication/            Medication (abstract), OtcMedication, PrescriptionMedication, Inventory
├── customer/              Customer (abstract), WalkInCustomer, RegisteredPatient, CustomerRegistry
├── transaction/           Transaction (sealed) = Dispensing | Restocking, TransactionLog
├── service/               Pharmacy (coordinator), DayReport
└── ui/                    ConsoleReporter         (the only class that prints)
```

Dependencies point one way: `ui → service → medication / customer / transaction → common`.
Business classes never print and never read the console.

## Behaviour preserved, and the deliberate differences

`PharmacyAppTest` replays the original day and checks every output line. Compared with
the original output captured in DIAGNOSIS.md:

| Original | Rebuilt | Why |
|---|---|---|
| Lines 1 to 3 | Identical | Same prices, 5% registered discount, `GHS 25.0` / `GHS 28.5` formatting |
| `Dispensed 100 x Amoxicillin ... GHS 3000.0` | `Cannot dispense 100 x Amoxicillin 250mg: only 39 in stock` | **The rule that must never break**: a sale cannot exceed stock |
| Amoxicillin `-61` | `39` | Consequence of the above |
| `Transactions today: 4` | `3` | A refused sale is not a transaction |

Smaller intentional tightenings (all were silent bugs; see DIAGNOSIS.md): zero or negative
quantities are rejected, a blank prescriber is not a prescriber, a registered patient must
have a phone number, and medicines or customers the pharmacy does not know are rejected.

Money is exact decimal (`BigDecimal`) and is **not rounded**, so values match the original
wherever `double` was exact. One open question for Akosua: sub-pesewa totals (e.g. 5% off
GHS 12.50 = GHS 11.875) are kept as-is, as the original did. Rounding to the pesewa is a
one-line change in `Money` once she decides the rule.

## Key design decisions

**1. Make the invalid state unrepresentable, not "checked in a few places".**
`Medication.stock` is private. The only code that can change it is `Medication.dispense`
and `Medication.restock`, both package-private, so only `Inventory` can reach them. Both
check before they change anything and neither can go below zero. I chose this over a
`if (qty > stock)` inside `Pharmacy.dispense` because that is just the original design
with one more check that the next change could bypass.

**2. Abstract class + polymorphism for medicines, instead of the category string.**
The original asked "what category are you?" in a central `if/else`. Now each kind of
medicine answers for itself through `authorise(...)`. `Pharmacy` contains no medicine-specific
logic. `Medication` is an abstract class rather than an interface because all medicines
share *state* that must be protected (name, price, the stock count and its invariant), and an
interface cannot hold that. I used `final` template methods (`dispense`, `restock`) so a
subclass can add a rule but cannot skip the stock check.

**3. Customers own their price.** `RegisteredPatient.priceFor` applies the 5% discount.
This removes the duplicated `* 0.95` blocks. I chose inheritance here (two kinds of the same
thing) and left composition for where behaviour varies independently of identity; there is
currently nothing like that, so adding a discount-policy object would be speculative.

**4. Refusals are checked exceptions.** `DispenseException` is checked so the compiler
forces every caller to handle a refused sale, which is what the original never did. A
refusal is thrown *before* anything changes, so a refused sale leaves stock and the audit
trail untouched (tested). Programmer errors (null, non-positive quantity) are
`IllegalArgumentException`.

**5. Records and a sealed interface for the audit trail.** `Dispensing` and `Restocking` are
immutable records, and `Transaction` is sealed, so the only entries that can exist are
the ones we define. `Dispensing` stores the prescriber, which fixes the original gap where
it was only printed. `TransactionLog` hands out only unmodifiable views. A customer's
history is derived from the log rather than stored a second time, so the two can never disagree.

**6. Console output is a separate class.** `ConsoleReporter` takes a `PrintStream`, which is
why the output can be asserted in a test.

**7. Dropped numeric IDs and static counters.** Nothing ever read them, and static counters
are shared global state. Transaction ids now come from the log (per pharmacy). Medicine
names are unique within the inventory instead.

### What changed from the sketch in DIAGNOSIS.md

- `Medication` is **not sealed** (the sketch said sealed). Sealing would mean editing a
  `permits` clause for every new medicine type, which is exactly the kind of edit this
  design is meant to avoid. Polymorphism means nothing switches over the subtypes, so
  sealing buys nothing here. `Transaction` stays sealed because there is a closed set of them.
- `Customer` became an abstract class, not a sealed interface of records, because it shares a
  validated name and its subclasses carry behaviour (`priceFor`).
- `Transaction` is split into `Dispensing` and `Restocking` rather than one record with a
  `type`, so a restock no longer carries a meaningless customer and total.
- `DispenseRequest` was dropped. Two overloads of `Pharmacy.dispense` do the job with less code.
- Stock mutators live behind `Inventory` (package-private) so that stock can only move through
  the pharmacy's own path.

## Adding controlled substances

The change Akosua mentioned: stricter rules, prescriber recorded on every dispense.

**With this design: add one class.** `ControlledSubstanceExtensionTest` does exactly
that, in a test file, with no production code edited:

```java
final class ControlledMedication extends Medication {
    public String categoryLabel() { return "CONTROLLED"; }
    protected Optional<Prescriber> authorise(Optional<Prescriber> p) throws PrescriberRequiredException {
        if (p.isEmpty()) throw new PrescriberRequiredException(name());
        return p;      // stricter checks (licence number, daily limits...) go here
    }
}
```

It automatically gets: the never-negative stock guarantee, pricing and discounts, the
prescriber stored on the `Dispensing`, the audit log, the stock report line, and the console
output. To ship it, move that class into `medication/`.

**With the original it would have meant** editing `Pharmacy.dispense` (a third copy of the
pricing/discount/stock block), the `category` string checks in the report, the `Transaction`
class (to add a prescriber field), and the console printing, all inside the same code that OTC
and prescription sales depend on, with no tests to show they still worked.

## The one improvement beyond the brief: a safety-net test suite

30 JUnit tests (`mvn test`). I chose this because the team's stated pain is that changes
"quietly broke another" and the owner no longer trusts the numbers. Tests turn that
fear into a check anyone can run in seconds:

- the **never-negative** rule, including refused sales leaving stock and the log untouched
- a **golden-output test** that replays the original day (behaviour-preservation evidence)
- a **controlled-substance extension test** that proves the "easy to grow" claim instead of asserting it

## Commit history

1. `chore: diagnosis of the codebase inherited`: DIAGNOSIS.md and design sketch, before any code change
2. `chore: add inherited starter code unchanged (baseline)`
3. `refactor: rebuild tracker ...`: the new structure
4. `test: add JUnit suite ...`
