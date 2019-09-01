package eu.arrowhead.common.messages;


import static eu.arrowhead.common.messages.ComplianceCategory.*;

public class ComplianceDetailResult {

    public enum ResultStrings {
        OK(COMPLIANT, "[ OK ]"),
        FOUND(COMPLIANT, "[ FOUND ]"),
        NOT_FOUND(NOT_COMPLIANT, "[ NOT FOUND ]"),
        WARNING(NOT_COMPLIANT, "[ WARNING ]"),
        UNKNOWN(ERROR, null);

        private final ComplianceCategory category;
        private final String string;
        ResultStrings(final ComplianceCategory category, final String string)
        {
            this.category = category;
            this.string = string;
        }
        public ComplianceCategory getCategory() { return category; }
        public boolean matches(final String candidate) { return string.equals(candidate); }
        public static ResultStrings analyse(final String string)
        {
            for(ResultStrings candidate : ResultStrings.values())
            {
                if(candidate != UNKNOWN && candidate.matches(string))
                {
                    return candidate;
                }
            }
            return UNKNOWN;
        }
    }

    String testName;
    ResultStrings result;

    public ComplianceDetailResult(String testName, String resultString) {
        this.testName = testName;
        this.result = ResultStrings.analyse(resultString);
    }

    public ComplianceDetailResult() {
        super();
    }

    public void setTestName(final String testName)
    {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public ResultStrings getResult() {
        return result;
    }

    public void setResult(ResultStrings result) {
        this.result = result;
    }

    public boolean isCompliant()
    {
        return result.getCategory() == COMPLIANT;
    }

    public boolean isError()
    {
        return result.getCategory() == ERROR;
    }

    public boolean isNotCompliant()
    {
        return result.getCategory() == NOT_COMPLIANT;
    }
}
