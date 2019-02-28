package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
public class NetworkNodeData implements IEntity, ISignable, ISignValidatable {
    private Hash nodeHash;
    private NodeType nodeType;
    private String address;
    private String httpPort;
    private String propagationPort;
    private String receivingPort;
    private String recoveryServerAddress;
    private NetworkType networkType;
    private transient Double trustScore;
    private transient Double feePercentage;
    private SignatureData nodeSignature;
    private NodeRegistrationData nodeRegistrationData;


    public NetworkNodeData(NodeType nodeType, String address, String httpPort, Hash nodeHash, NetworkType networkType) {
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
        this.nodeHash = nodeHash;
        this.networkType = networkType;
    }

    public NetworkNodeData(NodeType nodeType, String address, String httpPort, Hash nodeHash, NetworkType networkType, double feePercentage) {
        this(nodeType, address, httpPort, nodeHash, networkType);

        if (trustScore < 0.0 || trustScore > 100.0) {
            log.error("Trust score is invalid! trustScore: {} ", trustScore);
            return;
        }
        this.feePercentage = feePercentage;
        this.nodeType = nodeType;
        this.address = address;
        this.httpPort = httpPort;
        this.nodeHash = nodeHash;
    }

    public NetworkNodeData() {
    }

    public NetworkNodeData(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public boolean equals(Object a) {
        if (a instanceof NetworkNodeData) {
            NetworkNodeData aNetworkNodeData = (NetworkNodeData) a;
            return nodeType.equals(aNetworkNodeData.nodeType) && nodeHash.equals(aNetworkNodeData.getHash());
        } else {
            return false;
        }
    }

    public String getHttpFullAddress() {
        return "http://" + address + ":" + httpPort;
    }

    public String getPropagationFullAddress() {
        return "tcp://" + address + ":" + propagationPort;
    }

    public String getReceivingFullAddress() {
        return "tcp://" + address + ":" + receivingPort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeType, address, httpPort, nodeHash);
    }

    public Hash getHash() {
        return nodeHash;
    }

    public void setHash(Hash hash) {
        nodeHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return nodeSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.nodeSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }
}