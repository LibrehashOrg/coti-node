package io.coti.common.http;



import io.coti.common.data.TransactionData;
import io.coti.common.data.TransactionResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Data
public class GetAddressTransactionHistory extends Response {
    private List<TransactionResponseData> transactionsData;

    public GetAddressTransactionHistory(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = new Vector<>();
        for (TransactionData transactionData: transactionsData
             ) {
            this.transactionsData.add(new TransactionResponseData(transactionData));
        }


    }
}
