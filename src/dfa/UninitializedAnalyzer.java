package dfa;

import cfg.*;
import ir.*;
import java.util.*;

public class UninitializedAnalyzer {
    private ControlFlowGraph cfg;
    private Set<String> uninitializedVariables;

    public UninitializedAnalyzer(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.uninitializedVariables = new TreeSet<>();
    }

    public Set<String> analyze() {
        DfaEngine engine = new DfaEngine(cfg);
        engine.run();

        for (BasicBlock block : cfg.getBlocks()) {
            Set<String> currentInitialized = new HashSet<>(engine.getInSet(block));
            
            for (IrCommand cmd : block.getInstructions()) {
                Set<String> uses = DfaEngine.getUsedVars(cmd);
                
                for (String use : uses) {
                    if (!currentInitialized.contains(use)) {
                        String baseName = getBaseName(use);
                        if (baseName != null) {
                            uninitializedVariables.add(baseName);
                        }
                    }
                }
                
                Set<String> defs = DfaEngine.getDefinedVars(cmd);
                boolean allUsesInitialized = currentInitialized.containsAll(uses);
                
                for (String d : defs) {
                    currentInitialized.remove(d);
                    if (allUsesInitialized) {
                        currentInitialized.add(d);
                    }
                }
            }
        }
        
        return uninitializedVariables;
    }

    private String getBaseName(String uniqueName) {
        if (uniqueName.startsWith("Temp_")) {
            return null;
        }
        
        int lastUnderscore = uniqueName.lastIndexOf('_');
        if (lastUnderscore != -1) {
            String suffix = uniqueName.substring(lastUnderscore + 1);
            try {
                Integer.parseInt(suffix);
                return uniqueName.substring(0, lastUnderscore);
            } catch (NumberFormatException e) {
                return uniqueName;
            }
        }
        
        return uniqueName;
    }
}
