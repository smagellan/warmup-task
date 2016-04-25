package test.warmup.banktransfer;

import java.util.Objects;

/**
 * Created by vladimir on 4/19/16.
 */
public class Transfer {
    private final String fromAccountNum;
    private final String toAccountNum;
    private final long amount;
    public Transfer(String fromAccountNum, String toAccountNum, long amount) {
        this.fromAccountNum = fromAccountNum;
        this.toAccountNum   = toAccountNum;
        this.amount         = amount;
    }

    public String fromAccountNum() {
        return fromAccountNum;
    }

    public String toAccountNum() {
        return toAccountNum;
    }

    public long amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return amount == transfer.amount &&
                Objects.equals(fromAccountNum, transfer.fromAccountNum) &&
                Objects.equals(toAccountNum, transfer.toAccountNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromAccountNum, toAccountNum, amount);
    }
}
