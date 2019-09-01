package eu.arrowhead.core.compliance;

import eu.arrowhead.common.messages.ComplianceDetailResult;
import eu.arrowhead.common.messages.ComplianceResult;
import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComplianceChecker {

    private final Pattern lynisOutput = Pattern.compile("(.*\\bVerifying\\b.*)(\\[.*\\])");
    private final Pattern hardeningOutput = Pattern.compile("Hardening index : (\\d+)");

    public ComplianceResult checkService(final String ipAddress) throws IOException{
        final ComplianceChecker checker = new ComplianceChecker();
        final ComplianceResult result = new ComplianceResult();

        checker.performComplianceCheck(result, "bash", "-c", "sudo mkdir -p /home/pi/lynis/files && cd /home/pi/ && "
            + "sudo tar "
            + "czf ./lynis/files/lynis-remote.tar.gz --exclude=files/lynis-remote.tar.gz "
            + "./lynis && cd "
            + "lynis");
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q /home/pi/lynis/files/lynis-remote.tar.gz"
                                                                       + " pi@%s:~/tmp-lynis-remote.tgz", ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s \"sudo mkdir -p ~/tmp-lynis "
                                                                                 + "&& cd "
                                                                                 + "~/tmp-lynis "
                                                                                 + "&& sudo tar xzf ../tmp-lynis-remote.tgz && sudo rm ../tmp-lynis-remote.tgz && cd lynis && sudo ./lynis "
                                                                                 + "audit system --tests-from-group "
                                                                                 + "service.txt --no-colors | sed "
                                                                                 + "'s/\\x1B\\[[0-9;]*[C]//g' " + "&& "
                                                                                 + "sudo chmod "
                                                                                 + "644 /var/log/lynis.log "
                                                                                 + "/var/log/lynis-report.dat\"",
                                                                                 ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s sudo rm -rf ~/tmp-lynis",
                                                                   ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q pi@%s:/var/log/lynis.log "
                                                                       + "/home/pi/pi@%s-lynis-ser"
                                                                       + ".log", ipAddress, ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q pi@%s:/var/log/lynis-report.dat "
                                                                       + "/home/pi/pi@%s-lynis-report-ser.dat",
                                                                   ipAddress, ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s sudo rm /var/log/lynis.log "
                                                                       + "/var/log/lynis-report.dat", ipAddress));
        getserreport(ipAddress);

        return result;
    }

    public ComplianceResult checkSystem(final String ipAddress) throws IOException{
        final ComplianceChecker checker = new ComplianceChecker();
        final ComplianceResult result = new ComplianceResult();

        checker.performComplianceCheck(result, "bash", "-c", "sudo mkdir -p /home/pi/lynis/files && cd /home/pi/ && sudo tar "
            + "czf "
            + "./lynis/files/lynis-remote.tar.gz --exclude=files/lynis-remote.tar.gz ./lynis &&"
            + " cd "
            + "lynis");
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q /home/pi/lynis/files/lynis-remote.tar.gz"
                                                                       + " pi@%s:~/tmp-lynis-remote.tgz", ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s \"sudo mkdir -p ~/tmp-lynis "
                                                                                 + "&& cd "
                                                                                 + "~/tmp-lynis "
                                                                                 + "&& sudo tar xzf ../tmp-lynis-remote.tgz && sudo rm ../tmp-lynis-remote.tgz && cd lynis && sudo ./lynis "
                                                                                 + "audit system --tests-from-group "
                                                                                 + "system.txt --no-colors | sed "
                                                                                 + "'s/\\x1B\\[[0-9;]*[C]//g' " + "&& "
                                                                                 + "sudo chmod "
                                                                                 + "644 /var/log/lynis.log "
                                                                                 + "/var/log/lynis-report.dat\"",
                                                                                 ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s sudo rm -rf ~/tmp-lynis",
                                                                   ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q pi@%s:/var/log/lynis.log "
                                                                       + "/home/pi/pi@%s-lynis-sys"
                                                                       + ".log", ipAddress, ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q pi@%s:/var/log/lynis-report.dat "
                                                                       + "/home/pi/pi@%s-lynis-report-sys.dat",
                                                                   ipAddress, ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s sudo rm /var/log/lynis.log "
                                                                       + "/var/log/lynis-report.dat", ipAddress));
        getsysreport(ipAddress);

        return result;
    }

    public ComplianceResult checkDevice(final String ipAddress) throws IOException {
        final ComplianceChecker checker = new ComplianceChecker();
        final ComplianceResult result = new ComplianceResult();
        final List<ComplianceDetailResult> results;

        checker.performComplianceCheck(result, "bash", "-c", "sudo mkdir -p /home/pi/lynis/files && cd /home/pi/ && "
            + "sudo tar "
            + "czf "
            + "./lynis/files/lynis-remote.tar.gz --exclude=files/lynis-remote.tar.gz ./lynis && cd "
            + "lynis");
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q /home/pi/lynis/files/lynis-remote"
                                                                              + ".tar.gz"
                                                                       + " pi@%s:~/tmp-lynis-remote.tgz", ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s \"sudo mkdir -p " + "~/tmp-lynis "
                                                                                 + "&& cd "  + "~/tmp-lynis " + "&& " + "sudo tar xzf ."
                                                                                 + "./tmp-lynis-remote.tgz && sudo rm" + " ../tmp-lynis-remote.tgz"
                                                                                 + "&& cd "
                                                                                 + "lynis && sudo ./lynis "
                                                                                 + "audit system --tests-from-group "
                                                                                 + "device.txt --no-colors | sed "
                                                                                 + "'s/\\x1B\\[[0-9;]*[C]//g' " + "&& "
                                                                                 + "sudo chmod "
                                                                                 + "644 /var/log/lynis.log "
                                                                                 + "/var/log/lynis-report.dat\"",
                                                                                 ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s sudo rm -rf ~/tmp-lynis",
                                                                   ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q pi@%s:/var/log/lynis.log "
                                                                       + "/home/pi/pi@%s-lynis-dev"
                                                                       + ".log", ipAddress, ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("scp -q pi@%s:/var/log/lynis-report.dat "
                                                                       + "/home/pi/pi@%s-lynis-report-dev.dat",
                                                                   ipAddress, ipAddress));
        checker.performComplianceCheck(result, "bash", "-c", String.format("ssh pi@%s sudo rm /var/log/lynis.log "
                                                                       + "/var/log/lynis-report.dat", ipAddress));
        getdevreport(ipAddress);

        return result;
    }

    private void performComplianceCheck(final ComplianceResult result,
                                                                final String... command) throws IOException {
        final List<ComplianceDetailResult> results = new ArrayList<>();
        final ProcessBuilder builder;
        final Process process;

        builder = new ProcessBuilder();
        builder.command(command);
        process = builder.start();

        InputStreamReader stdOut = new InputStreamReader(process.getInputStream());
        InputStreamReader stdErr = new InputStreamReader(process.getErrorStream());

        BufferedReader stdOutReader = new BufferedReader(stdOut);
        BufferedReader stdErrReader = new BufferedReader(stdErr);

        do {
            while(stdOutReader.ready()) {
                final String line = stdOutReader.readLine();
                if (line == null || line.isEmpty()) {
                    continue;
                }
                try {
                    results.add(analyseOutput(line));
                } catch (NoMatchFoundException e) {
                    // ignored
                } catch (NullPointerException e) {
                    e.printStackTrace(System.err);
                } catch (Exception e) {
                    System.err.println("ERROR msg:" + e.getMessage());
                    System.err.println("DEBUG: " + line);
                }
                try {
                    result.setHardeningIndex(analyseHardening(line));
                } catch (NoMatchFoundException e) {
                    // ignored
                }
            }
            while(stdErrReader.ready())
            {
                System.out.println(stdErrReader.readLine());
            }
        }
        while(process.isAlive());

        if(process.exitValue() != 0)
        {
            throw new IOException("process terminated with " + process.exitValue() + ". cmd:" + command);
        }

        result.setResults(results);

    }


    /*
    public static void main(final String args []) throws IOException {

        final ComplianceChecker checker = new ComplianceChecker();
        final List<ComplianceDetailResult> results = checker.performComplianceCheck("bash", "-c", "lynis audit system");

        int countCompliant = 0;

        for (ComplianceDetailResult result : results) {
            if(result.isCompliant())
            {
                countCompliant++;
            }
        }

        System.out.println("Result of compliance: " + countCompliant + "/" + results.size());
    }
*/
    private ComplianceDetailResult analyseOutput(String line) throws IOException {
        line = line.trim();

        if(line.startsWith("+ "))
        {
            line = line.substring(2);
        }

        final Matcher matcher = lynisOutput.matcher(line);
        if(!matcher.find()) {
            throw new NoMatchFoundException(line);
        }
        if(matcher.groupCount() != 2)
        {
            throw new IOException("Couldn't match 2 groups on line: " + line);
        }
        final String testName = trim(matcher.group(1));
        final String result = trim(matcher.group(2));

        return new ComplianceDetailResult(testName, result);
    }

    private Integer analyseHardening(String line) throws IOException {
        line = line.trim();

        final Matcher matcher = hardeningOutput.matcher(line);
        if(!matcher.find())
        {
            throw new NoMatchFoundException(line);
        }

        if(matcher.groupCount() != 1)
        {
            throw new IOException("Couldn't match group on line: " + line);
        }

        return Integer.valueOf(trim(matcher.group(1)));
    }


    private String trim(final String string)
    {
        if(string == null)
        {
            return "";
        }
        else
        {
            return string.trim();
        }
    }

    private static class NoMatchFoundException extends IOException
    {
        final String line;

        private NoMatchFoundException(final String line) {
            super("No match for line: " + line);
            this.line = line;
        }

        public String getLine() {
            return line;
        }
    }

    public void getdevreport(final String ipAddress) {
        final String fileName = String.format("/home/pi/pi@%s-lynis-dev.log", ipAddress);
        File file = new File(fileName);
        Scanner in;
        try {
            in = new Scanner(file);
            while (in.hasNext()) {
                String line = in.nextLine();
                if (line.contains("Metric "))
                    System.out.println(line);
                if (line.contains("Result "))
                    System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void getsysreport(final String ipAddress) {
        final String fileName = String.format("/home/pi/pi@%s-lynis-sys.log", ipAddress);
        File file = new File(fileName);
        Scanner in;
        try {
            in = new Scanner(file);
            while (in.hasNext()) {
                String line = in.nextLine();
                if (line.contains("Metric "))
                    System.out.println(line);
                if (line.contains("Result "))
                    System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void getserreport(final String ipAddress) {
        final String fileName = String.format("/home/pi/pi@%s-lynis-ser.log", ipAddress);
        File file = new File(fileName);
        Scanner in;
        try {
            in = new Scanner(file);
            while (in.hasNext()) {
                String line = in.nextLine();
                if (line.contains("Metric "))
                    System.out.println(line);
                if (line.contains("Result "))
                    System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
