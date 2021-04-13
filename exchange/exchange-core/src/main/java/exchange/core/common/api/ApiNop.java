package exchange.core.common.api;


import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode(callSuper = false)
public final class ApiNop extends ApiCommand {
    @Override
    public String toString() {
        return "[NOP]";
    }
}
