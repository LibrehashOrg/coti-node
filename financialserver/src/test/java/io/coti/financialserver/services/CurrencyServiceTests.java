package io.coti.financialserver.services;

import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetTokenGenerationDataRequest;
import io.coti.basenode.http.GetTokenGenerationDataResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.Response;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IChunkService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.model.PendingCurrencies;
import io.coti.financialserver.model.UserTokenGenerations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {CurrencyService.class, GetTokenGenerationDataRequestCrypto.class, NodeCryptoHelper.class, UserTokenGenerations.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class CurrencyServiceTests {

    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private GetTokenGenerationDataRequestCrypto getTokenGenerationDataRequestCrypto;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private UserTokenGenerations userTokenGenerations;
    @MockBean
    private PendingCurrencies pendingCurrencies;
    @MockBean
    private Currencies currencies;

    //
    @MockBean
    private CurrencyNameIndexes currencyNameIndexes;
    @MockBean
    protected INetworkService networkService;
    @MockBean
    private GenerateTokenRequestCrypto generateTokenRequestCrypto;
    @MockBean
    private CurrencyOriginatorCrypto currencyOriginatorCrypto;
    @MockBean
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @MockBean
    private Transactions transactions;

    //from base node
    @MockBean
    protected RestTemplate restTemplate;
    @MockBean
    protected ApplicationContext applicationContext;
    @MockBean
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @MockBean
    private HttpJacksonSerializer jacksonSerializer;
    @MockBean
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @MockBean
    private IChunkService chunkService;

    //
    private static Hash userHash;
    private static Hash currencyHash;
    private static Hash transactionHash;
    private static CurrencyData currencyData;


    @BeforeClass
    public static void setUpOnce() {
        userHash = new Hash("00");
        transactionHash = new Hash("11");
        currencyHash = new Hash("22");

        currencyData = new CurrencyData();
        currencyData.setHash(currencyHash);
        currencyData.setName("AlexToken");
        currencyData.setSymbol("ALX");
        currencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData.setDescription("Dummy token for testing");
        currencyData.setTotalSupply(new BigDecimal("100"));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());
    }

    @Before
    public void setUpBeforeEachTest() {

    }

    //TODO 9/20/2019 astolia: clean up, remove duplicates...

    @Test
    public void getUserTokenGenerationData_UnsignedRequest_shouldReturnBadRequest() {
        GetTokenGenerationDataRequest getTokenGenerationDataRequest = new GetTokenGenerationDataRequest();
        getTokenGenerationDataRequest.setSenderHash(userHash);
        ResponseEntity expected = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));

        ResponseEntity actual = currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);
        Assert.assertEquals(expected, actual);

        getTokenGenerationDataRequest.setSenderHash(new Hash("1111"));
        actual = currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_noUserTokenGenerations_shouldReturnEmptyResponse() {
        GetTokenGenerationDataRequest getTokenGenerationDataRequest = new GetTokenGenerationDataRequest();
        getTokenGenerationDataRequest.setSenderHash(userHash);
        getTokenGenerationDataRequestCrypto.signMessage(getTokenGenerationDataRequest);
        ResponseEntity actual = currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);
        ResponseEntity expected = ResponseEntity.ok(new GetTokenGenerationDataResponse());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyPendingToken_shouldReturnResponseWithAvailableTransaction() {
        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetTokenGenerationDataRequest getTokenGenerationDataRequest = new GetTokenGenerationDataRequest();
        getTokenGenerationDataRequest.setSenderHash(userHash);
        getTokenGenerationDataRequestCrypto.signMessage(getTokenGenerationDataRequest);
        ResponseEntity actual = currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);

        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        getTokenGenerationDataResponse.addUnusedConfirmedTransaction(transactionHash);
        ResponseEntity expected = ResponseEntity.ok(getTokenGenerationDataResponse);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyCompletedToken_shouldReturnResponseWithAvailableTransactionAndCurrency() {
        when(currencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());

        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetTokenGenerationDataRequest getTokenGenerationDataRequest = new GetTokenGenerationDataRequest();
        getTokenGenerationDataRequest.setSenderHash(userHash);
        getTokenGenerationDataRequestCrypto.signMessage(getTokenGenerationDataRequest);
        ResponseEntity actual = currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);

        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        getTokenGenerationDataResponse.addCompletedTransactionHashToGeneratedCurrency(transactionHash, currencyData);
        ResponseEntity expected = ResponseEntity.ok(getTokenGenerationDataResponse);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyPendingToken_shouldReturnResponseWithPendingTransactionAndCurrency() {

        when(pendingCurrencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());

        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetTokenGenerationDataRequest getTokenGenerationDataRequest = new GetTokenGenerationDataRequest();
        getTokenGenerationDataRequest.setSenderHash(userHash);
        getTokenGenerationDataRequestCrypto.signMessage(getTokenGenerationDataRequest);
        ResponseEntity actual = currencyService.getUserTokenGenerationData(getTokenGenerationDataRequest);

        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        getTokenGenerationDataResponse.addPendingTransactionHashToGeneratedCurrency(transactionHash, currencyData);
        ResponseEntity expected = ResponseEntity.ok(getTokenGenerationDataResponse);
        Assert.assertEquals(expected, actual);
    }

    //TODO 9/22/2019 astolia: run TransactionService continueHandlePropagatedTransaction with TokenGenerationTransaction before some tests to mock insertion of data to db.

}
