package exchange.core.common;

import exchange.core.utils.HashingUtils;
import exchange.core.utils.SerializationUtils;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.bytes.WriteBytesMarshallable;
import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Objects;

@Slf4j
public final class UserProfile implements WriteBytesMarshallable, StateHash {

    public final long uid;

    // symbol -> margin position records
    // TODO initialize lazily (only needed if margin trading allowed)
    public final IntObjectHashMap<SymbolPositionRecord> positions;

    // protects from double adjustment
    public long adjustmentsCounter;

    // currency accounts
    // currency -> balance
    public final IntLongHashMap accounts;

    public UserStatus userStatus;

    public UserProfile(long uid, UserStatus userStatus) {
        //log.debug("New {}", uid);
        this.uid = uid;
        this.positions = new IntObjectHashMap<>();
        this.adjustmentsCounter = 0L;
        this.accounts = new IntLongHashMap();
        this.userStatus = userStatus;
    }

    public UserProfile(BytesIn bytesIn) {

        this.uid = bytesIn.readLong();

        // positions
        this.positions = SerializationUtils.readIntHashMap(bytesIn, b -> new SymbolPositionRecord(uid, b));

        // adjustmentsCounter
        this.adjustmentsCounter = bytesIn.readLong();

        // account balances
        this.accounts = SerializationUtils.readIntLongHashMap(bytesIn);

        // suspended
        this.userStatus = UserStatus.of(bytesIn.readByte());
    }

    public SymbolPositionRecord getPositionRecordOrThrowEx(int symbol) {
        final SymbolPositionRecord record = positions.get(symbol);
        if (record == null) {
            throw new IllegalStateException("not found position for symbol " + symbol);
        }
        return record;
    }

    @Override
    public void writeMarshallable(BytesOut bytes) {

        bytes.writeLong(uid);

        // positions
        SerializationUtils.marshallIntHashMap(positions, bytes);

        // adjustmentsCounter
        bytes.writeLong(adjustmentsCounter);

        // account balances
        SerializationUtils.marshallIntLongHashMap(accounts, bytes);

        // suspended
        bytes.writeByte(userStatus.getCode());
    }


    @Override
    public String toString() {
        return "UserProfile{" +
                "uid=" + uid +
                ", positions=" + positions.size() +
                ", accounts=" + accounts +
                ", adjustmentsCounter=" + adjustmentsCounter +
                ", userStatus=" + userStatus +
                '}';
    }

    @Override
    public int stateHash() {
        return Objects.hash(
                uid,
                HashingUtils.stateHash(positions),
                adjustmentsCounter,
                accounts.hashCode(),
                userStatus.hashCode());
    }
}
