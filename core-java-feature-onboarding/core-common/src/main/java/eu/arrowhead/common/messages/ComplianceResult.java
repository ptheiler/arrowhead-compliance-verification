package eu.arrowhead.common.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplianceResult implements Serializable {
    private List<ComplianceDetailResult> results = new ArrayList<>();
    private int hardeningIndex = 0;
    private int success = 0;
    private int error = 0;
    private int ignored = 0;
    public ComplianceResult()
    {
        super();
    }

    public void setResults(List<ComplianceDetailResult> results) {
        this.results = results;
        for (ComplianceDetailResult line : results) {
            switch(line.result) {
                // line.result.getCategory()
                case OK:
                case FOUND:
                    success++;
                    break;
                case NOT_FOUND:
                case WARNING:
                    error++;
                    break;
                case UNKNOWN:
                    ignored++;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + line.result);
            }
        }
    }

    public ComplianceResult(final List<ComplianceDetailResult> results)
    {
        setResults(results);
    }

    public int getHardeningIndex() { return hardeningIndex; }
    public void setHardeningIndex(int hardeningIndex) { this.hardeningIndex = hardeningIndex; }
    public int getTotal()
    {
        return (success + ignored + error);
    }
    public List<ComplianceDetailResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ComplianceResult{");
        sb.append("success=").append(success);
        sb.append(", error=").append(error);
        sb.append(", ignored=").append(ignored);
        sb.append(", total=").append(getTotal());
        sb.append(", Compliance Index: ").append(hardeningIndex);
        sb.append('}');
        return sb.toString();
    }

    public int getError() {
        return error;
    }

    public int getIgnored() {
        return ignored;
    }

    public int getSuccess() {
        return success;
    }

    private int getPercentage(int factor)
    {
        return ((factor * 100) / getTotal());
    }

    public int getSucccessPercentage()
    {
        return getPercentage(success);
    }
    public int getErrorPercentage()
    {
        return getPercentage(error);
    }
    public int getIgnoredPercentage()
    {
        return getPercentage(ignored);
    }
}
