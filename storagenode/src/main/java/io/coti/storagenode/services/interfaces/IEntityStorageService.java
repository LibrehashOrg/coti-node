package io.coti.storagenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.data.enums.ElasticSearchData;
import io.coti.storagenode.model.ObjectService;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IEntityStorageService
{
    ResponseEntity<IResponse> storeObjectToStorage(Hash hash, String objectJson);

    ResponseEntity<IResponse> retrieveObjectFromStorage(Hash hash, ElasticSearchData objectType);

    ResponseEntity<IResponse> storeMultipleObjectsToStorage(Map<Hash, String> hashToObjectJsonDataMap);

    ResponseEntity<IResponse> retrieveMultipleObjectsFromStorage(List<Hash> hashes);

    boolean isObjectDIOK(Hash objectHash, String objectAsJson);
}
