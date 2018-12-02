package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
public class RollingReserveReleaseDateData implements IEntity {
    private Date date;
    private Map<Hash,Set<Hash>> merchantTransactionHashes;

    public void setDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.date = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public Hash getHash() {
        return new Hash(date.getTime());
    }

    @Override
    public void setHash(Hash hash) {
    }
}