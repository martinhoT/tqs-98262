package tqs.assign.data;

public final class NullStats extends Stats {

    public NullStats() {
        super(
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD
        );
    }

    @Override
    public boolean isNull() { return true; }

}
