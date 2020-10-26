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
import gov.nasa.jpf.jvm.JVMStackFrame;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.ReferenceFieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.jvm.bytecode.*;
import gov.nasa.jpf.vm.StackFrame;

import java.util.*;
import java.io.*;

public class MemListener extends ListenerAdapter {
    HashMap<String, Object> MemorizationMap = new HashMap<String, Object>();

    // given an element info do a DFS to find the actual information, ultimately returns
    // a list of objects to be used in the hash
    private List<Object> ElementInfoDfs(ElementInfo ei, ThreadInfo currentThread) {
        List<Object> objects = new ArrayList();
        if(ei != null) {
            for(int i = 0; i < ei.getNumberOfFields(); i++) {
                FieldInfo fi = ei.getFieldInfo(i);
                if(fi.isReference()) {
                    Fields field = ei.getFields();
                    ElementInfo ei2 = currentThread.getElementInfo(field.getIntValue(i));
                    objects.add(ElementInfoDfs(ei2, currentThread));
                } else {
                    // primitive
                    objects.add(fi.getValueObject(ei.getFields()));
                }
            }
        }
        
        return objects;
    }

    // handles stack frames, and variables for the program to memoize
    private StackFrame Memoize(ThreadInfo currentThread, Instruction instructionToExecute, MethodInfo mi, ClassInfo ci) {
        String methodName = mi.getName();
        String methodReturnType = mi.getReturnType();
        Object returnValue = getReturnValue(instructionToExecute, currentThread);
        System.out.println("Return Value: " + returnValue);
        String className = ci.getName();
        System.out.println("Class Name: " + className);
        System.out.println("Method Name: " + methodName);
        System.out.println("method return type: " + methodReturnType);
        
        String key = className + ":" + methodName;
        StackFrame sf = currentThread.getModifiableTopFrame();
        for (int i = 0; i < sf.getLocalVariableCount(); i++) {
            System.out.println("local var: " + sf.getLocalVariable(i));
            String a = "";
            if(sf.isLocalVariableRef(i)) {
                ElementInfo ei = currentThread.getElementInfo(sf.getLocalVariable(i));
                List<Object> objects = ElementInfoDfs(ei, currentThread);
                for(int j = 0; j < objects.size(); j++) {
                    a += ":" + objects.get(j);
                }
            } else {
                System.out.println("sf local var type non ref: " + sf.getLocalVariableType(i));
                // assumed that no ints would be more than 1m
                // because doubles are stored as large numbers in the sf
                if(sf.getLocalVariable(i) > 1000000) {
                    a += ":" + sf.getDoubleLocalVariable(i);
                    i++;
                } else {
                    a += ":" + sf.getLocalVariable(i);
                }
            }
            
            System.out.println("a " + a);
            if(!a.equals("")) {
                key += a;
            } else {
                key = key + ":" + sf.getLocalVariable(i);
            }
        }

        System.out.println("key: " + key);
        Object is_in_map = MemorizationMap.get(key);

        if (is_in_map != null){
            // return is_in_map instead
            System.out.println("Action in memoize, popping " + sf.getLocalVariableCount());
            System.out.println("value : " + is_in_map);
            int originalCount = sf.getLocalVariableCount();
            for (int i = 0; i < sf.getLocalVariableCount(); i++) {
                sf.pop();
            }
            

            // extra pop for clean up
            if(instructionToExecute instanceof DRETURN) {
                sf.popDouble();
            }
            else {
                sf.pop();
            }
            sf = pushToFrame(sf, instructionToExecute, is_in_map);
            // Error that i ran into was dealing with an assert at
            // https://github.com/javapathfinder/jpf-core/blob/0df77f0a2a8fa55d58a4ed89b70b61e39626866c/src/main/gov/nasa/jpf/vm/StackFrame.java#L574
            // the problem was in the stack, the top var was smaller than the normal
            // my solution is to padd with 0's to get to the same number of TOP
            // this does make the stackframe uglier but it contains the correct values
            // this only needed to happen on doubles, java man, java.
            if(instructionToExecute instanceof DRETURN) {
                for(int i = 0; i < originalCount; i++) {
                    sf.push(0);
                }
            }

            appendToFile("----MEMOIZED----");
            appendToFile("This hash seen before: " + key);
            appendToFile("the result is: " + is_in_map);
            appendToFile("new stackframe is: " + sf.toString());
            appendToFile("----END MEMOIZED----");
            
            System.out.println("----MEMOIZED----");
        }

        else{
            appendToFile("This hash not seen before: " + key);
            System.out.println("putting in MemorizationMap: " + key);
            MemorizationMap.put(key, returnValue);
        }
        return sf;
    }

    // --------------- HELPER FUNCTIONS ---------------
    private Object getReturnValue(Instruction instructionToExecute, ThreadInfo currentThread) {
        if(instructionToExecute instanceof IRETURN) {
            IRETURN ret = (IRETURN) instructionToExecute;
            return ret.getReturnValue(currentThread);
        }
        else {
            DRETURN ret = (DRETURN) instructionToExecute;
            return ret.getReturnValue(currentThread);
        }
    }

    private StackFrame pushToFrame(StackFrame sf, Instruction instructionToExecute, Object value) {
        if(instructionToExecute instanceof IRETURN) {
            return pushInt(sf, value);
        }
        else if (instructionToExecute instanceof DRETURN) {
            return pushDouble(sf, value);
        }
        return null;
    }

    private StackFrame pushInt(StackFrame sf, Object value) {
        sf.push((int)value);
        return sf;
    }

    private StackFrame pushDouble(StackFrame sf, Object value) {
        sf.pushDouble((double)value);
        return sf;
    }

    private void printCleanBreak() {
        System.out.println("****************");
    }

    private void appendToFile(String info) {
        try {
            FileWriter fw = new FileWriter("results.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.newLine();
            bw.write(info);
            bw.close();
        } catch (Exception ex)  {
            System.out.println("there was an error");
        }
       
    }
    // --------------- END HELPER FUNCTIONS ---------------



    @Override
    public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
        MethodInfo mi = currentThread.getTopFrameMethodInfo();
        ClassInfo ci = mi.getClassInfo();
        String methodName = mi.getName();
        String init = "<init>";
        String l = instructionToExecute.getFileLocation();
        if (l != null && !l.startsWith("java/") && !l.startsWith("sun/") && !l.startsWith("gov/")) {
            if (instructionToExecute instanceof JVMReturnInstruction
                    && mi != null
                    && !(instructionToExecute instanceof NATIVERETURN)
                    && !(methodName.equals(init))
                    && !(methodName.equals("<clinit>"))
            ) {
                if(instructionToExecute instanceof IRETURN || instructionToExecute instanceof DRETURN) {
                    // as per instructions ints and doubles are the areas we care about
                    StackFrame memoized = Memoize(currentThread, instructionToExecute, mi, ci);
                    currentThread.popFrame();
                    currentThread.pushFrame(memoized);
                    System.out.println("sf after memoization: " + memoized);
                    printCleanBreak();
                }
            }
        }
    }
}
