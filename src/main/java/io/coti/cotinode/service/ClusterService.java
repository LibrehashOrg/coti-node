package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.IClusterService;
import io.coti.cotinode.service.interfaces.IQueueService;
import io.coti.cotinode.service.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

@Slf4j
@Service

public class ClusterService implements IClusterService {
    private final Vector<TransactionData>[] sourceListsByTrustScore = new Vector[101];
    @Value("${cluster.delay.after.tcc}")
    private int delayTimeAfterTccProcess;
    @Autowired
    private Transactions transactions;
    @Autowired
    private IQueueService queueService;
    @Autowired
    private ISourceSelector sourceSelector;
    @Autowired
    private TccConfirmationService tccConfirmationService;
    private ConcurrentHashMap<Hash, TransactionData> hashToUnconfirmedTransactionsMapping;

    private void initiateTrustScoreConsensusProcess() {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            while (true) {
                tccConfirmationService.init(hashToUnconfirmedTransactionsMapping);
                tccConfirmationService.topologicalSorting();
                List<Hash> transactionConsensusConfirmed = tccConfirmationService.setTransactionConsensus();

                for (Hash hash : transactionConsensusConfirmed) {
                    hashToUnconfirmedTransactionsMapping.remove(hash);
                    queueService.addToUpdateBalanceQueue(hash);
                    log.info("TCC has been reached for transaction {}!!", hash);
                }
                try {
                    TimeUnit.SECONDS.sleep(delayTimeAfterTccProcess);
                } catch (InterruptedException e) {
                    log.error(e.toString());
                }
            }
        });
    }

    public TransactionData attachToCluster(TransactionData newTransactionData) {
        hashToUnconfirmedTransactionsMapping.put(newTransactionData.getHash(), newTransactionData);
        removeTransactionParentsFromSources(newTransactionData);
        sourceListsByTrustScore[newTransactionData.getRoundedSenderTrustScore()].add(newTransactionData);
        log.info("added newTransactionData with hash:{}", newTransactionData.getHash());
        return newTransactionData;
    }

    private void removeTransactionParentsFromSources(TransactionData newTransactionData) {
        if(newTransactionData.getLeftParentHash() != null){
            removeTransactionFromSources(newTransactionData.getLeftParentHash());
        }
        if(newTransactionData.getRightParentHash() != null){
            removeTransactionFromSources(newTransactionData.getRightParentHash());
        }
    }

    private void removeTransactionFromSources(Hash transactionHash){
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if(sourceListsByTrustScore[transactionData.getRoundedSenderTrustScore()].contains(transactionData)){
            sourceListsByTrustScore[transactionData.getRoundedSenderTrustScore()].remove(transactionData); // TODO: synchronize
        }
    }

    @Override
    public TransactionData selectSources(TransactionData transactionData) {
        Vector<TransactionData>[] trustScoreToTransactionMappingSnapshot = new Vector[101];
        for (int i = 0; i <= 100; i++) {
            trustScoreToTransactionMappingSnapshot[i] = (Vector<TransactionData>) sourceListsByTrustScore[i].clone();
        }

        List<TransactionData> selectedSourcesForAttachment =
                sourceSelector.selectSourcesForAttachment(
                        trustScoreToTransactionMappingSnapshot,
                        transactionData.getSenderTrustScore());

        if (selectedSourcesForAttachment.size() > 0) {
            transactionData.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
        }
        if (selectedSourcesForAttachment.size() > 1) {
            transactionData.setRightParentHash(selectedSourcesForAttachment.get(1).getHash());
        }

        String hashes = "";
        for (TransactionData td: selectedSourcesForAttachment) {
            hashes += td.getHash() + " ";
        }
        log.info("For transaction with hash:{} we found the following sources:{}",transactionData.getHash(), hashes);

        return transactionData;
    }

    @Override
    public void setInitialUnconfirmedTransactions(List<Hash> transactionHashes) {
        for (int i = 0; i <= 100; i++) {
            sourceListsByTrustScore[i] = (new Vector<>());
        }

        hashToUnconfirmedTransactionsMapping = new ConcurrentHashMap<>();
        for(Hash transactionHash
                : transactionHashes){
            TransactionData transactionData = transactions.getByHash(transactionHash);
            hashToUnconfirmedTransactionsMapping.put(transactionHash, transactionData);
            sourceListsByTrustScore[transactionData.getRoundedSenderTrustScore()].add(transactionData);
        }

        initiateTrustScoreConsensusProcess();
    }
}
