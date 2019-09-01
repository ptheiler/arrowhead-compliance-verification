package eu.arrowhead.core.compliance;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
public class ComplianceServiceMain extends ArrowheadMain {

    private ComplianceServiceMain(String[] args) {
        String[] packages = {"eu.arrowhead.common.exception", "eu.arrowhead.common.json", "eu.arrowhead.common.filter",
            "eu.arrowhead.core.compliance"};
        init(CoreSystem.COMPLIANCE, args, null, packages);

        listenForInput();
    }
    public static void main(String[] args) {
        new ComplianceServiceMain(args);
    }
}