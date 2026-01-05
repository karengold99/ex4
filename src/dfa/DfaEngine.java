package dfa;

import cfg.*;
import ir.*;
import java.util.*;

public class DfaEngine {
    private ControlFlowGraph cfg;
    private Map<BasicBlock, Set<String>> inSets;
    private Map<BasicBlock, Set<String>> outSets;

    public DfaEngine(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.inSets = new HashMap<>();
        this.outSets = new HashMap<>();
        initialize();
    }

    private void initialize() {
        Set<String> allVars = new HashSet<>();
        for (BasicBlock block : cfg.getBlocks()) {
            for (IrCommand cmd : block.getInstructions()) {
                allVars.addAll(getDefinedVars(cmd));
                allVars.addAll(getUsedVars(cmd));
            }
        }
        
        for (BasicBlock block : cfg.getBlocks()) {
            inSets.put(block, new HashSet<>(allVars));
            outSets.put(block, new HashSet<>(allVars));
        }

        if (cfg.getEntryBlock() != null) {
            inSets.put(cfg.getEntryBlock(), new HashSet<>());
        }
    }

    public void run() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock block : cfg.getBlocks()) {
                Set<String> newIn;
                if (block.isEntry()) {
                    newIn = new HashSet<>();
                } else {
                    newIn = null;
                    for (BasicBlock pred : block.getPredecessors()) {
                        if (newIn == null) {
                            newIn = new HashSet<>(outSets.get(pred));
                        } else {
                            newIn.retainAll(outSets.get(pred));
                        }
                    }
                    if (newIn == null) newIn = new HashSet<>();
                }

                if (!newIn.equals(inSets.get(block))) {
                    inSets.put(block, newIn);
                    changed = true;
                }

                Set<String> newOut = transferFunction(block, newIn);
                if (!newOut.equals(outSets.get(block))) {
                    outSets.put(block, newOut);
                    changed = true;
                }
            }
        }
    }

    private Set<String> transferFunction(BasicBlock block, Set<String> in) {
        Set<String> current = new HashSet<>(in);
        for (IrCommand cmd : block.getInstructions()) {
            Set<String> defs = getDefinedVars(cmd);
            Set<String> uses = getUsedVars(cmd);

            boolean allUsesInitialized = current.containsAll(uses);
            
            for (String d : defs) {
                current.remove(d);
                if (allUsesInitialized) {
                    current.add(d);
                }
            }
        }
        return current;
    }

    public Set<String> getInSet(BasicBlock block) {
        return inSets.get(block);
    }

    /**
     * Centralized logic to get variables/temporaries used by an IR command.
     * This replaces the logic that was previously in the IR classes.
     */
    public static Set<String> getUsedVars(IrCommand cmd) {
        if (cmd instanceof IrCommandLoad) {
            return Collections.singleton(((IrCommandLoad) cmd).getVarName());
        }
        if (cmd instanceof IrCommandStore) {
            return Collections.singleton("Temp_" + ((IrCommandStore) cmd).getSrc().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopAddIntegers) {
            IrCommandBinopAddIntegers c = (IrCommandBinopAddIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopSubIntegers) {
            IrCommandBinopSubIntegers c = (IrCommandBinopSubIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopMulIntegers) {
            IrCommandBinopMulIntegers c = (IrCommandBinopMulIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopDivIntegers) {
            IrCommandBinopDivIntegers c = (IrCommandBinopDivIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopEqIntegers) {
            IrCommandBinopEqIntegers c = (IrCommandBinopEqIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopLtIntegers) {
            IrCommandBinopLtIntegers c = (IrCommandBinopLtIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopGtIntegers) {
            IrCommandBinopGtIntegers c = (IrCommandBinopGtIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandJumpIfEqToZero) {
            return Collections.singleton("Temp_" + ((IrCommandJumpIfEqToZero) cmd).getTemp().getSerialNumber());
        }
        if (cmd instanceof IrCommandPrintInt) {
            return Collections.singleton("Temp_" + ((IrCommandPrintInt) cmd).getTemp().getSerialNumber());
        }
        if (cmd instanceof IrCommandReturn) {
            IrCommandReturn c = (IrCommandReturn) cmd;
            if (c.getReturnValue() != null) {
                return Collections.singleton("Temp_" + c.getReturnValue().getSerialNumber());
            }
        }
        return Collections.emptySet();
    }

    /**
     * Centralized logic to get variables/temporaries defined by an IR command.
     */
    public static Set<String> getDefinedVars(IrCommand cmd) {
        if (cmd instanceof IrCommandLoad) {
            return Collections.singleton("Temp_" + ((IrCommandLoad) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandStore) {
            return Collections.singleton(((IrCommandStore) cmd).getVarName());
        }
        if (cmd instanceof IRcommandConstInt) {
            return Collections.singleton("Temp_" + ((IRcommandConstInt) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopAddIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopAddIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopSubIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopSubIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopMulIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopMulIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopDivIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopDivIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopEqIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopEqIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopLtIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopLtIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopGtIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopGtIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandAllocate) {
            return Collections.singleton(((IrCommandAllocate) cmd).getVarName());
        }
        return Collections.emptySet();
    }
}
