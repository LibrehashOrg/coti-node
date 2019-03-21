package testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.*;
import io.coti.fullnode.http.AddTransactionRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;
    private static final int ANY_NUMBER = 10000;
    public static final String ANY_ADDRESS = "localhost";
    public static final String ANY_HTTP_PORT = "8080";
    public static final String ANY_RECEIVING_PORT = "9090";

    private static double generateRandomTrustScore() {
        return Math.random() * 100;
    }

    public static Double generateRandomPositiveAmount() {
        return Math.random() * ANY_NUMBER;
    }

    public static double generateRandomCount() {
        return Math.random() * Double.MAX_VALUE;
    }

    public static long generateRandomLongNumber() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }

    public static int generateRandomIntNumber() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    public static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData(generateRandomHash(),
                        new BigDecimal(0),
                        new Date())));
        return new TransactionData(baseTransactions,
                hash,
                "test",
                generateRandomTrustScore(),
                new Date(),
                TransactionType.Payment);
    }

    public static Hash generateRandomHash() {
        return generateRandomHash(SIZE_OF_HASH);
    }

    private static Hash generateRandomHash(int lengthOfHash) {
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa.append(hexaOptions[randomNum]);
        }
        return new Hash(hexa.toString());
    }

    public static TransactionData createRandomTransaction() {
        return createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
    }

    public static TransactionData createTransactionFromJson(String transactionJson) {
        AddTransactionRequest addTransactionRequest = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            addTransactionRequest =
                    mapper.readValue(transactionJson, AddTransactionRequest.class);
        } catch (IOException e) {
            log.error("Error when create trx from JSON" + e);
        }
        return
                new TransactionData(
                        addTransactionRequest.baseTransactions,
                        addTransactionRequest.hash,
                        addTransactionRequest.transactionDescription,
                        addTransactionRequest.trustScoreResults.get(0).getTrustScore(),
                        addTransactionRequest.createTime,
                        TransactionType.Payment);
    }

    public static AddTransactionRequest generateAddTransactionRequest() {
        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        addTransactionRequest.hash = generateRandomHash(SIZE_OF_HASH);
        addTransactionRequest.baseTransactions = new ArrayList<>();
        return addTransactionRequest;
    }

    public static BaseTransactionData createBaseTransactionDataWithSpecificHash(Hash hash) {
        return new InputBaseTransactionData
                (hash,
                        new BigDecimal(0),
                        new Date());
    }

    public static NetworkNodeData generateRandomNetworkNodeData() {
        NodeType nodeType = NodeType.DspNode;
        String address = ANY_ADDRESS;
        String httpPort = ANY_HTTP_PORT;
        Hash nodeHash = generateRandomHash();
        NetworkType networkType = NetworkType.TestNet;

        NetworkNodeData networkNodeData = new NetworkNodeData(nodeType, address, httpPort, nodeHash, networkType);
        networkNodeData.setReceivingPort(ANY_RECEIVING_PORT);
        return networkNodeData;
    }
}
