package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Date;

@Data
public abstract class OutputBaseTransactionData extends BaseTransactionData {
    @Positive
    protected BigDecimal amount;
    @Positive
    protected BigDecimal originalAmount;

    protected OutputBaseTransactionData() {
        super();
    }

    public OutputBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, baseTransactionHash, signature, createTime);
        this.setOriginalAmount(originalAmount);
    }

    protected OutputBaseTransactionData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Date createTime) {
        super(addressHash, amount, createTime);
        this.setOriginalAmount(originalAmount);
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("Output transaction can not have non positive amount");
        }
        this.amount = amount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        if (originalAmount == null || originalAmount.signum() <= 0) {
            throw new IllegalStateException("Original amount can not have non positive amount");
        }
        this.originalAmount = originalAmount;
    }
}