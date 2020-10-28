
import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.VMListener;
import gov.nasa.jpf.ListenerAdapter;

import java.util.*;
import java.io.*;

public class CoverageListener extends ListenerAdapter {

    private Set<String> lines = new TreeSet<>();
    private HashMap<String, List<Integer>> coverageMap = new HashMap<>();


    private String className(String l) {
        return l.split(":")[0];
    }

    private Integer lineNumber(String l) {
        return Integer.parseInt(l.split(":")[1]);
    }

    private void clearDuplicates() {
        for (Map.Entry mapElement : coverageMap.entrySet()) { 
            mapElement.setValue(new ArrayList<Integer>(new HashSet<Integer>((List<Integer>) mapElement.getValue())));
        }
    }

    private void writeCoverageResults() {
        String text = formatForWriting();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream("results.txt"), "utf-8"))) {
                writer.write(text);
            }
        catch (Exception ex) {
            System.out.println("there was an error");
        }
    }

    private String formatForWriting() {
        TreeMap<String, List<Integer>> treeMap = new TreeMap<>(coverageMap);
        return treeMap.toString();
    }

    @Override
    public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
        String l = instructionToExecute.getFileLocation();
        if (l != null && !l.startsWith("java/") && !l.startsWith("sun/") && !l.startsWith("gov/")) {
            lines.add(l);
            if(coverageMap.containsKey(className(l))) {
                coverageMap.get(className(l)).add(lineNumber(l));
            } else {
                coverageMap.put(className(l), new ArrayList<Integer>(Arrays.asList(lineNumber(l))));
            }
        }
    }

    @Override
    public void searchFinished(Search search) {
        clearDuplicates();
        writeCoverageResults();
    }
}
